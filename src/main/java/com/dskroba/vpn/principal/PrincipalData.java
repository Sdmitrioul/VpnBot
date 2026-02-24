package com.dskroba.vpn.principal;

import com.dskroba.vpn.type.UserVpnConfiguration;

import java.util.List;

public record PrincipalData(
        List<UserVpnConfiguration> vpnConfigurations
) {
}
