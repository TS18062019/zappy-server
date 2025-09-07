package com.tsc.zappy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tsc.zappy.components.TransferMap;
import com.tsc.zappy.services.FileSenderService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class FileStreamController {

    private TransferMap mTransferMap;
    private FileSenderService senderService;

    @GetMapping("/{uuid}/{shouldTransfer}")
    public void handleTransferRequest(@PathVariable String uuid, @PathVariable boolean shouldTransfer) {
        if(shouldTransfer)
            senderService.send(uuid);
        else
            mTransferMap.remove(uuid);
    }
    
}
