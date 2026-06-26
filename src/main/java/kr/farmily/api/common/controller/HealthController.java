package kr.farmily.api.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.farmily.api.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    private final String currentRegion;

    public HealthController(){
        String region;
        try {
            region = DefaultAwsRegionProviderChain.builder().build().getRegion().id();
        }catch(Exception e){
            region = "local-or-unknown";
        }
        this.currentRegion = region;
    }
    @GetMapping("/health")
    @Operation(summary = "헬스 체크")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP","region",currentRegion));
    }

    @GetMapping("/public/ping")
    @Operation(summary = "인증 우회 핑")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("pong");
    }
}
