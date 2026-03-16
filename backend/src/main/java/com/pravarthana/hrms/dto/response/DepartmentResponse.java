package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.Department;
import com.pravarthana.hrms.entity.Team;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class DepartmentResponse {

    private Long   id;
    private Long   companyId;
    private String name;
    private String description;
    private Long   headEmployeeId;
    private Boolean isActive;
    private int    employeeCount;
    private LocalDateTime createdAt;

    /** Teams that belong to this department (populated on demand) */
    private List<TeamSummary> teams;

    public static DepartmentResponse from(Department d) {
        DepartmentResponse r = new DepartmentResponse();
        r.id             = d.getId();
        r.companyId      = d.getCompanyId();
        r.name           = d.getName();
        r.description    = d.getDescription();
        r.headEmployeeId = d.getHeadEmployeeId();
        r.isActive       = d.getIsActive();
        r.createdAt      = d.getCreatedAt();
        return r;
    }

    @Data
    @NoArgsConstructor
    public static class TeamSummary {
        private Long   id;
        private String name;
        private String description;

        public static TeamSummary from(Team t) {
            TeamSummary s = new TeamSummary();
            s.id          = t.getId();
            s.name        = t.getName();
            s.description = t.getDescription();
            return s;
        }
    }
}
