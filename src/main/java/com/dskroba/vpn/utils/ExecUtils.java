package com.dskroba.vpn.utils;

import com.dskroba.vpn.exception.CustomException;

public final class ExecUtils {
    public static String exec(String command) {
        return exec(command, null);
    }

    private static String exec(String command, String stdin) {
        try {
            Process p = new ProcessBuilder("/bin/bash", "-c", command)
                    .redirectErrorStream(true).start();
            if (stdin != null) {
                p.getOutputStream().write(stdin.getBytes());
                p.getOutputStream().close();
            }
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            return output;
        } catch (Exception e) {
            throw new CustomException("Command failed: " + command, e);
        }
    }

    public static String execWithInput(String command, String input) {
        return exec(command, input);
    }

    private ExecUtils() {
    }
}
