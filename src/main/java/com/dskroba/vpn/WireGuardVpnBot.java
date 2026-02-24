package com.dskroba.vpn;

import com.dskroba.vpn.utils.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.dskroba.vpn"
)
public class WireGuardVpnBot {
    public static void main(String[] args) {
        Configuration.loadGlobalProperties();
        SpringApplication.run(WireGuardVpnBot.class, args);
    }
}
