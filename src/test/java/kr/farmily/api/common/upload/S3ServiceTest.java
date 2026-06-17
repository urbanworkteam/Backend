package kr.farmily.api.common.upload;

import kr.farmily.api.common.config.S3Properties;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ServiceTest {

    private S3Properties props(String cdnBaseUrl) {
        return new S3Properties(
                "farmily-s3-bucket", // bucket
                "ap-northeast-2",    // region
                null,                // endpoint
                null,                // accessKey
                null,                // secretKey
                cdnBaseUrl,          // cdnBaseUrl
                300L,                // presignTtlSeconds
                3600L                // displayTtlSeconds
        );
    }

    @Test
    void toDisplayUrl_returnsNull_whenKeyNullOrBlank() {
        S3Presigner presigner = mock(S3Presigner.class);
        S3Service service = new S3Service(presigner, props("https://d123.cloudfront.net"));

        assertThat(service.toDisplayUrl(null)).isNull();
        assertThat(service.toDisplayUrl("   ")).isNull();
        verify(presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void toDisplayUrl_usesCdnFixedUrl_whenCdnConfigured() {
        S3Presigner presigner = mock(S3Presigner.class);
        S3Service service = new S3Service(presigner, props("https://d123.cloudfront.net"));

        String url = service.toDisplayUrl("generated/1/76/card1.png");

        assertThat(url).isEqualTo("https://d123.cloudfront.net/generated/1/76/card1.png");
        // CDN 경로에서는 presign 을 절대 호출하지 않는다.
        verify(presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void toDisplayUrl_collapsesDuplicateSlashes_whenCdnConfigured() {
        S3Presigner presigner = mock(S3Presigner.class);
        S3Service service = new S3Service(presigner, props("https://d123.cloudfront.net/"));

        String url = service.toDisplayUrl("/users/1/avatar/me.png");

        assertThat(url).isEqualTo("https://d123.cloudfront.net/users/1/avatar/me.png");
    }

    @Test
    void toDisplayUrl_fallsBackToPresignedGet_whenCdnBlank() throws Exception {
        S3Presigner presigner = mock(S3Presigner.class);
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(URI.create("https://farmily-s3-bucket.s3.amazonaws.com/generated/1/76/card1.png?X-Amz-Signature=abc").toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        S3Service service = new S3Service(presigner, props(""));

        String url = service.toDisplayUrl("generated/1/76/card1.png");

        assertThat(url).contains("X-Amz-Signature=abc");
        verify(presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
