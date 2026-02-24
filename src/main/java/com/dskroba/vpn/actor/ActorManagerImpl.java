package com.dskroba.vpn.actor;

import com.dskroba.vpn.exception.AuthorizationException;
import com.dskroba.vpn.exception.CustomException;
import com.dskroba.vpn.principal.Principal;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ActorManagerImpl implements ActorManager {
    private final Map<Long, PrincipalActor> actors = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final ContextProvider contextProvider;

    public ActorManagerImpl(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public PrincipalActor getActor(Principal principal) throws AuthorizationException {
        Long id = principal.key().telegramId();
        PrincipalActor actor = actors.get(id);
        if (actor == null) {
            actor = creatActor(principal);
        }
        return actor;
    }

    @Override
    public void removeActor(Principal principal) {
        lock.lock();
        try {
            actors.remove(principal.id()).close();
        } catch (Exception e) {
            throw new CustomException("Exception during actor closure.", e);
        } finally {
            lock.unlock();
        }
    }

    private PrincipalActor creatActor(Principal principal) {
        lock.lock();
        try {
            Long id = principal.key().telegramId();
            PrincipalActor actor = actors.get(id);
            if (actor == null) {
                actor = new PrincipalActorImpl(contextProvider.getContext(principal));
                actors.put(id, actor);
            }
            return actor;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        for (PrincipalActor actor : actors.values()) {
            actor.close();
        }
        actors.clear();
    }
}
