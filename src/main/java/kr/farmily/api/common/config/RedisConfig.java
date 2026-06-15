package kr.farmily.api.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import kr.farmily.api.common.cache.CacheNames;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
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
 * </ul>
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisMapper = objectMapper.copy();
        // 게터/세터가 없는 엔티티(@Getter only)도 필드 직접 접근으로 직렬화/역직렬화
        redisMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        redisMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 역직렬화 시 구체 타입 복원을 위해 @class 타입정보 포함 (내부 신뢰 데이터)
        redisMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .computePrefixWith(cacheName -> cacheName + "::")
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
}
