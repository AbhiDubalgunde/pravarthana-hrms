package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByDomain(String domain);

    boolean existsByDomain(String domain);
}
