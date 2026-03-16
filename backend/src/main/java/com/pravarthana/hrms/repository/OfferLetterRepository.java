package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.OfferLetter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfferLetterRepository extends JpaRepository<OfferLetter, Long> {

    /** Used to check uniqueness of letter number before insert */
    Optional<OfferLetter> findByLetterNumber(String letterNumber);

    /** List all offer letters ordered by creation date (no company_id in DB) */
    Page<OfferLetter> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
