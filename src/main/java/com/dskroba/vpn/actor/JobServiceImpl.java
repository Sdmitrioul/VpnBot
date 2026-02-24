package com.dskroba.vpn.actor;

import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.telegram.TelegramId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class JobServiceImpl implements JobService {
    private final ActorManager actorManager;
    private final PrincipalService principalService;

    @Autowired
    public JobServiceImpl(ActorManager actorManager, PrincipalService principalService) {
        this.actorManager = actorManager;
        this.principalService = principalService;
    }

    @Override
    public void submitJob(TelegramId userId, Job job) {
        Principal principal = principalService.loadPrincipal(userId);
        actorManager.getActor(principal).submit(job);
    }
}
