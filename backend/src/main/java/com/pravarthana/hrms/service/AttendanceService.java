package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.response.AttendanceResponse;
import com.pravarthana.hrms.entity.Attendance;
import com.pravarthana.hrms.entity.Attendance.AttendanceStatus;
import com.pravarthana.hrms.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // ── Punch In ─────────────────────────────────────────────────
    public AttendanceResponse punchIn(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance record = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setEmployeeId(employeeId);
                    a.setDate(today);
                    a.setStatus(AttendanceStatus.ABSENT);
                    return a;
                });

        if (record.getCheckInTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Already punched in for today at " + record.getCheckInTime());
        }

        record.setCheckInTime(LocalTime.now());
        record.setStatus(AttendanceStatus.PRESENT);
        return AttendanceResponse.from(attendanceRepository.save(record));
    }

    // ── Punch Out ────────────────────────────────────────────────
    public AttendanceResponse punchOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance record = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No punch-in found for today. Please punch in first."));

        if (record.getCheckInTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have not punched in today.");
        }
        if (record.getCheckOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already punched out today.");
        }

        LocalTime punchOut = LocalTime.now();
        record.setCheckOutTime(punchOut);

        // Calculate total hours
        double hours = Duration.between(record.getCheckInTime(), punchOut).toMinutes() / 60.0;
        record.setTotalHours(Math.round(hours * 100.0) / 100.0);

        // Mark as HALF_DAY if < 5 hours
        if (hours < 5.0) record.setStatus(AttendanceStatus.HALF_DAY);

        return AttendanceResponse.from(attendanceRepository.save(record));
    }

    // ── Today's record ───────────────────────────────────────────
    public AttendanceResponse getToday(Long employeeId) {
        return attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .map(AttendanceResponse::from)
                .orElseGet(() -> {
                    AttendanceResponse r = new AttendanceResponse();
                    r.setEmployeeId(employeeId);
                    r.setDate(LocalDate.now());
                    r.setStatus("ABSENT");
                    return r;
                });
    }

    // ── All records for employee ──────────────────────────────────
    public List<AttendanceResponse> getByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId)
                .stream().map(AttendanceResponse::from).toList();
    }

    // ── Monthly records ──────────────────────────────────────────
    public List<AttendanceResponse> getMonthly(Long employeeId, String month) {
        // month format: YYYY-MM
        YearMonth ym = YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to   = ym.atEndOfMonth();
        return attendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, from, to)
                .stream().map(AttendanceResponse::from).toList();
    }
}
