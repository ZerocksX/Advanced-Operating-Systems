package hr.fer.zemris.nos.lab2.files;

import java.util.List;

public class Signature {
    private String description;
    private String fileName;
    private List<String> method;
    private List<String> keyLength;
    private String signature;

    public Signature(String description, String fileName, List<String> method, List<String> keyLength, String signature) {
        this.description = description;
        this.fileName = fileName;
        this.method = method;
        this.keyLength = keyLength;
        this.signature = signature;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getMethod() {
        return method;
    }

    public List<String> getKeyLength() {
        return keyLength;
    }

    public String getSignature() {
        return signature;
    }
}
