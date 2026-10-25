package com.example.pgfinder;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Fictional demonstration records so the UI and filters can be tried. No NoBroker URL, no real properties. */
@Component
public class DemoDataLoader implements CommandLineRunner {
    private final ListingRepository repo;
    private final boolean seed;

    public DemoDataLoader(ListingRepository repo, @Value("${app.seed-demo:false}") boolean seed) {
        this.repo = repo; this.seed = seed;
    }

    @Override
    public void run(String... args) {
        if (!seed || repo.count() > 0) return;
        demo("DEMO record A", Locality.ANDHERI, 12000, Audience.GIRLS, Set.of(Sharing.DOUBLE), Set.of(Food.VEG, Food.JAIN), Set.of("wifi", "laundry"), Availability.AVAILABLE);
        demo("DEMO record B", Locality.ANDHERI, 18000, Audience.BOYS, Set.of(Sharing.SINGLE, Sharing.DOUBLE), Set.of(Food.VEG, Food.NON_VEG), Set.of("wifi", "ac"), Availability.AVAILABLE);
        demo("DEMO record C", Locality.VILE_PARLE, 15000, Audience.CO_ED, Set.of(Sharing.DOUBLE), Set.of(), Set.of("wifi"), null);
        demo("DEMO record D (most details unknown)", Locality.VILE_PARLE, null, null, Set.of(), Set.of(), Set.of(), null);
    }

    private void demo(String name, Locality loc, Integer rent, Audience aud, Set<Sharing> sh, Set<Food> food,
                      Set<String> am, Availability av) {
        Listing l = new Listing();
        l.setName(name); l.setLocality(loc); l.setMonthlyRent(rent); l.setAudience(aud);
        l.setSharingOptions(new java.util.HashSet<>(sh)); l.setFoodOptions(new java.util.HashSet<>(food));
        l.setAmenities(new java.util.HashSet<>(am)); l.setAvailability(av);
        l.setNotes("Demonstration record, not a real property.");
        l.setSource(DataSource.DEMO); l.setLastChecked(LocalDate.now());
        repo.save(l);
    }
}
