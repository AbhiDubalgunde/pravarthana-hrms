package com.pravarthana.hrms.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.pravarthana.hrms.dto.request.OfferLetterRequest;
import com.pravarthana.hrms.dto.response.OfferLetterResponse;
import com.pravarthana.hrms.entity.OfferLetter;
import com.pravarthana.hrms.repository.OfferLetterRepository;
import com.pravarthana.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferLetterService {

    private final OfferLetterRepository offerLetterRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final DeviceRgb BRAND_TEAL = new DeviceRgb(13, 148, 136);

    // ── Create + Generate PDF ────────────────────────────────────────────────
    public OfferLetterResponse create(OfferLetterRequest req) throws IOException {
        Long userId = TenantContext.getUserId();

        // Validation — all NOT NULL fields must be non-null
        if (req.getCandidateName() == null || req.getCandidateName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidateName is required");
        if (req.getCandidateEmail() == null || req.getCandidateEmail().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidateEmail is required");
        if (req.getDesignation() == null || req.getDesignation().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "designation is required");
        if (req.getSalary() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salary is required");
        if (req.getJoiningDate() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "joiningDate is required");

        // Build letter number BEFORE first save (letter_number is NOT NULL in DB)
        long existingCount = offerLetterRepository.count();
        String letterNumber = String.format("OL-%d-%03d", LocalDate.now().getYear(), existingCount + 1);

        // Check uniqueness in case of race condition
        while (offerLetterRepository.findByLetterNumber(letterNumber).isPresent()) {
            existingCount++;
            letterNumber = String.format("OL-%d-%03d", LocalDate.now().getYear(), existingCount + 1);
        }

        OfferLetter ol = new OfferLetter();
        ol.setLetterNumber(letterNumber); // NOT NULL — set before save
        ol.setCandidateName(req.getCandidateName()); // NOT NULL
        ol.setCandidateEmail(req.getCandidateEmail()); // NOT NULL
        ol.setDesignation(req.getDesignation()); // NOT NULL
        ol.setSalary(req.getSalary()); // NOT NULL
        ol.setJoiningDate(req.getJoiningDate()); // NOT NULL
        ol.setDepartment(req.getDepartment() != null ? req.getDepartment() : "");
        ol.setLocation(req.getLocation() != null ? req.getLocation() : "");
        ol.setGeneratedBy(userId);
        ol.setStatus("GENERATED");

        // Template content stores extra info not mapped to columns
        String reportingManager = req.getReportingManager() != null ? req.getReportingManager() : "HR Manager";
        String workingHours = req.getWorkingHours() != null ? req.getWorkingHours() : "9 hours / day, 5 days a week";
        int probationMonths = req.getProbationMonths() != null ? req.getProbationMonths() : 6;
        ol.setTemplateContent(String.format(
                "reportingManager=%s|workingHours=%s|probationMonths=%d", reportingManager, workingHours,
                probationMonths));

        log.info(
                "[OfferLetter] Creating offer letter: letterNumber={}, candidate={}, designation={}, salary={}, joining={}",
                letterNumber, req.getCandidateName(), req.getDesignation(), req.getSalary(), req.getJoiningDate());

        // Save record
        ol = offerLetterRepository.save(ol);
        log.info("[OfferLetter] Saved with id={}", ol.getId());

        // Generate PDF and store path
        try {
            String pdfPath = generatePdf(ol, reportingManager, workingHours, probationMonths);
            ol.setPdfUrl(pdfPath);
            ol.setStatus("GENERATED");
            ol = offerLetterRepository.save(ol);
            log.info("[OfferLetter] PDF generated at {}", pdfPath);
        } catch (Exception pdfEx) {
            // PDF failure should NOT delete the saved record — just log and continue
            log.error("[OfferLetter] PDF generation failed for id={}: {}", ol.getId(), pdfEx.getMessage(), pdfEx);
            // Save status as DRAFT if PDF failed
            ol.setStatus("DRAFT");
            ol = offerLetterRepository.save(ol);
        }

        return OfferLetterResponse.from(ol);
    }

    // ── List ────────────────────────────────────────────────────────────────
    public Page<OfferLetterResponse> list(int page, int size) {
        return offerLetterRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(OfferLetterResponse::from);
    }

    // ── Get PDF bytes for download ───────────────────────────────────────────
    public byte[] getPdfBytes(Long id) throws IOException {
        OfferLetter ol = offerLetterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer letter not found."));
        if (ol.getPdfUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF not generated for this offer letter.");
        }
        return Files.readAllBytes(Path.of(ol.getPdfUrl()));
    }

    // ── PDF Generation ───────────────────────────────────────────────────────
    private String generatePdf(OfferLetter ol, String reportingManager,
            String workingHours, int probationMonths) throws IOException {
        // Create directory — use flat uploads/offerletters since no company_id
        Path dir = Path.of(uploadDir, "offerletters");
        Files.createDirectories(dir);
        String fileName = "offer_" + ol.getId() + ".pdf";
        String fullPath = dir.resolve(fileName).toString();

        PdfWriter writer = new PdfWriter(fullPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(50, 60, 50, 60);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String today = LocalDate.now().format(fmt);
        String joiningFmt = ol.getJoiningDate() != null ? ol.getJoiningDate().format(fmt) : "To be confirmed";

        // ── PAGE 1 ────────────────────────────────────────────────
        doc.add(new Paragraph("PRAVARTHANA TECHNOLOGIES PVT. LTD.")
                .setFont(bold).setFontSize(16).setFontColor(BRAND_TEAL)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Human Resources Department")
                .setFont(normal).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
                .setMarginTop(5).setMarginBottom(15));

        Table metaTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
        metaTable.addCell(cell(bold, 10, "Ref: " + ol.getLetterNumber(), TextAlignment.LEFT));
        metaTable.addCell(cell(normal, 10, "Date: " + today, TextAlignment.RIGHT));
        doc.add(metaTable);
        doc.add(new Paragraph("\n"));

        doc.add(new Paragraph("To,").setFont(normal).setFontSize(11));
        doc.add(new Paragraph(ol.getCandidateName()).setFont(bold).setFontSize(12));
        doc.add(new Paragraph("\n"));

        doc.add(new Paragraph("Subject: Offer of Employment - " + ol.getDesignation())
                .setFont(bold).setFontSize(11).setFontColor(BRAND_TEAL));

        doc.add(new Paragraph(
                "Dear " + ol.getCandidateName() + ",\n\n" +
                        "We are pleased to offer you the position of " + ol.getDesignation() +
                        " in the " + (ol.getDepartment() != null && !ol.getDepartment().isEmpty()
                                ? ol.getDepartment()
                                : "our")
                        + " department at Pravarthana Technologies Pvt. Ltd. " +
                        "After reviewing your profile and interview performance, we are confident that you will be a " +
                        "valuable addition to our team.\n\n" +
                        "Your employment is subject to the terms and conditions outlined in this letter.")
                .setFont(normal).setFontSize(10.5f).setMultipliedLeading(1.5f));

        doc.add(new Paragraph("\nKey Details").setFont(bold).setFontSize(11).setFontColor(BRAND_TEAL));
        Table details = new Table(UnitValue.createPercentArray(new float[] { 35, 65 })).useAllAvailableWidth();
        addRow(details, bold, normal, "Position", ol.getDesignation());
        addRow(details, bold, normal, "Department", ol.getDepartment() != null ? ol.getDepartment() : "—");
        addRow(details, bold, normal, "Date of Joining", joiningFmt);
        addRow(details, bold, normal, "Reporting Manager", reportingManager);
        addRow(details, bold, normal, "Working Hours", workingHours);
        addRow(details, bold, normal, "Probation Period", probationMonths + " months");
        doc.add(details);
        doc.add(new Paragraph("\n"));

        // CTC Annexure — use 'salary' field (no ₹ symbol — Helvetica doesn't support
        // it)
        doc.add(new Paragraph("Annexure A - CTC Breakdown (Annual)").setFont(bold).setFontSize(11)
                .setFontColor(BRAND_TEAL));
        doc.add(ctcTable(ol.getSalary(), bold, normal));
        doc.add(new Paragraph("\n"));

        // ── PAGE 2 — TERMS ────────────────────────────────────────
        pdf.addNewPage();
        doc.add(new Paragraph("Terms & Conditions of Employment")
                .setFont(bold).setFontSize(14).setFontColor(BRAND_TEAL).setTextAlignment(TextAlignment.CENTER));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)).setMarginTop(5)
                .setMarginBottom(15));
        String[] terms = {
                "1. PROBATION: Your employment will commence with a probationary period of " + probationMonths
                        + " months.",
                "2. CONFIDENTIALITY: You agree to maintain strict confidentiality of all company information.",
                "3. CODE OF CONDUCT: You are expected to adhere to the company's Code of Conduct and all HR policies.",
                "4. LEAVE POLICY: You are entitled to leave as per the company's Leave Policy.",
                "5. INTELLECTUAL PROPERTY: All work product created during employment shall remain the property of Pravarthana Technologies Pvt. Ltd.",
                "6. NOTICE PERIOD: During probation, 15 days' notice. After confirmation, 30-day notice period applies.",
                "7. BACKGROUND VERIFICATION: Your offer is conditional upon successful background verification.",
                "8. GOVERNING LAW: This offer is governed by the laws of India.",
                "9. OUTSIDE EMPLOYMENT: You shall not take up any other employment without prior written consent.",
                "10. COMPANY PROPERTY: Any company assets must be returned upon separation."
        };
        for (String term : terms) {
            doc.add(new Paragraph(term).setFont(normal).setFontSize(10).setMultipliedLeading(1.5f).setMarginBottom(6));
        }

        // ── PAGE 3 — ACCEPTANCE ───────────────────────────────────
        pdf.addNewPage();
        doc.add(new Paragraph("Acceptance of Offer")
                .setFont(bold).setFontSize(14).setFontColor(BRAND_TEAL).setTextAlignment(TextAlignment.CENTER));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)).setMarginTop(5)
                .setMarginBottom(20));

        doc.add(new Paragraph(
                "Please sign and return one copy of this letter by " +
                        LocalDate.now().plusDays(7).format(fmt) +
                        " to confirm your acceptance of this offer. This offer is valid for 7 days from the date of issuance.")
                .setFont(normal).setFontSize(10.5f).setMultipliedLeading(1.5f));

        doc.add(new Paragraph("\n\n\n"));
        Table sigTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
        Cell sigL = new Cell().add(
                new Paragraph("____________________________\n" + ol.getCandidateName() + "\nCandidate Signature & Date")
                        .setFont(normal).setFontSize(10))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        Cell sigR = new Cell().add(
                new Paragraph("____________________________\nAuthorised Signatory\nPravarthana Technologies Pvt. Ltd.")
                        .setFont(normal).setFontSize(10))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        sigTable.addCell(sigL);
        sigTable.addCell(sigR);
        doc.add(sigTable);

        doc.close();
        return fullPath;
    }

    // ── CTC breakdown table ──────────────────────────────────────────────────
    private Table ctcTable(BigDecimal annualCTC, PdfFont bold, PdfFont normal) {
        BigDecimal monthly = annualCTC.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal basic = annualCTC.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hra = annualCTC.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal special = annualCTC.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pfEmp = annualCTC.multiply(new BigDecimal("0.0481")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pfEmpl = annualCTC.multiply(new BigDecimal("0.0481")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pt = new BigDecimal("2400.00");
        BigDecimal gross = basic.add(hra).add(special);
        BigDecimal netMonthly = monthly
                .subtract(pfEmpl.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .subtract(pt.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));

        Table t = new Table(UnitValue.createPercentArray(new float[] { 50, 25, 25 })).useAllAvailableWidth();
        t.addHeaderCell(hCell(bold, "Component", BRAND_TEAL));
        t.addHeaderCell(hCell(bold, "Annual (INR)", BRAND_TEAL)); // no rupee symbol — Helvetica doesn't support it
        t.addHeaderCell(hCell(bold, "Monthly (INR)", BRAND_TEAL));

        addCtcRow(t, normal, "Basic Salary (40%)", basic,
                basic.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        addCtcRow(t, normal, "HRA (20%)", hra, hra.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        addCtcRow(t, normal, "Special Allowance (30%)", special,
                special.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        addCtcRow(t, normal, "Gross Salary", gross, gross.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        addCtcRow(t, normal, "PF Employer (4.81%)", pfEmp,
                pfEmp.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        addCtcRow(t, bold, "Total CTC", annualCTC, monthly);
        addCtcRow(t, normal, "PF Employee Deduction", pfEmpl.negate(),
                pfEmpl.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP).negate());
        addCtcRow(t, normal, "Professional Tax (annual)", pt.negate(),
                pt.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP).negate());
        addCtcRow(t, bold, "Estimated Take-Home (Net)", annualCTC.subtract(pfEmpl).subtract(pt), netMonthly);

        return t;
    }

    // ── Cell helpers ─────────────────────────────────────────────────────────
    private Cell cell(PdfFont font, float size, String text, TextAlignment align) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(size).setTextAlignment(align))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(2);
    }

    private void addRow(Table t, PdfFont bold, PdfFont normal, String label, String value) {
        t.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(10))
                .setBackgroundColor(new DeviceRgb(245, 245, 245)));
        t.addCell(new Cell().add(new Paragraph(value != null ? value : "—").setFont(normal).setFontSize(10)));
    }

    private Cell hCell(PdfFont bold, String text, DeviceRgb bg) {
        return new Cell().add(new Paragraph(text).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(bg).setPadding(5);
    }

    private void addCtcRow(Table t, PdfFont font, String label, BigDecimal annual, BigDecimal monthly) {
        t.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(9.5f)));
        t.addCell(new Cell()
                .add(new Paragraph(fmt(annual)).setFont(font).setFontSize(9.5f).setTextAlignment(TextAlignment.RIGHT)));
        t.addCell(new Cell().add(
                new Paragraph(fmt(monthly)).setFont(font).setFontSize(9.5f).setTextAlignment(TextAlignment.RIGHT)));
    }

    private String fmt(BigDecimal v) {
        if (v == null)
            return "-";
        return String.format("INR %,.2f", v); // avoid ₹ — not in Helvetica font
    }
}
