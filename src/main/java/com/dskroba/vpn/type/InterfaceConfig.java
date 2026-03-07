package com.dskroba.vpn.type;

import java.nio.file.Path;

public record InterfaceConfig(String interfaceId, String displayName, Path configFilePath, VpnServerConfiguration serverConfiguration) {
}
