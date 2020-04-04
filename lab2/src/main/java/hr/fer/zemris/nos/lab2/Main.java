package hr.fer.zemris.nos.lab2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hr.fer.zemris.nos.lab2.files.*;
import hr.fer.zemris.nos.lab2.files.SecretKey;

import javax.crypto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Main {
    public static final Gson gson = new GsonBuilder().create();
    public static void main(String[] args) throws IOException {
//        if (args.length != 1) {
//            System.out.println("Please provide cipher mode");
//            return;
//        }
//        String cipherMode = args[0];
        String simCipherAlgorithm = "AES";
        String simCipherMode = "ECB";
        int simKeySize = 192;
        String aSimCipherAlgorithm = "RSA";
        String aSimCipherMode = "ECB";
        int aSimKeySize = 1024;
        String digestAlgorithm = "SHA3";
        String digestMode = "256";
        String fileName = "test.txt";
        String message = new String(Files.readAllBytes(Paths.get(fileName)));

        CryptoInfo senderInfo = getSenderInfo(simCipherAlgorithm, simCipherMode, simKeySize, aSimCipherAlgorithm, aSimCipherMode, aSimKeySize);

        MessageDigest messageDigest = CryptoFactory.getDigest(digestAlgorithm, digestMode);
        System.out.printf("Sending message: '%s'%n", message);
        DigitalStamp stamp = sendMessage(
                senderInfo.simCrypto,
                senderInfo.aSimSenderCrypto,
                senderInfo.aSimRecieverCrypto,
                messageDigest,
                message
        );

        String simSecretKey = Base64.getEncoder().encodeToString(senderInfo.simCrypto.getKeys().secretKey.getEncoded());

        String aSimSenderPublicKeyModulus = Base64.getEncoder().encodeToString(
                ((RSAPublicKey) senderInfo.aSimSenderCrypto.getKeys().privateKey)
                        .getModulus().toByteArray()
        );
        String aSimSenderPublicKeyExponent = Base64.getEncoder().encodeToString(
                ((RSAPublicKey) senderInfo.aSimSenderCrypto.getKeys().privateKey)
                        .getPublicExponent().toByteArray()
        );
        String aSimSenderPrivateKeyModulus = Base64.getEncoder().encodeToString(
                ((RSAPrivateKey) senderInfo.aSimSenderCrypto.getKeys().publicKey)
                        .getModulus().toByteArray()
        );
        String aSimSenderPrivateKeyExponent = Base64.getEncoder().encodeToString(
                ((RSAPrivateKey) senderInfo.aSimSenderCrypto.getKeys().publicKey)
                        .getPrivateExponent().toByteArray()
        );


        String aSimRecieverPrivateKeyModulus = Base64.getEncoder().encodeToString(
                ((RSAPrivateKey) senderInfo.aSimRecieverCrypto.getKeys().privateKey)
                        .getModulus().toByteArray()
        );
        String aSimRecieverPrivateKeyExponent = Base64.getEncoder().encodeToString(
                ((RSAPrivateKey) senderInfo.aSimRecieverCrypto.getKeys().privateKey)
                        .getPrivateExponent().toByteArray()
        );
        String aSimRecieverPublicKeyModulus = Base64.getEncoder().encodeToString(
                ((RSAPublicKey) senderInfo.aSimRecieverCrypto.getKeys().publicKey)
                        .getModulus().toByteArray()
        );
        String aSimRecieverPublicKeyExponent = Base64.getEncoder().encodeToString(
                ((RSAPublicKey) senderInfo.aSimRecieverCrypto.getKeys().publicKey)
                        .getPublicExponent().toByteArray()
        );

        CryptedFile cryptedFile = new CryptedFile(
                "Crypted fie",
                simCipherAlgorithm,
                fileName,
                stamp.digitalEnvelope.message
        );
        Files.writeString(Paths.get("cryptedFile.json"), gson.toJson(cryptedFile));

        SecretKey secretKey = new SecretKey(
                "Secret key",
                simCipherAlgorithm,
                simSecretKey
        );
        Files.writeString(Paths.get("secretKey.json"), gson.toJson(secretKey));


        PrivateKey senderPrivateKey = new PrivateKey(
                "Private key",
                aSimCipherAlgorithm,
                Integer.toHexString(aSimKeySize),
                aSimSenderPrivateKeyModulus,
                aSimSenderPrivateKeyExponent
        );
        Files.writeString(Paths.get("senderPrivateKey.json"), gson.toJson(senderPrivateKey));


        PrivateKey recieverPrivateKey = new PrivateKey(
                "Private key",
                aSimCipherAlgorithm,
                Integer.toHexString(aSimKeySize),
                aSimRecieverPrivateKeyModulus,
                aSimRecieverPrivateKeyExponent
        );
        Files.writeString(Paths.get("recieverPrivateKey.json"), gson.toJson(recieverPrivateKey));

        PublicKey senderPublicKey = new PublicKey(
                "Public key",
                aSimCipherAlgorithm,
                Integer.toHexString(aSimKeySize),
                aSimSenderPublicKeyModulus,
                aSimSenderPublicKeyExponent
        );
        Files.writeString(Paths.get("senderPublicKey.json"), gson.toJson(senderPublicKey));

        PublicKey recieverPublicKey = new PublicKey(
                "Public key",
                aSimCipherAlgorithm,
                Integer.toHexString(aSimKeySize),
                aSimRecieverPublicKeyModulus,
                aSimRecieverPublicKeyExponent
        );
        Files.writeString(Paths.get("recieverPublicKey.json"), gson.toJson(recieverPublicKey));


        Signature signature = new Signature(
                "Signature",
                fileName,
                Arrays.asList(
                        digestAlgorithm,
                        aSimCipherAlgorithm
                ),
                Arrays.asList(
                        Integer.toHexString(stamp.digitalSignature.signature.getBytes().length),
                        Integer.toHexString(aSimKeySize)
                ),
                stamp.digitalSignature.signature
        );
        Files.writeString(Paths.get("signature.json"), gson.toJson(signature));

        Envelope envelope = new Envelope(
                "Envelope",
                fileName,
                Arrays.asList(
                        simCipherAlgorithm,
                        aSimCipherAlgorithm
                ),
                Arrays.asList(
                        Integer.toHexString(simKeySize),
                        Integer.toHexString(aSimKeySize)
                ),
                stamp.digitalEnvelope.message,
                stamp.digitalEnvelope.key
        );
        Files.writeString(Paths.get("envelope.json"), gson.toJson(envelope));


        CryptoInfo recieverInfo = getRecieverInfo(
                aSimCipherAlgorithm, aSimCipherMode,
                aSimSenderPrivateKeyModulus, aSimSenderPrivateKeyExponent, aSimSenderPublicKeyModulus, aSimSenderPublicKeyExponent,
                aSimRecieverPrivateKeyModulus, aSimRecieverPrivateKeyExponent, aSimRecieverPublicKeyModulus, aSimRecieverPublicKeyExponent
        );

        String receivedMessage = readMessage(
                simCipherAlgorithm, simCipherMode,
                recieverInfo.aSimSenderCrypto,
                recieverInfo.aSimRecieverCrypto,
                messageDigest,
                stamp
        );
        System.out.println("Recieved message: ");
        System.out.println(receivedMessage);

    }

    private static CryptoInfo getSenderInfo(String simCipherAlgorithm, String simCipherMode, int simKeySize, String aSimCipherAlgorithm, String aSimCipherMode, int aSimKeySize) {
        Crypto simCrypto = CryptoFactory.getCrypto(
                simCipherAlgorithm,
                simCipherMode,
                Cipher.ENCRYPT_MODE,
                new CryptoFactory.KeyParams(
                        simKeySize,
                        false
                )
        );
        Crypto aSimSenderCrypto = CryptoFactory.getCrypto(
                aSimCipherAlgorithm,
                aSimCipherMode,
                Cipher.ENCRYPT_MODE,
                new CryptoFactory.KeyParams(
                        aSimKeySize,
                        true
                )
        );
        Crypto aSimRecieverCrypto = CryptoFactory.getCrypto(
                aSimCipherAlgorithm,
                aSimCipherMode,
                Cipher.ENCRYPT_MODE,
                new CryptoFactory.KeyParams(
                        aSimKeySize,
                        false
                )
        );
        return new CryptoInfo(
                simCrypto,
                aSimSenderCrypto,
                aSimRecieverCrypto
        );
    }


    public static DigitalStamp sendMessage(Crypto simCrypto, Crypto aSimSenderCrypto, Crypto aSimRecieverCrypto, MessageDigest messageDigest, String message) {
        Cipher simCipher = simCrypto.getCipher();
        Cipher aSimRecieverCipher = aSimRecieverCrypto.getCipher();
        Cipher aSimSenderCipher = aSimSenderCrypto.getCipher();
        try {
            DigitalEnvelope envelope = new DigitalEnvelope(
                    Base64.getEncoder().encodeToString(simCipher.doFinal(message.getBytes())),
                    Base64.getEncoder().encodeToString(aSimRecieverCipher.doFinal(simCrypto.getKeys().secretKey.getEncoded()))
            );
            System.out.println("Envelope:");
            System.out.println("message: ");
            System.out.println(envelope.message);
            System.out.println("key: ");
            System.out.println(envelope.key);
            DigitalSignature signature = new DigitalSignature(
                    envelope.message,
                    Base64.getEncoder().encodeToString(aSimSenderCipher.doFinal(messageDigest.digest(envelope.message.getBytes())))
            );
            System.out.println("Signature: ");
            System.out.println("message: ");
            System.out.println(signature.message);
            System.out.println("signature: ");
            System.out.println(signature.signature);
            return new DigitalStamp(envelope, signature);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static CryptoInfo getRecieverInfo(
            String aSimCipherAlgorithm, String aSimCipherMode,
            String aSimSenderPrivateKeyModulus, String aSimSenderPrivateKeyExponent, String aSimSenderPublicKeyModulus, String aSimSenderPublicKeyExponent,
            String aSimRecieverPrivateKeyModulus, String aSimRecieverPrivateKeyExponent, String aSimRecieverPublicKeyModulus, String aSimRecieverPublicKeyExponent) {
        Crypto simCrypto = null;
        Crypto aSimSenderCrypto = CryptoFactory.getCrypto(
                aSimCipherAlgorithm,
                aSimCipherMode,
                Cipher.DECRYPT_MODE,
                new CryptoFactory.KeyParams(
                        null,
                        aSimSenderPrivateKeyModulus,
                        aSimSenderPrivateKeyExponent,
                        aSimSenderPublicKeyModulus,
                        aSimSenderPublicKeyExponent,
                        true
                )
        );
        Crypto aSimRecieverCrypto = CryptoFactory.getCrypto(
                aSimCipherAlgorithm,
                aSimCipherMode,
                Cipher.DECRYPT_MODE,
                new CryptoFactory.KeyParams(
                        null,
                        aSimRecieverPrivateKeyModulus,
                        aSimRecieverPrivateKeyExponent,
                        aSimRecieverPublicKeyModulus,
                        aSimRecieverPublicKeyExponent,
                        false
                )
        );
        return new CryptoInfo(
                simCrypto,
                aSimSenderCrypto,
                aSimRecieverCrypto
        );
    }

    private static String readMessage(String simCipherAlgorithm, String simCipherMode, Crypto aSimSenderCrypto, Crypto aSimRecieverCrypto, MessageDigest messageDigest, DigitalStamp stamp) {
        Cipher aSimSenderCipher = aSimSenderCrypto.getCipher();
        Cipher aSimRecieverCipher = aSimRecieverCrypto.getCipher();
        String hash = Base64.getEncoder().encodeToString(messageDigest.digest(stamp.digitalSignature.message.getBytes()));
        try {
            String gotHash = Base64.getEncoder().encodeToString(aSimSenderCipher.doFinal(Base64.getDecoder().decode(stamp.digitalSignature.signature)));
            System.out.printf("Is same hash? - %s%n", hash.equals(gotHash));
            String simKey = Base64.getEncoder().encodeToString(aSimRecieverCipher.doFinal(Base64.getDecoder().decode(stamp.digitalEnvelope.key)));
            Crypto simCrypto = CryptoFactory.getCrypto(
                    simCipherAlgorithm,
                    simCipherMode,
                    Cipher.DECRYPT_MODE,
                    new CryptoFactory.KeyParams(
                            simKey,
                            null,
                            null,
                            null,
                            null,
                            false
                    )
            );
            Cipher simCipher = simCrypto.getCipher();
            String message = new String(simCipher.doFinal(Base64.getDecoder().decode(stamp.digitalEnvelope.message)));
            return message;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }


    public static class CryptoInfo {
        private Crypto simCrypto;
        private Crypto aSimSenderCrypto;
        private Crypto aSimRecieverCrypto;

        public CryptoInfo(Crypto simCrypto, Crypto aSimSenderCrypto, Crypto aSimRecieverCrypto) {
            this.simCrypto = simCrypto;
            this.aSimSenderCrypto = aSimSenderCrypto;
            this.aSimRecieverCrypto = aSimRecieverCrypto;
        }

        public Crypto getSimCrypto() {
            return simCrypto;
        }

        public Crypto getaSimSenderCrypto() {
            return aSimSenderCrypto;
        }

        public Crypto getaSimRecieverCrypto() {
            return aSimRecieverCrypto;
        }
    }

    public static class DigitalStamp {
        private DigitalEnvelope digitalEnvelope;
        private DigitalSignature digitalSignature;

        public DigitalStamp(DigitalEnvelope digitalEnvelope, DigitalSignature digitalSignature) {
            this.digitalEnvelope = digitalEnvelope;
            this.digitalSignature = digitalSignature;
        }

        public DigitalEnvelope getDigitalEnvelope() {
            return digitalEnvelope;
        }

        public DigitalSignature getDigitalSignature() {
            return digitalSignature;
        }
    }

    public static class DigitalEnvelope {
        private String message;
        private String key;

        public DigitalEnvelope(String message, String key) {
            this.message = message;
            this.key = key;
        }

        public String getMessage() {
            return message;
        }

        public String getKey() {
            return key;
        }
    }

    public static class DigitalSignature {
        private String message;
        private String signature;

        public DigitalSignature(String message, String signature) {
            this.message = message;
            this.signature = signature;
        }

        public String getMessage() {
            return message;
        }

        public String getSignature() {
            return signature;
        }
    }
}
