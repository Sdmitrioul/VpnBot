package com.dskroba.vpn.principal;

import com.dskroba.vpn.telegram.TelegramId;

import java.util.List;

public interface PrincipalService {
    Principal savePrincipal(Principal principal);

    Principal authenticate(TelegramId issuer);

    Principal loadPrincipal(TelegramId id);

    List<Principal> getAllPrincipals();

    void deletePrincipalData(TelegramId id);
}
