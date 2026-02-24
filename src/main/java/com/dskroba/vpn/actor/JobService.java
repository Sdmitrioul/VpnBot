package com.dskroba.vpn.actor;

import com.dskroba.vpn.telegram.TelegramId;

public interface JobService {
    void submitJob(TelegramId userId, Job job);
}
