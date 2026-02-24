package com.dskroba.vpn.principal;

import com.dskroba.vpn.telegram.TelegramId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrincipalServiceImpl implements PrincipalService {
    private final PrincipalRepository principalRepository;

    @Autowired
    public PrincipalServiceImpl(PrincipalRepository principalRepository) {
        this.principalRepository = principalRepository;
    }

    @Override
    public Principal authenticate(TelegramId issuer) {
        Principal savedPrincipal = principalRepository.getByTelegramId(issuer.telegramId());
        if (savedPrincipal == null) {
            Principal newPrincipal = Principal.builder(issuer)
                    //TODO: context
                    .build();
            return savePrincipal(newPrincipal);
        }
        if (issuer.hasSameMetadata(savedPrincipal.key())) {
            Principal updatedPrincipal = savedPrincipal.toBuilder()
                    .telegramId(issuer)
                    .build();
            return savePrincipal(updatedPrincipal);
        }
        return savedPrincipal;
    }

    @Override
    public Principal loadPrincipal(TelegramId id) {
        if (id.isResolved()) {
            return principalRepository.getByTelegramId(id.telegramId());
        }
        return principalRepository.getPrincipals().stream()
                .filter(principal -> principal.key().telegramHandle().equals(id.telegramHandle()))
                .findAny()
                .orElse(null);
    }

    @Override
    public Principal savePrincipal(Principal principal) {
        return principalRepository.savePrincipal(principal);
    }

    @Override
    public List<Principal> getAllPrincipals() {
        return principalRepository.getPrincipals();
    }

    @Override
    public void deletePrincipalData(TelegramId id) {
        if (id.isResolved()) {
            principalRepository.deletePrincipal(id.telegramId());
        }
    }
}