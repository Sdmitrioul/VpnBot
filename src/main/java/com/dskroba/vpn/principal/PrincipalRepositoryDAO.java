package com.dskroba.vpn.principal;

import com.dskroba.vpn.storage.KeyValueStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class PrincipalRepositoryDAO implements PrincipalRepository {
    private final KeyValueStorage<Long, Principal> storage;

    @Autowired
    public PrincipalRepositoryDAO(KeyValueStorage<Long, Principal> storage) {
        this.storage = storage;
    }

    @Override
    public Principal savePrincipal(Principal principal) {
        storage.save(principal.id(), principal);
        return principal;
    }

    @Override
    public Principal getByTelegramId(Long telegramId) {
        return storage.find(telegramId);
    }

    @Override
    public List<Principal> getPrincipals() {
        return storage.values();
    }

    @Override
    public void deletePrincipal(Long telegramId) {
        storage.delete(telegramId);
    }
}
