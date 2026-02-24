package com.dskroba.vpn.base.bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractBean implements Bean {
    protected final Logger log = LogManager.getLogger(getClass());
    private volatile boolean isRunning;

    @PostConstruct
    @Override
    public final synchronized void start() {
        if (isRunning) {
            log.warn("Already started!");
            return;
        }

        log.info("Starting {}", this.getClass().getSimpleName());
        startImpl();
        isRunning = true;
        log.info("{} started!", this.getClass().getSimpleName());
    }

    @PreDestroy
    @Override
    public final synchronized void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        log.info("Stopping {}", this.getClass().getSimpleName());
        stopImpl();
        log.info("{} stopped!", this.getClass().getSimpleName());
    }

    public final boolean isRunning() {
        return isRunning;
    }

    public void startImpl() {
    }

    public void stopImpl() {
    }

    public void close() {
        stop();
    }
}
