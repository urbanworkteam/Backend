package kr.farmily.api.common.upload;

import kr.farmily.api.common.config.S3Properties;
import kr.farmily.api.common.upload.dto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;
    private final S3Properties props;

    public PresignResponse presignPut(String key, String contentType, Duration ttl) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(put)
                .build();
        String url = presigner.presignPutObject(req).url().toString();
        String publicUrl = (props.cdnBaseUrl() != null && !props.cdnBaseUrl().isBlank())
                ? props.cdnBaseUrl().replaceAll("/$", "") + "/" + key
                : url;
        return new PresignResponse(url, key, publicUrl, ttl.toSeconds());
    }
}
