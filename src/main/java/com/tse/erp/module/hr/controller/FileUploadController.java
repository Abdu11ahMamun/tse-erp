package com.tse.erp.module.hr.controller;

import com.tse.erp.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Authenticated image upload. The returned url is what the employee record
 * stores in its photo column; the file is then served publicly at that path.
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(
            value = "/employee-photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadEmployeePhoto(
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeEmployeePhoto(file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("url", url));
    }
}
