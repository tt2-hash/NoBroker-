package com.example.pgfinder;

import java.util.List;

public record SearchResponse(String dataMode, String notice, String disclaimer, int count, List<ListingView> results) {}
