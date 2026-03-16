package com.pravarthana.hrms.repository;

import com.pravarthana.hrms.entity.Attendance;
import com.pravarthana.hrms.entity.Attendance.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

        Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

        List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

        List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(
                        Long employeeId, LocalDate from, LocalDate to);

        // ── Dashboard counts (no company_id in attendance table) ──────────────
        long countByDateAndStatus(LocalDate date, AttendanceStatus status);

}
