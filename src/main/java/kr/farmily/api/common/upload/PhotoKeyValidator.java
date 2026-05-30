package kr.farmily.api.common.upload;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhotoKeyValidator {

    /** S3 key 가 본인 prefix 인지 검증. 형식: users/{userId}/{UploadKind.prefix}/... */
    public void requireAllOwnedBy(long userId, List<String> keys) {
        if (keys == null) return;
        String userPrefix = "users/" + userId + "/";
        for (String key : keys) {
            if (key == null || !key.startsWith(userPrefix)) {
                throw new BusinessException(ErrorCode.NOT_RESOURCE_OWNER,
                        "본인 소유의 업로드 키가 아닙니다", "photoKeys");
            }
            String rest = key.substring(userPrefix.length());
            boolean matched = false;
            for (UploadKind kind : UploadKind.values()) {
                if (rest.startsWith(kind.prefix + "/")) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "허용되지 않은 업로드 kind: " + rest.split("/", 2)[0], "photoKeys");
            }
        }
    }
}
