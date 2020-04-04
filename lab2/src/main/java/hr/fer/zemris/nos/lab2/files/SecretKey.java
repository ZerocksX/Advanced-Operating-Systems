package hr.fer.zemris.nos.lab2.files;

public class SecretKey {
    private String description;
    private String method;
    private String secretKey;

    public SecretKey(String description, String method, String secretKey) {
        this.description = description;
        this.method = method;
        this.secretKey = secretKey;
    }

    public String getDescription() {
        return description;
    }

    public String getMethod() {
        return method;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
