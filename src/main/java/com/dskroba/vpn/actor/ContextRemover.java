package com.dskroba.vpn.actor;

import com.dskroba.vpn.telegram.TelegramId;

public interface ContextRemover {
    void removeAllPrincipalContext(TelegramId id);
}
