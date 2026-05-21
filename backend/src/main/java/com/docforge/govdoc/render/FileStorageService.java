package com.docforge.govdoc.render;

import com.docforge.govdoc.config.AppStorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {
    private final AppStorageProperties properties;

    public FileStorageService(AppStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root());
        Files.createDirectories(root().resolve("templates"));
        Files.createDirectories(root().resolve("exports"));
        Files.createDirectories(root().resolve("uploads"));
    }

    public Path root() {
        return Paths.get(properties.root()).toAbsolutePath().normalize();
    }

    public String writeTemplateFile(String templateVersionId, byte[] data) throws IOException {
        Path path = root().resolve("templates").resolve(templateVersionId + ".docx");
        Files.write(path, data);
        return path.toString();
    }

    public String writeExportFile(String exportJobId, byte[] data) throws IOException {
        Path path = root().resolve("exports").resolve(exportJobId + ".docx");
        Files.write(path, data);
        return path.toString();
    }

    public String writeUploadFile(String fileName, byte[] data) throws IOException {
        Path path = root().resolve("uploads").resolve(fileName);
        Files.write(path, data);
        return path.toString();
    }

    public byte[] readFile(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }
}

