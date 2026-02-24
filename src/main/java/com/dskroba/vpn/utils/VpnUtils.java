package com.dskroba.vpn.utils;

import com.dskroba.vpn.type.VpnKeys;
import com.dskroba.vpn.type.VpnServerConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dskroba.vpn.utils.ExecUtils.exec;
import static com.dskroba.vpn.utils.ExecUtils.execWithInput;

public final class VpnUtils {
    private static final Pattern ALLOWED_IPS_PATTERN =
            Pattern.compile("AllowedIPs\\s*=\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)");

    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        return (Long.parseLong(parts[0]) << 24)
                | (Long.parseLong(parts[1]) << 16)
                | (Long.parseLong(parts[2]) << 8)
                | Long.parseLong(parts[3]);
    }

    private static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public static Optional<String> getNextPeerIp(String content, String minApi) {
        Matcher matcher = ALLOWED_IPS_PATTERN.matcher(content);

        Set<Long> existedIps = new HashSet<>();
        long minIp = ipToLong(minApi);
        while (matcher.find()) {
            long current = ipToLong(matcher.group(1));
            existedIps.add(current);
            if (current < minIp) {
                minIp = current;
            }
        }

        for (long i = minIp + 1; i < Integer.MAX_VALUE; i++) {
            if (existedIps.contains(i)) {
                continue;
            }
            return Optional.of(longToIp(i));
        }
        return Optional.empty();
    }

    public static String removePeerBlock(String content, String name) {
        List<String> lines = Arrays.stream(content.split(System.lineSeparator())).toList();
        List<String> result = new ArrayList<>();

        boolean skip = false;
        String beginMarker = beginPeerMarker(name);
        String endMarker = endPeerMarker(name);

        for (String line : lines) {
            if (line.trim().equals(beginMarker)) {
                skip = true;
                continue;
            }
            if (line.trim().equals(endMarker)) {
                skip = false;
                continue;
            }
            if (!skip) {
                result.add(line);
            }
        }
        return result.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    public static VpnKeys generateKeys() {
        String clientPrivateKey = exec("wg genkey").trim();
        String clientPublicKey = execWithInput("wg pubkey", clientPrivateKey).trim();
        String presharedKey = exec("wg genpsk").trim();
        return new VpnKeys(clientPublicKey, clientPrivateKey, presharedKey);
    }

    public static String generatePeerBlock(VpnKeys keys, String clientConfigName, String clientIp) {
        return """
                
                %s
                [Peer]
                PublicKey = %s
                PresharedKey = %s
                AllowedIPs = %s/32
                %s
                """.formatted(
                beginPeerMarker(clientConfigName),
                keys.publicKey(), keys.presharedKey(), clientIp,
                endPeerMarker(clientConfigName));
    }

    public static String generateUserConfig(VpnKeys keys, String clientIp, VpnServerConfiguration vpnServerConfiguration) {
        return """
                [Interface]
                PrivateKey = %s
                Address = %s/24
                DNS = %s
                
                [Peer]
                PublicKey = %s
                PresharedKey = %s
                Endpoint = %s:%d
                AllowedIPs = 0.0.0.0/0
                PersistentKeepalive = 25
                """.formatted(
                keys.privateKey(),
                clientIp,
                vpnServerConfiguration.dnsServers(),
                vpnServerConfiguration.publicKey(),
                keys.presharedKey(),
                vpnServerConfiguration.publicIp(),
                vpnServerConfiguration.port()
        );
    }

    private static String beginPeerMarker(String clientConfigName) {
        return "# BEGIN_PEER " + clientConfigName;
    }

    private static String endPeerMarker(String clientConfigName) {
        return "# END_PEER " + clientConfigName;
    }

    private VpnUtils() {
    }
}
