package com.dskroba.vpn.type;

public record VpnServerConfiguration(String publicIp, int port, String publicKey, String dnsServers, String vpnInterface, String minClientIp) {
}
