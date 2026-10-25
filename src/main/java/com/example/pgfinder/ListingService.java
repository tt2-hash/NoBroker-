package com.example.pgfinder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingService {
    static final String NS = "Not specified";
    static final String DISCLAIMER =
        "Prices and availability change often. Verify current details and contact the property provider "
      + "through the listing's source (NoBroker or BedR) before deciding or paying anything.";

    private final ListingRepository repo;

    public ListingService(ListingRepository repo) { this.repo = repo; }

    static String normalize(String s) {
        if (s == null) return null;
        String n = s.trim().toLowerCase();
        return n.isEmpty() ? null : n;
    }

    public SearchResponse search(ListingFilter f) {
        List<Listing> found = repo.findAll(ListingSpecs.from(f),
                Sort.by("monthlyRent").ascending().and(Sort.by("name").ascending()));
        boolean hasRealData = repo.existsBySourceNot(DataSource.DEMO);
        String mode = hasRealData ? "MANUAL_OR_AUTHORIZED_EXPORT" : "DEMO";
        String notice = hasRealData
            ? "Records were entered manually or imported from data we are permitted to use. They are NOT "
              + "synchronised live with NoBroker, BedR or any other site."
            : "No authorized property data has been loaded. These are demonstration records, not live NoBroker listings.";
        return new SearchResponse(mode, notice, DISCLAIMER, found.size(),
                found.stream().map(this::toView).collect(Collectors.toList()));
    }

    private <E extends Enum<E>> List<String> names(Collection<E> c) {
        if (c == null || c.isEmpty()) return List.of(NS);
        return c.stream().map(e -> e.name().replace('_', '-').toLowerCase()).sorted().collect(Collectors.toList());
    }

    ListingView toView(Listing l) {
        boolean demo = l.getSource() == DataSource.DEMO;
        return new ListingView(l.getId(), l.getName(),
                l.getLocality().name().replace('_', ' '),
                l.getMonthlyRent() == null ? NS : "₹" + l.getMonthlyRent() + " / month (indicative)",
                l.getAudience() == null ? NS : l.getAudience().name().replace('_', '-'),
                names(l.getSharingOptions()), names(l.getFoodOptions()),
                l.getAmenities() == null || l.getAmenities().isEmpty()
                        ? List.of(NS) : l.getAmenities().stream().sorted().collect(Collectors.toList()),
                l.getAvailability() == null ? NS : l.getAvailability().name().replace('_', ' ').toLowerCase(),
                l.getNotes(), sourceLabel(l.getSource()), l.getLastChecked().toString(),
                l.getVerificationStatus() == VerificationStatus.ADMIN_VERIFIED
                        ? "Verified by " + (l.getVerifiedBy() == null ? "admin" : l.getVerifiedBy())
                          + " on " + l.getVerifiedOn()
                        : "Unverified",
                l.getExternalUrl(), demo, contactText(l));
    }

    private static String sourceLabel(DataSource s) {
        return switch (s) {
            case BEDR_MANUAL -> "BedR India";
            case NOBROKER_MANUAL -> "NoBroker (entered manually)";
            case NOBROKER_AUTHORIZED_EXPORT -> "NoBroker (authorized export)";
            case DEMO -> "Sample data";
        };
    }

    private static String contactText(Listing l) {
        if (!Boolean.TRUE.equals(l.getContactConsent())) return null;
        String name = l.getContactName() == null ? "" : l.getContactName();
        String phone = l.getContactPhone() == null ? "" : l.getContactPhone();
        String text = (name + " " + phone).trim();
        if (text.isEmpty()) return null;
        return l.getSource() == DataSource.DEMO ? "Sample (fictional) contact: " + text : text;
    }

    private static String clean(String s, int max) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    /** Copies validated input onto the entity. Any edit resets verification. */
    public Listing apply(Listing l, ListingInput in) {
        if (in.name() == null || in.name().isBlank()) throw bad("name is required");
        if (in.locality() == null) throw bad("locality is required (ANDHERI or VILE_PARLE)");
        if (in.monthlyRent() != null && in.monthlyRent() < 0) throw bad("monthlyRent must not be negative");
        if (in.lastChecked() == null) throw bad("lastChecked is required");
        if (in.lastChecked().isAfter(LocalDate.now().plusDays(1))) throw bad("lastChecked cannot be in the future");
        if (in.source() == null) throw bad("source is required");
        if (in.source() == DataSource.DEMO && in.externalUrl() != null && !in.externalUrl().isBlank())
            throw bad("sample (DEMO) records must not have a NoBroker link");
        l.setName(in.name().trim());
        l.setLocality(in.locality());
        l.setMonthlyRent(in.monthlyRent());
        l.setAudience(in.audience());
        l.setSharingOptions(in.sharingOptions() == null ? new HashSet<>() : new HashSet<>(in.sharingOptions()));
        l.setFoodOptions(in.foodOptions() == null ? new HashSet<>() : new HashSet<>(in.foodOptions()));
        Set<String> am = new HashSet<>();
        if (in.amenities() != null) in.amenities().forEach(a -> { String n = normalize(a); if (n != null) am.add(n); });
        l.setAmenities(am);
        l.setAvailability(in.availability());
        l.setNotes(in.notes());
        l.setExternalUrl(UrlGuard.normalize(in.externalUrl()));
        l.setSource(in.source());
        boolean consent = Boolean.TRUE.equals(in.contactConsent());
        l.setContactConsent(consent);
        l.setContactName(consent ? clean(in.contactName(), 100) : null);
        l.setContactPhone(consent ? clean(in.contactPhone(), 20) : null);
        l.setLastChecked(in.lastChecked());
        l.setVerificationStatus(VerificationStatus.UNVERIFIED);
        l.setVerifiedBy(null);
        l.setVerifiedOn(null);
        return repo.save(l);
    }

    public Listing verify(Long id, String verifiedBy) {
        Listing l = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (l.getSource() == DataSource.DEMO) throw bad("demo records cannot be verified");
        l.setVerificationStatus(VerificationStatus.ADMIN_VERIFIED);
        l.setVerifiedBy(verifiedBy == null || verifiedBy.isBlank() ? "admin" : verifiedBy.trim());
        l.setVerifiedOn(LocalDate.now());
        l.setLastChecked(LocalDate.now());
        return repo.save(l);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }

    public Listing create(ListingInput in) { return apply(new Listing(), in); }

    public Listing update(Long id, ListingInput in) {
        Listing l = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return apply(l, in);
    }
}
