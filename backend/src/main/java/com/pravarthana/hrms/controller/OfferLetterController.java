package com.pravarthana.hrms.controller;

import com.pravarthana.hrms.dto.request.OfferLetterRequest;
import com.pravarthana.hrms.dto.response.OfferLetterResponse;
import com.pravarthana.hrms.service.OfferLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/offer-letters")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','HR_ADMIN')")
public class OfferLetterController {

    private final OfferLetterService offerLetterService;

    /** POST /api/offer-letters — create and generate PDF */
    @PostMapping
    public ResponseEntity<OfferLetterResponse> create(
            @Valid @RequestBody OfferLetterRequest req) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offerLetterService.create(req));
    }

    /** GET /api/offer-letters — list all for company */
    @GetMapping
    public ResponseEntity<Page<OfferLetterResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(offerLetterService.list(page, size));
    }

    /** GET /api/offer-letters/{id}/download — stream PDF */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
        byte[] bytes = offerLetterService.getPdfBytes(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"offer-letter-" + id + ".pdf\"")
                .body(bytes);
    }
}
