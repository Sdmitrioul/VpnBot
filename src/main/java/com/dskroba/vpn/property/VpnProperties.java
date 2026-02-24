package com.dskroba.vpn.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.vpn")
@Validated
public record VpnProperties(
        @NotBlank
        String file,
        @NotBlank
        String minClientIp,
        @NotBlank
        String vpnInterface,
        @NotBlank
        String publicIp,
        @Min(1) @Max(400000)
        int vpnPort,
        @NotBlank
        String publicKey,
        @NotBlank
        String dnsServers
) {
}
