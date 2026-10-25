package com.example.pgfinder;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final ListingService service;

    public SearchController(ListingService service) { this.service = service; }

    /** Example: /api/listings?locality=ANDHERI&maxRent=15000&audience=GIRLS&food=JAIN&amenities=wifi */
    @GetMapping("/listings")
    public SearchResponse search(@RequestParam(required = false) Locality locality,
                                 @RequestParam(required = false) Integer minRent,
                                 @RequestParam(required = false) Integer maxRent,
                                 @RequestParam(required = false) Audience audience,
                                 @RequestParam(required = false) Sharing sharing,
                                 @RequestParam(required = false) Food food,
                                 @RequestParam(required = false) Availability availability,
                                 @RequestParam(required = false) List<String> amenities) {
        return service.search(new ListingFilter(locality, minRent, maxRent, audience, sharing, food, availability, amenities));
    }
}
