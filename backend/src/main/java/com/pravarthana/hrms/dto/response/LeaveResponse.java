package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.Leave;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private String reason;
    private String status;
    private String rejectionReason;
    private LocalDateTime reviewedAt;

    // Optional employee info for HR view
    private String employeeName;
    private String department;

    public static LeaveResponse from(Leave l) {
        LeaveResponse r = new LeaveResponse();
        r.setId(l.getId());
        r.setEmployeeId(l.getEmployeeId());
        r.setLeaveType(l.getLeaveType());
        r.setStartDate(l.getStartDate());
        r.setEndDate(l.getEndDate());
        r.setDays(l.getDays());
        r.setReason(l.getReason());
        r.setStatus(l.getStatus().name());
        r.setRejectionReason(l.getRejectionReason());
        r.setReviewedAt(l.getReviewedAt());
        return r;
    }
}
