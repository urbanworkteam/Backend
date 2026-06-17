package kr.farmily.api.common.upload;

import kr.farmily.api.common.config.S3Properties;
import kr.farmily.api.common.upload.dto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
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
        return new PresignResponse(url, key, toDisplayUrl(key), ttl.toSeconds());
    }

    /**
     * 업로드 버킷의 객체를 브라우저가 직접 받을 수 있는 presigned GET URL.
     * 버킷은 완전 비공개라 이 서명 URL로만 접근 가능하며 ttl 후 만료된다.
     */
    public String presignGet(String key, Duration ttl) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();
        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();
        return presigner.presignGetObject(req).url().toString();
    }

    /**
     * key -> 화면 표시용 URL 변환의 단일 출처.
     * cdn-base-url(farmily.s3.cdn-base-url)이 설정돼 있으면 버킷 전용 CloudFront(OAC)
     * 고정 URL "<cdnBaseUrl>/<key>" 를 반환한다. 비어 있으면(로컬 등) 기존 업로드 버킷
     * presigned GET(표시용 TTL) 으로 fallback 한다.
     */
    public String toDisplayUrl(String key) {
        if (key == null || key.isBlank()) return null;
        String cdn = props.cdnBaseUrl();
        if (cdn != null && !cdn.isBlank()) {
            return cdn.replaceAll("/+$", "") + "/" + key.replaceAll("^/+", "");
        }
        long ttl = props.displayTtlSeconds() != null ? props.displayTtlSeconds() : 3600L;
        return presignGet(key, Duration.ofSeconds(ttl));
    }
}
