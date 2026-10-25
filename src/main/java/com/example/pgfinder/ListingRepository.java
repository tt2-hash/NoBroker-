package com.example.pgfinder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {
    boolean existsBySourceNot(DataSource source);
    boolean existsBySource(DataSource source);
}
