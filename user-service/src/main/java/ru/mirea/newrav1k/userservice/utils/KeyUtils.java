package ru.mirea.newrav1k.userservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyUtils {

    public static PrivateKey loadPrivateKey(String filename) throws IOException, NoSuchAlgorithmException {
        try (InputStream inputStream = new ClassPathResource(filename).getInputStream()) {
            String keyContent = new String(FileCopyUtils.copyToByteArray(inputStream));

            String privateKeyPEM = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            try {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception exception) {
                throw new RuntimeException("Failed to load private key from: " + filename, exception);
            }
        }
    }

    public static PublicKey loadPublicKey(String filename) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (InputStream inputStream = new ClassPathResource(filename).getInputStream()) {
            String keyContent = new String(FileCopyUtils.copyToByteArray(inputStream));

            String publicKeyPEM = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);

            return keyFactory.generatePublic(keySpec);
        }
    }

}