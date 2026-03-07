package com.dskroba.vpn.service;

import com.dskroba.vpn.exception.CustomException;
import com.dskroba.vpn.type.InterfaceConfig;
import com.dskroba.vpn.type.VpnKeys;
import com.dskroba.vpn.type.VpnServerConfiguration;
import com.dskroba.vpn.utils.VpnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.dskroba.vpn.utils.ExecUtils.exec;
import static com.dskroba.vpn.utils.VpnUtils.*;

public class VpnService {
    private static final Logger log = LogManager.getLogger(VpnService.class);

    private final Lock updateLock = new ReentrantLock();
    private final Map<String, InterfaceConfig> interfaceConfigsById;

    public VpnService(List<InterfaceConfig> interfaceConfigs) {
        this.interfaceConfigsById = interfaceConfigs.stream()
                .collect(Collectors.toMap(InterfaceConfig::interfaceId, c -> c));
        log.info("Vpn interfaces: {}", interfaceConfigsById.keySet());
    }

    public List<InterfaceConfig> getInterfaceConfigs() {
        return List.copyOf(interfaceConfigsById.values());
    }

    public void deleteUserConfig(String userConfiguration, String interfaceId) {
        InterfaceConfig iface = getInterfaceConfig(interfaceId);
        updateLock.lock();
        try {
            removeUserFromVpnConfigurationFile(userConfiguration, iface.configFilePath());
            reloadInterface(iface.serverConfiguration().vpnInterface());
        } finally {
            updateLock.unlock();
        }
    }

    public UserConfiguration createUserConfiguration(String configurationName, String interfaceId) {
        InterfaceConfig iface = getInterfaceConfig(interfaceId);
        VpnServerConfiguration serverConfiguration = iface.serverConfiguration();
        Path mainConfigurationFile = iface.configFilePath();
        updateLock.lock();
        try {
            VpnKeys keys = generateKeys();
            String mainConfiguration = new String(readFile(mainConfigurationFile)
                    .orElseThrow(() -> new CustomException("Configuration file must be present!")));
            String clientIp = getNextPeerIp(mainConfiguration, serverConfiguration.minClientIp())
                    .orElseThrow(() -> new CustomException("There is too much users!"));
            String clientConfigFile = VpnUtils.generateUserConfig(keys, clientIp, serverConfiguration);
            byte[] png = VpnUtils.generateQrPng(clientConfigFile);
            String newPeerBlock = VpnUtils.generatePeerBlock(keys, configurationName, clientIp);
            updateMainConfigurationFile(mainConfiguration + newPeerBlock, mainConfigurationFile);
            reloadInterface(serverConfiguration.vpnInterface());
            return new UserConfiguration(clientConfigFile.getBytes(StandardCharsets.UTF_8), png);
        } finally {
            updateLock.unlock();
        }
    }

    public InterfaceConfig getInterfaceConfig(String interfaceId) {
        InterfaceConfig config = interfaceConfigsById.get(interfaceId);
        if (config == null) {
            throw new CustomException("Unknown VPN interface: " + interfaceId);
        }
        return config;
    }

    private void reloadInterface(String vpnInterface) {
        exec("wg-quick down %s && wg-quick up %s".formatted(vpnInterface, vpnInterface));
    }

    private void removeUserFromVpnConfigurationFile(String userConfiguration, Path mainConfigurationFile) {
        var content = new String(readFile(mainConfigurationFile)
                .orElseThrow(() -> new CustomException("Configuration file must be present!")));
        updateMainConfigurationFile(removePeerBlock(content, userConfiguration), mainConfigurationFile);
    }

    private void updateMainConfigurationFile(String content, Path mainConfigurationFile) {
        try {
            Path tempFile = Files.createTempFile("wg-config", ".tmp");
            Files.writeString(tempFile, content);
            Files.move(tempFile, mainConfigurationFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.error("Error saving new file: {}", mainConfigurationFile, e);
            throw new CustomException("Error saving new file", e);
        }
    }

    private static Optional<byte[]> readFile(Path file) {
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (FileNotFoundException e) {
            log.error("Unable to find file: {}", file, e);
        } catch (IOException e) {
            log.error("IO exception reading: {}", file, e);
        }
        return Optional.empty();
    }

    public record UserConfiguration(byte[] file, byte[] image) {
    }
}
