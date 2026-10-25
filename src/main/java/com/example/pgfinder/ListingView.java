package com.example.pgfinder;

import java.util.List;

public record ListingView(Long id, String name, String locality, String monthlyRent, String audience,
                          List<String> sharing, List<String> food, List<String> amenities,
                          String availability, String notes, String source, String lastChecked,
                          String verification, String nobrokerUrl, boolean demo) {}
