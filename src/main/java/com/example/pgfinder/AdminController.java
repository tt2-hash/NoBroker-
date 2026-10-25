package com.example.pgfinder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Minimal admin API protected by an X-Admin-Key header (set ADMIN_KEY in the environment).
 * For anything beyond a coursework demo, replace this with Spring Security + real accounts.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ListingService service;
    private final CsvImportService importer;
    private final String adminKey;

    public AdminController(ListingService service, CsvImportService importer, @Value("${app.admin-key:}") String adminKey) {
        this.service = service; this.importer = importer; this.adminKey = adminKey;
    }

    private void auth(String key) {
        boolean ok = !adminKey.isBlank() && key != null && MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin access denied");
    }

    @PostMapping("/listings")
    public ListingView create(@RequestHeader(value = "X-Admin-Key", required = false) String key, @RequestBody ListingInput in) {
        auth(key);
        return service.toView(service.create(in));
    }

    @PutMapping("/listings/{id}")
    public ListingView update(@RequestHeader(value = "X-Admin-Key", required = false) String key,
                              @PathVariable Long id, @RequestBody ListingInput in) {
        auth(key);
        return service.toView(service.update(id, in));
    }

    /** Call only after you have actually checked the attributes against the NoBroker listing / provider. */
    @PostMapping("/listings/{id}/verify")
    public ListingView verify(@RequestHeader(value = "X-Admin-Key", required = false) String key,
                              @PathVariable Long id, @RequestParam(required = false) String verifiedBy) {
        auth(key);
        return service.toView(service.verify(id, verifiedBy));
    }

    @PostMapping("/import")
    public CsvImportService.Result importCsv(@RequestHeader(value = "X-Admin-Key", required = false) String key,
                                             @RequestParam DataSource source, @RequestParam MultipartFile file) throws IOException {
        auth(key);
        if (source == DataSource.DEMO) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid source");
        return importer.run(file.getInputStream(), source);
    }
}
