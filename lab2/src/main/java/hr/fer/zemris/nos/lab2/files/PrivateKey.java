package hr.fer.zemris.nos.lab2.files;

public class PrivateKey {
    private String description;
    private String method;
    private String keyLength;
    private String modulus;
    private String privateExponent;

    public PrivateKey(String description, String method, String keyLength, String modulus, String privateExponent) {
        this.description = description;
        this.method = method;
        this.keyLength = keyLength;
        this.modulus = modulus;
        this.privateExponent = privateExponent;
    }

    public String getDescription() {
        return description;
    }

    public String getMethod() {
        return method;
    }

    public String getKeyLength() {
        return keyLength;
    }

    public String getModulus() {
        return modulus;
    }

    public String getPrivateExponent() {
        return privateExponent;
    }
}
