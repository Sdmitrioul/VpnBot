package com.dskroba.vpn.telegram.user;

import com.dskroba.vpn.base.Listener;
import com.dskroba.vpn.storage.KeyValueStorage;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class AllowedUsersStorage implements UserHandlersProvider, UserHandlerManager {
    private static final Logger log = LogManager.getLogger(AllowedUsersStorage.class);

    private static final Long USER_HANDLERS_KEY = -239L;

    private final List<String> adminUsers;
    private final Set<String> userHandlers = new HashSet<>();
    private final Lock readLock;
    private final Lock writeLock;
    private final KeyValueStorage<Long, List<String>> userHandlersStorage;

    @Autowired
    public AllowedUsersStorage(KeyValueStorage<Long, List<String>> userHandlersStorage,
                               Set<String> adminUsers,
                               Listener<String> removeUserListener) {
        this.userHandlersStorage = userHandlersStorage;
        this.adminUsers = List.copyOf(adminUsers);
        var lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    @PostConstruct
    private void loadUserHandlers() {
        List<String> storedUserHandlers = userHandlersStorage.find(USER_HANDLERS_KEY);
        if (storedUserHandlers == null) {
            log.info("No saved user handlers");
            return;
        }
        log.info("Stored handlers: {}", storedUserHandlers);
        userHandlers.addAll(storedUserHandlers);
    }

    @Override
    public List<String> adminHandlers() {
        return adminUsers;
    }

    @Override
    public List<String> userHandles() {
        List<String> handlers;
        readLock.lock();
        try {
            handlers = List.copyOf(userHandlers);
        } finally {
            readLock.unlock();
        }
        return handlers;
    }

    @Override
    public void addUser(String handler) {
        boolean changed;
        writeLock.lock();
        try {
            if (adminUsers.contains(handler)) {
                changed = false;
            } else {
                changed = userHandlers.add(handler);
            }
        } finally {
            writeLock.unlock();
        }
        if (changed) {
            flushUsers();
        }
    }

    @Override
    public void removeUser(String handler) {
        boolean changed;
        writeLock.lock();
        try {
            changed = userHandlers.remove(handler);
        } finally {
            writeLock.unlock();
        }
        if (changed) {
            flushUsers();
            removeUser(handler);
        }
    }

    private void flushUsers() {
        readLock.lock();
        try {
            userHandlersStorage.save(USER_HANDLERS_KEY, List.copyOf(userHandlers));
        } finally {
            readLock.unlock();
        }
    }
}
