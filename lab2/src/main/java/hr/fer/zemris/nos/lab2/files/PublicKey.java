package hr.fer.zemris.nos.lab2.files;

public class PublicKey {
    private String description;
    private String method;
    private String keyLength;
    private String modulus;
    private String publicExponent;

    public PublicKey(String description, String method, String keyLength, String modulus, String publicExponent) {
        this.description = description;
        this.method = method;
        this.keyLength = keyLength;
        this.modulus = modulus;
        this.publicExponent = publicExponent;
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

    public String getPublicExponent() {
        return publicExponent;
    }
}
