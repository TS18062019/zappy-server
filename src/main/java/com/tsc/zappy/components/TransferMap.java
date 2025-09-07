package com.tsc.zappy.components;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

import com.tsc.zappy.dto.FileTransferMetaData;

/**
 * Maintains a map of files with an unique identifier.
 * Kept until the receiving server has not yet responded, 
 * i.e, until the user on the receiving end has confirmed or cancelled it
 */

@Component
public class TransferMap {
    
    private Map<String, FileTransferMetaData> tMap;

    public TransferMap() {
        tMap = new ConcurrentHashMap<>();
    }

    public void add(String destination, Set<String> fileSet) {
        String key = UUID.randomUUID().toString();
        tMap.put(key, new FileTransferMetaData(destination, fileSet));
    }

    public FileTransferMetaData get(String key) {
        return tMap.getOrDefault(key, null);
    }

    /**
     * Remove after the transfer is successful
     */
    public void remove(String key) {
        tMap.remove(key);
    }
}
