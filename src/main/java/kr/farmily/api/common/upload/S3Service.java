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
        // Content-Type 은 서명에서 제외 (SignedHeaders=host 만).
        // 클라이언트(브라우저) 가 보내는 실제 MIME 과 presign 시 추정한 MIME 이 어긋나
        // SignatureDoesNotMatch 가 나는 흔한 함정을 피하기 위함. 확장자는 key path 에서
        // 이미 결정되어 있어 MIME 임의 변경의 의미가 제한적.
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
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
