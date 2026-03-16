package com.pravarthana.hrms.dto.response;

import com.pravarthana.hrms.entity.OfferLetter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OfferLetterResponse {
    private Long id;
    private String letterNumber;
    private String candidateName;
    private String candidateEmail;
    private String designation; // was 'position' before schema fix
    private String department;
    private BigDecimal salary; // was 'annualCTC' before schema fix
    private LocalDate joiningDate;
    private String location;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime generatedAt;
    private String downloadUrl;

    public static OfferLetterResponse from(OfferLetter ol) {
        OfferLetterResponse r = new OfferLetterResponse();
        r.setId(ol.getId());
        r.setLetterNumber(ol.getLetterNumber());
        r.setCandidateName(ol.getCandidateName());
        r.setCandidateEmail(ol.getCandidateEmail());
        r.setDesignation(ol.getDesignation());
        r.setDepartment(ol.getDepartment());
        r.setSalary(ol.getSalary());
        r.setJoiningDate(ol.getJoiningDate());
        r.setLocation(ol.getLocation());
        r.setStatus(ol.getStatus());
        r.setCreatedAt(ol.getCreatedAt());
        r.setGeneratedAt(ol.getGeneratedAt());
        if (ol.getId() != null && ol.getPdfUrl() != null) {
            r.setDownloadUrl("/api/offer-letters/" + ol.getId() + "/download");
        }
        return r;
    }
}
