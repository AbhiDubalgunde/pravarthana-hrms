package com.pravarthana.hrms.dto.response;

import lombok.Data;

@Data
public class DashboardStatsResponse {
    private long totalEmployees;
    private long presentToday;
    private long onLeave;
    private long pendingLeaves;
}
