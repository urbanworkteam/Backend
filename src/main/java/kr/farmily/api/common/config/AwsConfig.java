package kr.farmily.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(props.region() != null ? props.region() : "ap-northeast-2"));
        if (props.endpoint() != null && !props.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        applyCredentials(builder::credentialsProvider, props);
        return builder.build();
    }

    @Bean
    public S3Client s3Client(S3Properties props) {
        S3Client.Builder builder = S3Client.builder()
                .region(Region.of(props.region() != null ? props.region() : "ap-northeast-2"));
        if (props.endpoint() != null && !props.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        applyCredentials(builder::credentialsProvider, props);
        return builder.build();
    }

    @Bean
    public BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient(
            org.springframework.beans.factory.annotation.Value("${farmily.ai.aws-region:us-west-2}") String region) {
        return BedrockAgentRuntimeAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
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
