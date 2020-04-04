package hr.fer.zemris.nos.lab2.files;

public class CryptedFile {
    private String description;
    private String method;
    private String fileName;
    private String data;

    public CryptedFile(String description, String method, String fileName, String data) {
        this.description = description;
        this.method = method;
        this.fileName = fileName;
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public String getData() {
        return data;
    }
}
