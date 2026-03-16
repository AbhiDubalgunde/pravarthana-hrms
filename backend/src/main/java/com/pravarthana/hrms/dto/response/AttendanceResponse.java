package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.Attendance;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Double totalHours;
    private String status;
    private LocalDateTime createdAt;

    public static AttendanceResponse from(Attendance a) {
        AttendanceResponse r = new AttendanceResponse();
        r.id           = a.getId();
        r.employeeId   = a.getEmployeeId();
        r.date         = a.getDate();
        r.checkInTime  = a.getCheckInTime();
        r.checkOutTime = a.getCheckOutTime();
        r.totalHours   = a.getTotalHours();
        r.status       = a.getStatus() != null ? a.getStatus().name() : "ABSENT";
        r.createdAt    = a.getCreatedAt();
        return r;
    }
}
