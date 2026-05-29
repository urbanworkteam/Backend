package kr.farmily.api.common.upload;

import kr.farmily.api.common.config.S3Properties;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.dto.PresignRequest;
import kr.farmily.api.common.upload.dto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignService {

    private final S3Service s3Service;
    private final S3Properties s3Properties;

    public PresignResponse presign(long userId, PresignRequest req) {
        String ext = req.ext().toLowerCase();
        if (!UploadKind.ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "허용되지 않은 확장자입니다", "ext");
        }
        if (req.sizeBytes() > req.kind().maxBytes) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "최대 사이즈 초과 (" + req.kind().maxBytes + " bytes)", "sizeBytes");
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String key = "users/" + userId + "/" + req.kind().prefix + "/" + datePath + "/"
                + UUID.randomUUID() + "." + ext;
        Duration ttl = Duration.ofSeconds(
                s3Properties.presignTtlSeconds() != null ? s3Properties.presignTtlSeconds() : 300L);
        return s3Service.presignPut(key, UploadKind.mimeOf(ext), ttl);
    }
}
