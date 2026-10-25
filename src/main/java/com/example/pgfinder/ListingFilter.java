package com.example.pgfinder;

import java.util.List;

public record ListingFilter(Locality locality, Integer minRent, Integer maxRent, Audience audience,
                            Sharing sharing, Food food, Availability availability, List<String> amenities) {}
