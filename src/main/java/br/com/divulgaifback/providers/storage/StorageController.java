package br.com.divulgaifback.providers.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String upload(@RequestBody MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return storageService.upload(Objects.requireNonNull(file.getOriginalFilename()), inputStream, file.getSize());
        }
    }
}
