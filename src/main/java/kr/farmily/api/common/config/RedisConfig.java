package kr.farmily.api.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import kr.farmily.api.common.cache.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * ElastiCache Redis 를 핫 read 캐시로 사용하기 위한 설정.
 * <ul>
 *   <li>값 직렬화: {@link JacksonConfig} 의 ObjectMapper 를 복사해 default typing + 필드 가시성만 추가
 *       → record/엔티티(WeatherSnapshot, Crop) 모두 역직렬화 가능. MVC 응답용 ObjectMapper 는 건드리지 않음.</li>
 *   <li>캐시별 TTL: {@link RedisCacheManagerBuilderCustomizer} 로 지정.</li>
 *   <li>키 prefix 버전화({@link CacheNames#VERSION}): 직렬화 포맷 변경/롤링배포 시 옛 포맷 키와 자동 격리.</li>
 *   <li>내성: {@link CacheErrorHandler} 로 역직렬화 등 캐시 오류를 삼켜 캐시 미스로 강등(+깨진 키 evict) → 500 대신 DB 폴백.</li>
 * </ul>
 */
@Configuration
public class RedisConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisMapper = objectMapper.copy();
        // 게터/세터가 없는 엔티티(@Getter only)도 필드 직접 접근으로 직렬화/역직렬화
        redisMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        redisMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 역직렬화 시 구체 타입 복원을 위해 타입정보 포함(WRAPPER_ARRAY, 내부 신뢰 데이터).
        // ★ EVERYTHING 사용: 캐시 값 DTO 가 Java record(=final)라 NON_FINAL 로는 루트에 타입정보가 안 붙어
        //   쓰기 {본문} / 읽기 [타입,{본문}] 비대칭 → 캐시 HIT 마다 SerializationException 발생. EVERYTHING 이 final 도 포함.
        redisMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.EVERYTHING);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                // prefix 에 버전 포함: <cacheName>::<VERSION>::<key> → 포맷 변경 시 VERSION bump 으로 옛 키 자동 격리
                .computePrefixWith(cacheName -> cacheName + "::" + CacheNames.VERSION + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheTtlCustomizer(RedisCacheConfiguration base) {
        return builder -> builder
                .withCacheConfiguration(CacheNames.WEATHER, base.entryTtl(Duration.ofMinutes(60)))
                .withCacheConfiguration(CacheNames.PUBLIC_PROFILE, base.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CacheNames.MY_PROFILE, base.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CacheNames.MY_PAGE, base.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration(CacheNames.CROPS, base.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration(CacheNames.NOTIFICATION_SETTINGS, base.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(CacheNames.DIARY_CALENDAR, base.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(CacheNames.WORK_TYPES, base.entryTtl(Duration.ofHours(24)));
    }

    /**
     * 캐시 오류 내성 핸들러. 역직렬화 실패(포맷 불일치/깨진 엔트리)나 Redis 일시 장애를 캐시 오류로 처리하지 않고
     * 삼켜서: GET 오류 → 캐시 미스로 강등(+깨진 키 evict) → 대상 메서드 실행(DB 폴백). PUT/EVICT/CLEAR 오류도 무시.
     * → 캐시 문제가 사용자 요청 500 으로 번지지 않게 한다.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }

    static class ResilientCacheErrorHandler implements CacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache GET 실패 → 캐시 미스로 강등 + 깨진 키 evict: cache={}, key={}, err={}",
                    cache.getName(), key, exception.toString());
            try {
                cache.evict(key); // 깨진 엔트리 제거 → 다음 호출에서 정상 포맷으로 재적재(자가복구)
            } catch (RuntimeException evictEx) {
                log.warn("깨진 키 evict 실패(무시): cache={}, key={}, err={}", cache.getName(), key, evictEx.toString());
            }
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Cache PUT 실패(무시): cache={}, key={}, err={}", cache.getName(), key, exception.toString());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache EVICT 실패(무시): cache={}, key={}, err={}", cache.getName(), key, exception.toString());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Cache CLEAR 실패(무시): cache={}, err={}", cache.getName(), exception.toString());
        }
    }
}
