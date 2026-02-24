package com.dskroba.vpn.actor;

public interface PrincipalActor extends AutoCloseable {
    boolean submit(Job job);
}
