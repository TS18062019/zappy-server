package com.tsc.zappy.dto;

import java.util.Set;

public record FileTransferMetaData(
    String address,
    Set<String> fileSet
) {
}
