package kr.farmily.api.auth.dto;

import java.util.List;

public record HandleCheckResponse(boolean available, List<String> suggestions) {}
