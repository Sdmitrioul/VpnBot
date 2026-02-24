package com.dskroba.vpn.storage;

import com.dskroba.vpn.exception.InitializationException;
import com.dskroba.vpn.exception.RepositoryException;
import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PermanentStorage<K, V> implements KeyValueStorage<K, V>, AutoCloseable {
    private static final Logger log = LogManager.getLogger(PermanentStorage.class);

    private final File dbFile;
    private final ValueConverter<K> keyConverter;
    private final ValueConverter<V> valueConverter;
    private final RocksDB db;

    public PermanentStorage(File dbFile, ValueConverter<K> keyConverter, ValueConverter<V> valueConverter) {
        this.dbFile = dbFile;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        this.db = createStorage();
    }

    private RocksDB createStorage() {
        createParentPathIfMissing();
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            return RocksDB.open(options, dbFile.getAbsolutePath());
        } catch (RocksDBException e) {
            log.error("Unable to create storage", e);
            throw new InitializationException("Unable to create RocksDB instance", e);
        }
    }

    private void createParentPathIfMissing() {
        try {
            Files.createParentDirs(dbFile);
        } catch (IOException e) {
            log.error("Unable to create parent directory for DB, dir: {}", dbFile, e);
            throw new InitializationException("Unable to create parent directory for DB, dir: " + dbFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void save(K key, V value) {
        log.debug("Trying to put value with key: {}", key);
        try {
            db.put(keyConverter.convertToString(key).getBytes(StandardCharsets.UTF_8),
                    valueConverter.convertToString(value).getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            log.error("Error saving entry in RocksDB, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new RepositoryException("Error saving data", e);
        }
    }

    @Override
    public V find(K key) {
        log.debug("Trying to get value from DB under key {}", key);
        try {
            byte[] bytes = db.get(keyConverter.convertToString(key).getBytes(StandardCharsets.UTF_8));
            if (bytes == null) {
                return null;
            }
            return valueConverter.parse(new String(bytes));
        } catch (RocksDBException e) {
            log.error("Error getting entry in RocksDB, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new RepositoryException("Error getting data", e);
        }
    }

    @Override
    public void delete(K key) {
        log.info("Trying to delete value from DB under key {}", key);
        try {
            db.delete(keyConverter.convertToString(key).getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            log.error("Error deleting entry in RocksDB, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new RepositoryException("Error deleting entry.", e);
        }
    }

    @Override
    public List<V> values() {
        log.debug("Retrieving all values from DB");
        List<V> result = new ArrayList<>();
        try (RocksIterator iterator = db.newIterator()) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                String valueString = new String(iterator.value(), StandardCharsets.UTF_8);
                result.add(valueConverter.parse(valueString));
            }
            iterator.status();
        } catch (RocksDBException e) {
            log.error("Error iterating over RocksDB entries, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new RepositoryException("Error retrieving all values", e);
        }
        return result;
    }

    @Override
    public void close() {
        db.close();
    }
}
