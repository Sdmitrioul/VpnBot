package com.dskroba.vpn.telegram.user;

import java.util.List;

public interface UserHandlersProvider {
    List<String> adminHandlers();
    List<String> userHandles();
}
