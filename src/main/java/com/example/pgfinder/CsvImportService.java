package com.example.pgfinder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;

/**
 * Imports rows from a CSV you are permitted to use (manually compiled, or an export NoBroker authorized).
 * Header: name,locality,monthlyRent,audience,sharing,food,amenities,availability,externalUrl,lastChecked,notes
 * Multi-values use '|' (e.g. food = VEG|JAIN). A blank cell means "Not specified".
 */
@Service
public class CsvImportService {
    public record Result(int imported, List<String> errors) {}

    private final ListingService service;
    public CsvImportService(ListingService service) { this.service = service; }

    public Result run(InputStream in, DataSource source) throws IOException {
        CSVFormat fmt = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build();
        int ok = 0;
        List<String> errors = new ArrayList<>();
        try (Reader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             CSVParser parser = fmt.parse(r)) {
            for (CSVRecord rec : parser) {
                try {
                    service.create(new ListingInput(
                            rec.get("name"), en(Locality.class, rec.get("locality")), num(rec.get("monthlyRent")),
                            en(Audience.class, rec.get("audience")), set(Sharing.class, rec.get("sharing")),
                            set(Food.class, rec.get("food")), strs(rec.get("amenities")),
                            en(Availability.class, rec.get("availability")), blankToNull(rec.get("notes")),
                            rec.get("externalUrl"), source, date(rec.get("lastChecked"))));
                    ok++;
                } catch (Exception e) {
                    errors.add("row " + rec.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }
        return new Result(ok, errors);
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private static Integer num(String s) { return blankToNull(s) == null ? null : Integer.valueOf(s.trim()); }
    private static LocalDate date(String s) { return blankToNull(s) == null ? null : LocalDate.parse(s.trim()); }

    private static <E extends Enum<E>> E en(Class<E> c, String s) {
        return blankToNull(s) == null ? null : Enum.valueOf(c, s.trim().toUpperCase().replace('-', '_'));
    }
    private static <E extends Enum<E>> Set<E> set(Class<E> c, String s) {
        if (blankToNull(s) == null) return new HashSet<>();
        return Arrays.stream(s.split("\\|")).map(x -> en(c, x)).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    private static Set<String> strs(String s) {
        if (blankToNull(s) == null) return new HashSet<>();
        return Arrays.stream(s.split("\\|")).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.toSet());
    }
}
