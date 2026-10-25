package com.example.pgfinder;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

/**
 * Every predicate requires the attribute to be present AND equal. SQL comparisons against NULL
 * are never true, so listings with an unknown attribute are excluded from that filter.
 */
public final class ListingSpecs {
    private ListingSpecs() {}

    public static Specification<Listing> from(ListingFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.locality() != null) p.add(cb.equal(root.get("locality"), f.locality()));
            if (f.minRent() != null) p.add(cb.greaterThanOrEqualTo(root.<Integer>get("monthlyRent"), f.minRent()));
            if (f.maxRent() != null) p.add(cb.lessThanOrEqualTo(root.<Integer>get("monthlyRent"), f.maxRent()));
            if (f.audience() != null) p.add(cb.equal(root.get("audience"), f.audience()));
            if (f.availability() != null) p.add(cb.equal(root.get("availability"), f.availability()));
            if (f.sharing() != null) p.add(cb.isMember(f.sharing(), root.<Set<Sharing>>get("sharingOptions")));
            if (f.food() != null) p.add(cb.isMember(f.food(), root.<Set<Food>>get("foodOptions")));
            if (f.amenities() != null) {
                for (String a : f.amenities()) {
                    String n = ListingService.normalize(a);
                    if (n != null) p.add(cb.isMember(n, root.<Set<String>>get("amenities")));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
