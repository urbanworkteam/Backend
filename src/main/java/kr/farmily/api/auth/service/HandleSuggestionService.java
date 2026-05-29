package kr.farmily.api.auth.service;

import kr.farmily.api.common.util.HandleValidator;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HandleSuggestionService {

    private static final Set<String> RESERVED = Set.of(
            "admin", "me", "api", "public", "auth", "login", "signup", "farmily", "support", "static", "assets"
    );
    private static final SecureRandom RNG = new SecureRandom();

    private final UserRepository userRepository;

    public boolean isAvailable(String handle) {
        if (!HandleValidator.isValid(handle)) return false;
        if (RESERVED.contains(handle)) return false;
        return userRepository.findActiveByHandle(handle).isEmpty();
    }

    public List<String> suggest(String seed) {
        String base = sanitize(seed);
        if (base.length() < 3) base = "farmer-" + randomSuffix(4);

        List<String> candidates = new ArrayList<>();
        candidates.add(base);
        candidates.add(base + "-farm");
        candidates.add(base + "-2");
        candidates.add(base + "-3");
        candidates.add(base + "-" + randomSuffix(3));
        candidates.add(base + "-" + randomSuffix(4));

        return candidates.stream()
                .filter(HandleValidator::isValid)
                .filter(this::isAvailable)
                .distinct()
                .limit(5)
                .toList();
    }

    /** ASCII 만 남기고 소문자화 + hyphen 정리. 한글 등 다른 문자는 단순 제거(간이 구현). */
    private static String sanitize(String input) {
        if (input == null) return "";
        String s = input.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");
        if (s.length() > 25) s = s.substring(0, 25);
        return s;
    }

    private static String randomSuffix(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int r = RNG.nextInt(36);
            sb.append((char) (r < 10 ? '0' + r : 'a' + r - 10));
        }
        return sb.toString();
    }
}
