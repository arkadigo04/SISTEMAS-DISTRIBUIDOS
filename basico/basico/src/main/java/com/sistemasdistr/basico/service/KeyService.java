package com.sistemasdistr.basico.service;

import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeyService {

    public Map<String, String> generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();

        String publicKey = Base64.getEncoder()
                .encodeToString(pair.getPublic().getEncoded());

        String privateKey = Base64.getEncoder()
                .encodeToString(pair.getPrivate().getEncoded());

        Map<String, String> keys = new HashMap<>();
        keys.put("public", publicKey);
        keys.put("private", privateKey);

        return keys;
    }
}