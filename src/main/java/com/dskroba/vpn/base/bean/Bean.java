package com.dskroba.vpn.base.bean;

import org.springframework.context.SmartLifecycle;

public interface Bean extends SmartLifecycle {
    void start();

    void stop();

    default void close() {
        stop();
    }
}
