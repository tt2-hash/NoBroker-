package com.example.pgfinder;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One accommodation record. A null / empty attribute means "Not specified":
 * it is never guessed, and it never matches a filter on that attribute.
 */
@Entity
@Table(name = "listings")
@Getter @Setter @NoArgsConstructor
public class Listing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                       // our own label, not copied marketing text

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Locality locality;

    private Integer monthlyRent;               // INR, null = not specified

    @Enumerated(EnumType.STRING)
    private Audience audience;                 // null = not specified

    @ElementCollection(fetch = FetchType.EAGER) @Enumerated(EnumType.STRING)
    private Set<Sharing> sharingOptions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER) @Enumerated(EnumType.STRING)
    private Set<Food> foodOptions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> amenities = new HashSet<>();   // stored lower-case

    @Enumerated(EnumType.STRING)
    private Availability availability;         // null = not specified

    @Column(length = 500)
    private String notes;                      // our own short notes only

    @Column(length = 500)
    private String externalUrl;                // nobroker.in listing URL, if any

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DataSource source;

    @Column(nullable = false)
    private LocalDate lastChecked;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    // Contact details are shown only when contactConsent is true (owner agreed, or sample data).
    private Boolean contactConsent;
    private String contactName;
    private String contactPhone;

    private String verifiedBy;
    private LocalDate verifiedOn;
}
