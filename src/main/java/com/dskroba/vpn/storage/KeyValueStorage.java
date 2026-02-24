package com.dskroba.vpn.storage;

import java.util.List;

public interface KeyValueStorage<K, V> {
    void save(K key, V value);

    V find(K key);

    void delete(K key);

    List<V> values();
}