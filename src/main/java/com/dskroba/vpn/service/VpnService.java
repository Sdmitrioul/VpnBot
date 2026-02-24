package com.dskroba.vpn.service;

import com.dskroba.vpn.exception.CustomException;
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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.dskroba.vpn.utils.ExecUtils.exec;
import static com.dskroba.vpn.utils.VpnUtils.*;

public class VpnService {
    private static final Logger log = LogManager.getLogger(VpnService.class);

    private final Lock updateLock = new ReentrantLock();
    private final Path mainConfigurationFile;
    private final VpnServerConfiguration serverConfiguration;

    public VpnService(String mainConfigurationFile, VpnServerConfiguration serverConfiguration) {
        this.mainConfigurationFile = Paths.get(mainConfigurationFile);
        this.serverConfiguration = serverConfiguration;
    }

    public void deleteUserConfig(String userConfiguration) {
        updateLock.lock();
        try {
            removeUserFromVpnConfigurationFile(userConfiguration);
            reloadClients();
        } finally {
            updateLock.unlock();
        }
    }

    public byte[] createUserConfiguration(String configurationName) {
        updateLock.lock();
        try {
            VpnKeys keys = generateKeys();
            String mainConfiguration = new String(readFile(mainConfigurationFile)
                    .orElseThrow(() -> new CustomException("Configuration file must be present!")));
            String clientIp = getNextPeerIp(mainConfiguration, serverConfiguration.minClientIp())
                    .orElseThrow(() -> new CustomException("There is too much users!"));
            String clientConfigFile = VpnUtils.generateUserConfig(keys, clientIp, serverConfiguration);
            String newPeerBlock = VpnUtils.generatePeerBlock(keys, configurationName, clientIp);
            updateMainConfigurationFile(mainConfiguration + newPeerBlock);
            reloadClients();
            return clientConfigFile.getBytes(StandardCharsets.UTF_8);
        } finally {
            updateLock.unlock();
        }
    }

    private void reloadClients() {
        if (System.getenv("PROD") != null) {
            exec("wg-quick strip %s | wg syncconf %s /dev/stdin"
                    .formatted(serverConfiguration.vpnInterface(), serverConfiguration.vpnInterface()));
        }
    }

    private void removeUserFromVpnConfigurationFile(String userConfiguration) {
        var content = mainConfigurationFileContent();
        updateMainConfigurationFile(removePeerBlock(content, userConfiguration));
    }

    private void updateMainConfigurationFile(String content) {
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

    private String mainConfigurationFileContent() {
        return new String(readFile(mainConfigurationFile)
                .orElseThrow(() -> new CustomException("Configuration file must be present!")));
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
}
