package com.dskroba.vpn.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "telegram.bot")
@Validated
public record TelegramProperties(
        @NotBlank
        String token,
        @NotBlank
        String adminUsers,
        @Min(1) @Max(10)
        int threadsCount
) {
}
