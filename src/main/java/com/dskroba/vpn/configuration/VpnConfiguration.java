package com.dskroba.vpn.configuration;

import com.dskroba.vpn.base.Listener;
import com.dskroba.vpn.property.VpnProperties;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.type.VpnServerConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({VpnProperties.class})
public class VpnConfiguration {
    @Bean
    public VpnService vpnService(VpnProperties properties, VpnServerConfiguration serverConfiguration) {
        return new VpnService(properties.file(), serverConfiguration);
    }

    @Bean
    public VpnServerConfiguration vpnServerConfiguration(VpnProperties properties) {
        return new VpnServerConfiguration(properties.publicIp(), properties.vpnPort(), properties.publicKey(),
                properties.dnsServers(), properties.vpnInterface(), properties.minClientIp());
    }

    @Bean
    public Listener<String> removeUserListener() {
        return _ -> {
        };
    }
}
