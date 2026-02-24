package com.dskroba.vpn.principal;

import java.util.List;

public interface PrincipalRepository {
    Principal savePrincipal(Principal principal);

    Principal getByTelegramId(Long telegramId);

    List<Principal> getPrincipals();

    void deletePrincipal(Long telegramId);
}