package hr.fer.zemris.nos.lab2;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class CryptoFactory {
    private static Map<String, Function<String, Cipher>> ciphers = new HashMap<>();
    private static Map<String, Function<String, MessageDigest>> digests = new HashMap<>();
    private static Map<String, Function<KeyParams, Crypto.Keys>> keys = new HashMap<>();
    private static Map<String, BiConsumer<Integer, Crypto>> cipherInits = new HashMap<>();

    static {
        ciphers.put("AES", ciphermode -> {
            try {
                return Cipher.getInstance(String.format("AES/%s/PKCS5Padding", ciphermode));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
        });
        ciphers.put("3DES", ciphermode -> {
            try {
                return Cipher.getInstance(String.format("DESede/%s/PKCS5Padding", ciphermode));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
        });
        ciphers.put("RSA", ciphermode -> {
            try {
                return Cipher.getInstance(String.format("RSA/%s/PKCS1Padding", ciphermode));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
        });
        digests.put("SHA2", digestMode -> {
            try {
                return MessageDigest.getInstance(String.format("SHA-%s", digestMode));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        digests.put("SHA3", digestMode -> {
            try {
                return MessageDigest.getInstance(String.format("SHA3-%s", digestMode));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        keys.put("RSA", keyParams -> {
            try {
                KeyPair keyPair;
                if (keyParams.keySize == null) {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                    PublicKey publicKey = keyFactory.generatePublic(
                            new RSAPublicKeySpec(
                                    new BigInteger(Base64.getDecoder().decode(keyParams.publicModulus)),
                                    new BigInteger(Base64.getDecoder().decode(keyParams.publicExponent))
                            )
                    );
                    PrivateKey privateKey = keyFactory.generatePrivate(
                            new RSAPrivateKeySpec(
                                    new BigInteger(Base64.getDecoder().decode(keyParams.privateModulus)),
                                    new BigInteger(Base64.getDecoder().decode(keyParams.privateExponent))
                            )
                    );
                    keyPair = new KeyPair(publicKey, privateKey);
                } else {
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    keyPairGenerator.initialize(keyParams.keySize);
                    keyPair = keyPairGenerator.generateKeyPair();
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    keyPair = new KeyPair(
                            keyFactory.generatePublic(new RSAPublicKeySpec(
                                    ((RSAPublicKey) keyPair.getPublic()).getModulus(),
                                    ((RSAPublicKey) keyPair.getPublic()).getPublicExponent()
                            )),
                            keyFactory.generatePrivate(new RSAPrivateKeySpec(
                                    ((RSAPrivateKey) keyPair.getPrivate()).getModulus(),
                                    ((RSAPrivateKey) keyPair.getPrivate()).getPrivateExponent()
                            ))
                    );
                }

                if (keyParams.inverseKeys) {
                    return new Crypto.Keys(keyPair.getPublic(), keyPair.getPrivate(), null);
                } else {
                    return new Crypto.Keys(keyPair.getPrivate(), keyPair.getPublic(), null);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        });
        keys.put("AES", keyParams -> {
            try {
                Key key;
                if (keyParams.keySize != null) {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                    keyGenerator.init(keyParams.keySize);
                    key = keyGenerator.generateKey();
                } else {
                    key = new SecretKeySpec(Base64.getDecoder().decode(keyParams.secretKey), "AES");
                }
                return new Crypto.Keys(null, null, key);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        keys.put("3DES", keyParams -> {
            try {
                Key key;
                if (keyParams.keySize != null) {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
                    keyGenerator.init(keyParams.keySize);
                    key = keyGenerator.generateKey();
                } else {
                    key = new SecretKeySpec(Base64.getDecoder().decode(keyParams.secretKey), "DESede");
                }
                return new Crypto.Keys(null, null, key);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });

        cipherInits.put("AES", (mode, crypto) -> {
            try {
                crypto.getCipher().init(mode, crypto.getKeys().secretKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });
        cipherInits.put("3DES", (mode, crypto) -> {
            try {
                crypto.getCipher().init(mode, crypto.getKeys().secretKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });
        cipherInits.put("RSA", (mode, crypto) -> {
            try {
                crypto.getCipher().init(mode, mode == Cipher.ENCRYPT_MODE ? crypto.getKeys().publicKey : crypto.getKeys().privateKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static MessageDigest getDigest(String digestAlgorithm, String digestMode) {
        return digests.get(digestAlgorithm).apply(digestMode);
    }

    public static Crypto getCrypto(String cipherAlgorithm, String cipherMode, Integer mode, KeyParams keyParams) {
        Cipher cipher = ciphers.get(cipherAlgorithm).apply(cipherMode);
        Crypto.Keys cryptoKeys = keys.get(cipherAlgorithm).apply(keyParams);
        Crypto crypto = new Crypto(
                cipher,
                cryptoKeys
        );
        cipherInits.get(cipherAlgorithm).accept(mode, crypto);
        return crypto;
    }

    public static class KeyParams {
        Integer keySize;
        String secretKey;
        String privateModulus;
        String privateExponent;
        String publicModulus;
        String publicExponent;
        boolean inverseKeys;


        public KeyParams(Integer keySize, boolean inverseKeys) {
            this.keySize = keySize;
            this.inverseKeys = inverseKeys;
        }

        public KeyParams(String secretKey, String privateModulus, String privateExponent, String publicModulus, String publicExponent, boolean inverseKeys) {
            this.secretKey = secretKey;
            this.privateModulus = privateModulus;
            this.privateExponent = privateExponent;
            this.publicModulus = publicModulus;
            this.publicExponent = publicExponent;
            this.inverseKeys = inverseKeys;
        }
    }

}
