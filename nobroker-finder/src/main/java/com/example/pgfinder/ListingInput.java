package com.example.pgfinder;

import java.time.LocalDate;
import java.util.Set;

/** Admin input. Only put facts here that the source (or a manual check) explicitly confirms. */
public record ListingInput(String name, Locality locality, Integer monthlyRent, Audience audience,
                           Set<Sharing> sharingOptions, Set<Food> foodOptions, Set<String> amenities,
                           Availability availability, String notes, String externalUrl,
                           DataSource source, LocalDate lastChecked,
                           String contactName, String contactPhone, Boolean contactConsent) {}
