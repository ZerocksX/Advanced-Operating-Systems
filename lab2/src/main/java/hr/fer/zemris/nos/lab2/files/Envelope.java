package hr.fer.zemris.nos.lab2.files;

import java.util.List;

public class Envelope {
    private String description;
    private String fileName;
    private List<String> method;
    private List<String> keyLength;
    private String envelopeData;
    private String envelopeCryptKey;

    public Envelope(String description, String fileName, List<String> method, List<String> keyLength, String envelopeData, String envelopeCryptKey) {
        this.description = description;
        this.fileName = fileName;
        this.method = method;
        this.keyLength = keyLength;
        this.envelopeData = envelopeData;
        this.envelopeCryptKey = envelopeCryptKey;
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

    public String getEnvelopeData() {
        return envelopeData;
    }

    public String getEnvelopeCryptKey() {
        return envelopeCryptKey;
    }
}
