package kr.farmily.api.common.upload;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PhotoKeyValidator {

    private static final Set<String> ALLOWED_KINDS = Set.of(
            "diary", "profile_bg", "profile_avatar", "story", "content_extra"
    );

    /** S3 key 가 본인 prefix 인지 검증. 형식: users/{userId}/{kind}/... */
    public void requireAllOwnedBy(long userId, List<String> keys) {
        if (keys == null) return;
        String prefix = "users/" + userId + "/";
        for (String key : keys) {
            if (key == null || !key.startsWith(prefix)) {
                throw new BusinessException(ErrorCode.NOT_RESOURCE_OWNER,
                        "본인 소유의 업로드 키가 아닙니다", "photoKeys");
            }
            String[] parts = key.substring(prefix.length()).split("/", 2);
            if (parts.length < 2 || !ALLOWED_KINDS.contains(parts[0])) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "허용되지 않은 업로드 kind: " + (parts.length > 0 ? parts[0] : "<empty>"), "photoKeys");
            }
        }
    }
}
