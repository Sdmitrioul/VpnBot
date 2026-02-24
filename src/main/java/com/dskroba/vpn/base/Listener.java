package com.dskroba.vpn.base;

public interface Listener<T> {
    void receive(T item);
}
