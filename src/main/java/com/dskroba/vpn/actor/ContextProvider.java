package com.dskroba.vpn.actor;

import com.dskroba.vpn.principal.Principal;

public interface ContextProvider {
    Context getContext(Principal principal);
}
