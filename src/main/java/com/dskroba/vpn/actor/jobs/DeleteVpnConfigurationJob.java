package com.dskroba.vpn.actor.jobs;

import com.dskroba.vpn.actor.Job;
import com.dskroba.vpn.actor.JobException;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.utils.VpnPrincipalConfigurationEncoder;

public final class DeleteVpnConfigurationJob implements Job {
    private final String vpnConfigurationName;
    private final String interfaceId;
    private final VpnService vpnService;

    public DeleteVpnConfigurationJob(String vpnConfigurationName, String interfaceId, VpnService vpnService) {
        this.vpnConfigurationName = vpnConfigurationName;
        this.interfaceId = interfaceId;
        this.vpnService = vpnService;
    }

    @Override
    public void run() throws JobException {
        if (interfaceId == null || interfaceId.isEmpty()) {
            throw new JobException("Interface id is required for delete");
        }
        ContextAccessor.context().setPrincipal(ContextAccessor.principal().toBuilder()
                .removeVpnConfiguration(vpnConfigurationName, interfaceId).build());
        vpnService.deleteUserConfig(
                VpnPrincipalConfigurationEncoder.configurationName(ContextAccessor.principal().key(), vpnConfigurationName),
                interfaceId);
    }
}
