package com.dskroba.vpn.property;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix = "application.vpn")
@Validated
public record VpnProperties(
        @NotBlank
        String publicKey,
        @NotBlank
        String publicIp,
        @NotBlank
        String dnsServers,
        Map<String, VpnInterfaceProperties> interfaces
) {
    public Map<String, VpnInterfaceProperties> interfaces() {
        return interfaces != null ? interfaces : Map.of();
    }
}
