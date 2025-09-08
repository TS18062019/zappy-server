package com.tsc.zappy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tsc.zappy.services.FileSenderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileStreamController {

    private final FileSenderService senderService;
    
    @GetMapping("/cancelBatch/{uuid}")
    public ResponseEntity<String> cancel(@PathVariable String uuid) {
        return ResponseEntity.ok().body(senderService.cancelBatch(uuid));
    }

    @GetMapping("/acceptBatch/{uuid}")
    public void accept(@PathVariable String uuid) {
        senderService.sendToClient(uuid);
    }
    
}
