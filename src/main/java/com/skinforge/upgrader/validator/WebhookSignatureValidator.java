package com.skinforge.upgrader.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WebhookSignatureValidator {

    private final byte[] secretKey;

    public WebhookSignatureValidator(
            @Value("${cryptoproc.secret-key}") String secretKey
    ) {
        this.secretKey = secretKey.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValid(byte[] body, String signature) {

        if (signature == null ||
                !signature.matches("(?i)[0-9a-f]{64}")) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    secretKey,
                    "HmacSHA256"
            ));

            byte[] expected = mac.doFinal(body);
            byte[] actual = HexFormat.of().parseHex(signature);

            return MessageDigest.isEqual(expected, actual);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot verify CryptoProc webhook signature",
                    e
            );
        }
    }
}