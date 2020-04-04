package hr.fer.zemris.nos.lab2;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.MessageDigest;

public class Crypto {
    private Cipher cipher;
    private Keys keys;

    public Crypto(Cipher cipher, Keys keys) {
        this.cipher = cipher;
        this.keys = keys;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public Keys getKeys() {
        return keys;
    }

    public static class Keys {
        Key privateKey;
        Key publicKey;
        Key secretKey;

        public Keys(Key privateKey, Key publicKey, Key secretKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.secretKey = secretKey;
        }
    }
}
