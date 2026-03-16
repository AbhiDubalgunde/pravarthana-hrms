package com.pravarthana.hrms.service;

import com.pravarthana.hrms.dto.response.DashboardStatsResponse;
import com.pravarthana.hrms.entity.Attendance.AttendanceStatus;
import com.pravarthana.hrms.entity.Leave.LeaveStatus;
import com.pravarthana.hrms.repository.AttendanceRepository;
import com.pravarthana.hrms.repository.EmployeeRepository;
import com.pravarthana.hrms.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final EmployeeRepository employeeRepository;
        private final AttendanceRepository attendanceRepository;
        private final LeaveRepository leaveRepository;

        public DashboardStatsResponse getStats() {
                // NOTE: attendance and leaves tables have NO company_id column.
                // Use global counts (this is a single-company deployment).
                DashboardStatsResponse dto = new DashboardStatsResponse();
                dto.setTotalEmployees(employeeRepository.count());
                dto.setPresentToday(attendanceRepository.countByDateAndStatus(
                                LocalDate.now(), AttendanceStatus.PRESENT));
                dto.setOnLeave(attendanceRepository.countByDateAndStatus(
                                LocalDate.now(), AttendanceStatus.LEAVE));
                dto.setPendingLeaves(leaveRepository.countByStatus(LeaveStatus.PENDING));
                return dto;
        }
}
