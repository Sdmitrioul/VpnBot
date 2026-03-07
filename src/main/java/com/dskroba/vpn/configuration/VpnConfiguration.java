package com.dskroba.vpn.configuration;

import com.dskroba.vpn.base.Listener;
import com.dskroba.vpn.property.VpnInterfaceProperties;
import com.dskroba.vpn.property.VpnProperties;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.type.InterfaceConfig;
import com.dskroba.vpn.type.VpnServerConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({VpnProperties.class})
public class VpnConfiguration {

    @Bean
    public List<InterfaceConfig> interfaceConfigs(VpnProperties properties) {
        Map<String, VpnInterfaceProperties> map = properties.interfaces();
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    String interfaceName = entry.getKey();
                    VpnInterfaceProperties p = entry.getValue();
                    VpnServerConfiguration server = new VpnServerConfiguration(
                            properties.publicIp(), p.vpnPort(), properties.publicKey(), properties.dnsServers(),
                            p.vpnInterface(), p.minClientIp());
                    return new InterfaceConfig(
                            interfaceName,
                            p.displayName(),
                            Paths.get(p.file()),
                            server);
                })
                .collect(Collectors.toList());
    }

    @Bean
    public VpnService vpnService(List<InterfaceConfig> interfaceConfigs) {
        return new VpnService(interfaceConfigs);
    }

    @Bean
    public Listener<String> removeUserListener() {
        return _ -> {
        };
    }
}
