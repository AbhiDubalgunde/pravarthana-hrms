package com.pravarthana.hrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OfferLetterRequest {

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Valid email is required")
    private String candidateEmail;

    /** Maps to OfferLetter.designation (the position/role) */
    @NotBlank(message = "Position/designation is required")
    private String designation;

    private String department;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    private BigDecimal salary;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String location;
    private String reportingManager; // stored in template content, not its own column
    private String workingHours; // stored in template content, not its own column
    private Integer probationMonths; // stored in template content, not its own column
}
