package kr.farmily.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @Size(max = 30) String name,
        @Email @Size(max = 100) String email
) {}
