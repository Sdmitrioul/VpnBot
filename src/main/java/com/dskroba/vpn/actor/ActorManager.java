package com.dskroba.vpn.actor;

import com.dskroba.vpn.principal.Principal;

public interface ActorManager extends AutoCloseable {
    PrincipalActor getActor(Principal principal);

    void removeActor(Principal principal);
}
