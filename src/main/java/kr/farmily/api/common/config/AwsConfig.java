package kr.farmily.api.common.config;

import kr.farmily.api.ai.config.AiProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;

@Configuration
public class AwsConfig {

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        boolean useCustomEndpoint = props.endpoint() != null && !props.endpoint().isBlank();
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(props.region() != null ? props.region() : "ap-northeast-2"));
        if (useCustomEndpoint) {
            builder.endpointOverride(URI.create(props.endpoint()));
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        applyCredentials(builder::credentialsProvider, props);
        return builder.build();
    }

    @Bean
    public S3Client s3Client(S3Properties props) {
        boolean useCustomEndpoint = props.endpoint() != null && !props.endpoint().isBlank();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.region() != null ? props.region() : "ap-northeast-2"));
        if (useCustomEndpoint) {
            builder.endpointOverride(URI.create(props.endpoint()));
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        applyCredentials(builder::credentialsProvider, props);
        return builder.build();
    }

    @Bean
    public BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient(
            @Value("${farmily.ai.aws-region:us-west-2}") String region) {
        return BedrockAgentRuntimeAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public WebClient agentCoreWebClient(AiProperties props) {
        // AgentCore는 CloudMap(farmily-agentcore.farmily.local)으로 연결되며 재배포 시 task IP가 바뀐다.
        // 기본 커넥션 풀은 keep-alive 연결을 오래 보관해, 옛 IP로의 죽은 연결을 재사용하다
        // "connection already closed"로 실패한다. idle/수명을 짧게 잡고 백그라운드 eviction을 켜서
        // 죽은 연결을 자동 폐기 → 재배포 후 백엔드 재시작 없이 새 IP로 자동 재연결되도록 한다.
        ConnectionProvider provider = ConnectionProvider.builder("agentcore")
                .maxIdleTime(Duration.ofSeconds(30))       // 30초 이상 idle 연결 폐기
                .maxLifeTime(Duration.ofSeconds(60))       // 연결 최대 수명 — 주기적 재생성으로 DNS 재해석 유도
                .evictInBackground(Duration.ofSeconds(30)) // 백그라운드에서 만료 연결 정리
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(props.invokeTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(props.agentcoreUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @FunctionalInterface
    private interface CredsApplier {
        void apply(software.amazon.awssdk.auth.credentials.AwsCredentialsProvider provider);
    }

    private void applyCredentials(CredsApplier applier, S3Properties props) {
        if (props.accessKey() != null && !props.accessKey().isBlank()
                && props.secretKey() != null && !props.secretKey().isBlank()) {
            applier.apply(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKey(), props.secretKey())));
        } else {
            applier.apply(DefaultCredentialsProvider.create());
        }
    }
}
