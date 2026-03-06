package com.dskroba.vpn.utils;

import com.dskroba.vpn.exception.CustomException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public final class ExecUtils {
    private static final Logger log = LogManager.getLogger(ExecUtils.class);
    private static final AtomicLong value = new AtomicLong(0);

    public static String exec(String command) {
        return exec(command, null);
    }

    private static String exec(String command, String stdin) {
        long id = value.getAndIncrement();
        log.info("Command id-{} to execute {}", id, command);
        try {
            Process p = new ProcessBuilder("/bin/bash", "-c", command)
                    .redirectErrorStream(true).start();
            if (stdin != null) {
                p.getOutputStream().write(stdin.getBytes());
                p.getOutputStream().close();
            }
            String output = new String(p.getInputStream().readAllBytes());
            log.info("Command id-{} output {}", id, output);
            p.waitFor();
            return output;
        } catch (Exception e) {
            log.error("Exception during command id-{} execution: {}", id, command, e);
            throw new CustomException("Command failed: " + command, e);
        }
    }

    public static String execWithInput(String command, String input) {
        return exec(command, input);
    }

    private ExecUtils() {
    }
}
