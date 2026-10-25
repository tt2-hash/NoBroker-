package com.example.pgfinder;

import java.io.InputStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * On startup, loads data/bedr-listings.csv once (if no BedR records exist yet) and removes the
 * built-in "DEMO record" samples. Edit or replace the CSV to change the starting data.
 */
@Component
@Order(1)
public class SeedFileLoader implements CommandLineRunner {
    private final ListingRepository repo;
    private final CsvImportService importer;

    public SeedFileLoader(ListingRepository repo, CsvImportService importer) {
        this.repo = repo; this.importer = importer;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource res = new ClassPathResource("data/bedr-listings.csv");
        if (!res.exists() || repo.existsBySource(DataSource.BEDR_MANUAL)) return;
        try (InputStream in = res.getInputStream()) {
            CsvImportService.Result r = importer.run(in, DataSource.BEDR_MANUAL);
            System.out.println("Seed file imported: " + r.imported() + " rows, errors: " + r.errors());
            if (r.imported() > 0) {
                repo.deleteAll(repo.findAll().stream()
                        .filter(l -> l.getSource() == DataSource.DEMO && l.getName().startsWith("DEMO record"))
                        .toList());
            }
        }
    }
}
