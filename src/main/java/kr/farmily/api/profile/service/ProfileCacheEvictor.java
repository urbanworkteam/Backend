package kr.farmily.api.profile.service;

import kr.farmily.api.common.cache.CacheNames;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 프로필/블록/채널 변경 시 내 프로필(userId 키)·공개 프로필(handle 키) 두 캐시를 함께 무효화.
 * handle 은 SpEL 로 얻을 수 없어(쓰기 메서드 인자에 없음) UserRepository 로 조회해 프로그램적으로 evict.
 */
@Component
@RequiredArgsConstructor
public class ProfileCacheEvictor {

    private final CacheManager cacheManager;
    private final UserRepository userRepository;

    public void evict(long userId) {
        evict(CacheNames.MY_PROFILE, userId);
        userRepository.findById(userId)
                .map(User::getHandle)
                .filter(h -> h != null && !h.isBlank())
                .ifPresent(handle -> evict(CacheNames.PUBLIC_PROFILE, handle));
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.evict(key);
    }
}
