package com.dskroba.vpn.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VpnInterfaceProperties(
        @NotBlank
        String file,
        @NotBlank
        String minClientIp,
        @NotBlank
        String vpnInterface,
        @Min(1) @Max(400000)
        int vpnPort,
        String displayName
) {
    public String displayName() {
        return displayName != null && !displayName.isBlank() ? displayName : vpnInterface;
    }
}
