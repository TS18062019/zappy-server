package com.tsc.zappy.components;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tsc.zappy.dto.DatagramFormat;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class HmacUtil {

    @Value("${secret.key}")
    private String secretKey;

    private static final String HMAC_ALGO = "HmacSHA256";

    public String getNonce() {
        return System.currentTimeMillis() + "-" + Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    private String sign(String message) {
        try {
            Mac m = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec spec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGO);
            m.init(spec);
            byte[] b = m.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(b);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to sign the message", e);
        }
        return "";
    }

    public void signDatagram(DatagramFormat dgf) {
        String signature = sign(dgf.getNonce()+"|"+dgf.getAddress());
        dgf.setSignature(signature);
    }

    public boolean verifyDatagram(DatagramFormat dgf) {
        return verify(dgf.getNonce(), dgf.getAddress(), dgf.getSignature());
    }

    private boolean verify(String nonce, String addr, String expectedSignature) {
        if(expectedSignature == null)
            return false;
        return expectedSignature.equals(sign(nonce+"|"+addr));
    }

    public boolean verify(String deviceId, String signature) {
        String calculatedSignature = sign(deviceId);
        return signature.equals(calculatedSignature);
    }
}
