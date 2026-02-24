package com.dskroba.vpn.actor.jobs;

import com.dskroba.vpn.actor.ContextRemover;
import com.dskroba.vpn.actor.Job;
import com.dskroba.vpn.actor.JobException;
import com.dskroba.vpn.statemachine.state.ContextAccessor;

public class RemoveUserJob implements Job {
    private final ContextRemover contextRemover;

    public RemoveUserJob(ContextRemover contextRemover) {
        this.contextRemover = contextRemover;
    }

    @Override
    public void run() throws JobException {
        contextRemover.removeAllPrincipalContext(ContextAccessor.principal().key());
    }
}
