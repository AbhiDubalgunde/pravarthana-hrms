package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCompanyId(Long companyId);

    List<Team> findByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

    Optional<Team> findByIdAndCompanyId(Long id, Long companyId);
}
