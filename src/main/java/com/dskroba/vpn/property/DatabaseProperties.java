package com.dskroba.vpn.property;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.database")
@Validated
public record DatabaseProperties(
        @NotBlank
        String principals,
        @NotBlank
        String principalStates,
        @NotBlank
        String principalHandlers,
        @NotBlank
        String path
) {
}

