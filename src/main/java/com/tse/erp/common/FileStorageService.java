package com.tse.erp.common;

import com.tse.erp.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Saves uploaded images on local disk and returns the public URL path that is
 * stored in the database (the image itself is never stored in the DB).
 *
 * The root folder comes from app.upload.dir. It must be a persistent absolute
 * path outside the build folder (target/), so files survive restarts/redeploys.
 * To move to cloud storage later, only this class needs to change.
 */
@Slf4j
@Service
public class FileStorageService {

    public static final String URL_PREFIX = "/uploads/";
    public static final String EMPLOYEE_PHOTO_DIR = "employee-photos";
    private static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:${user.home}/tse-erp-uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root.resolve(EMPLOYEE_PHOTO_DIR));
        } catch (IOException e) {
            log.error("Cannot create upload directory {}", root, e);
            throw new IllegalStateException(
                    "Cannot create upload directory: " + root, e);
        }
        // Printed once at startup so the real location is visible in the server log.
        Path photos = root.resolve(EMPLOYEE_PHOTO_DIR);
        if (Files.isWritable(photos)) {
            log.info("Upload directory: {} (exists, writable)", root);
        } else {
            log.error("Upload directory {} is NOT writable by user '{}': "
                            + "photo uploads will fail. Fix the folder "
                            + "permissions or set UPLOAD_DIR.",
                    root, System.getProperty("user.name"));
        }
    }

    public Path getRoot() {
        return root;
    }

    /** Validates (size + real file signature) and saves; returns e.g. /uploads/employee-photos/{uuid}.png */
    public String storeEmployeePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Photo file is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Photo must not exceed 2 MB");
        }

        String extension;
        try (InputStream in = file.getInputStream()) {
            extension = detectImageExtension(in.readNBytes(8));
        } catch (IOException e) {
            throw new BadRequestException("Photo file could not be read");
        }
        if (extension == null) {
            throw new BadRequestException(
                    "Photo must be a JPG, JPEG or PNG image");
        }

        // The name is generated here; the client's file name is never used.
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(EMPLOYEE_PHOTO_DIR)
                .resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new BadRequestException("Invalid file path");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            log.error("Could not save uploaded photo to {}", target, e);
            throw new IllegalStateException(
                    "Could not save the uploaded photo", e);
        }
        return URL_PREFIX + EMPLOYEE_PHOTO_DIR + "/" + fileName;
    }

    // JPEG: FF D8 FF   PNG: 89 50 4E 47 0D 0A 1A 0A
    private static String detectImageExtension(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF
                && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 0x50
                && h[2] == 0x4E && h[3] == 0x47 && h[4] == 0x0D
                && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return "png";
        }
        return null;
    }
}
