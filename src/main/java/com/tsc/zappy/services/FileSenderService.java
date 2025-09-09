package com.tsc.zappy.services;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.tsc.zappy.components.TransferMap;
import com.tsc.zappy.dto.FileTransferMetaData;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class FileSenderService {

    private final TransferMap map;
    private RestTemplate restTemplate = new RestTemplate();
    private Map<String, Boolean> activeTransfers = new ConcurrentHashMap<>();

    /**
     * Send the file set metadata to the receiver
     * Do not wait for confirmation here
     */
    public void notify(String url, Set<String> fileSet) {
        var response = restTemplate.postForEntity(url, fileSet, String.class);
        if(response.getStatusCode() == HttpStatus.OK)
            map.add(url, fileSet);
    }

    public void sendToClient(String uuid) {
        FileTransferMetaData ftm = map.get(uuid);
        if (ftm == null) {
            log.info("No match for given uuid {}", uuid);
            return;
        }
        if (activeTransfers.putIfAbsent(uuid, true) != null) {
            String msg = String.format("Rejecting duplicate transfer request for %s", uuid);
            log.info(msg);
            restTemplate.postForEntity(ftm.address(), msg, String.class);
            return;
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(ftm.fileSet().size());
        for (String path : ftm.fileSet()) {
            var fsr = new FileSystemResource(path);
            if (fsr.exists() && fsr.isReadable()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDisposition(ContentDisposition.attachment().filename(fsr.getFilename()).build());
                long contentLength = 0;
                try {
                    contentLength = fsr.contentLength();
                } catch (IOException e) {
                    log.error(e);
                }
                headers.setContentLength(contentLength);
                body.add("files", new HttpEntity<>(fsr, headers));
            }
        }
        var response = restTemplate.postForEntity(ftm.address(), body, String.class);
        if(response.getStatusCode() == HttpStatus.OK) {
            log.info("Delivered!");
        }
    }

    public String cancelBatch(String uuid) {
        if(!activeTransfers.containsKey(uuid)) {
            map.remove(uuid);
            return "Successfully removed!";
        }
        return "";
    }
}
