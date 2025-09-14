package com.tsc.zappy.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tsc.zappy.components.HardwareInfo;
import com.tsc.zappy.components.HmacUtil;
import com.tsc.zappy.constants.Constants;

import lombok.RequiredArgsConstructor;

@CrossOrigin(originPatterns = "http://192.168.0.*:[5173]")
@RestController
@RequestMapping("/zappy")
@RequiredArgsConstructor
public class LanController {
    
    private final HardwareInfo info;
    private final HmacUtil util;

    @GetMapping
    public Map<String, String> sendCredentials(@RequestParam String deviceId) {
        return Map.of(
            Constants.DEVICE_ID, deviceId,
            Constants.SIGN, util.sign(deviceId),
            "serverId", info.getDeviceId(),
            "serverIp", info.getServerIp(),
            "name", "server"
        );
    }
}
