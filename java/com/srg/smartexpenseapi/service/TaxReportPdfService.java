package com.srg.smartexpenseapi.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.srg.smartexpenseapi.payload.response.TaxReportResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TaxReportPdfService {

    public byte[] generateTaxReportPdf(TaxReportResponse report) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Paragraph title = new Paragraph("SmartExpense Manager - Tax Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Paragraph subtitle = new Paragraph("FY 2025-26 Financial Assessment", subTitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Section 1: Summary Table
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10f);

        addTableHeader(summaryTable, "Key Metric", headerFont);
        addTableHeader(summaryTable, "Value", headerFont);

        addTableRow(summaryTable, "Recommended ITR Form", report.getItrType().split(" ")[0]);
        addTableRow(summaryTable, "Recommended Regime", report.getRecommendedRegime().replace("_", " "));
        addTableRow(summaryTable, "Net Taxable Income", "Rs. " + String.format("%,.2f", report.getNetTaxableIncome()));
        addTableRow(summaryTable, "Estimated Tax (New)", "Rs. " + String.format("%,.2f", report.getEstimatedTaxNewRegime()));
        addTableRow(summaryTable, "Estimated Tax (Old)", "Rs. " + String.format("%,.2f", report.getEstimatedTaxOldRegime()));

        document.add(summaryTable);

        // Section 2: Income Breakdown
        Paragraph incomeHeader = new Paragraph("\nIncome Breakdown", titleFont);
        incomeHeader.setSpacingBefore(20);
        document.add(incomeHeader);

        PdfPTable incomeTable = new PdfPTable(2);
        incomeTable.setWidthPercentage(100);
        incomeTable.setSpacingBefore(10f);

        addTableHeader(incomeTable, "Category", headerFont);
        addTableHeader(incomeTable, "Amount", headerFont);

        addTableRow(incomeTable, "Salary Income", "Rs. " + String.format("%,.2f", report.getTotalSalaryIncome()));
        addTableRow(incomeTable, "Business Income", "Rs. " + String.format("%,.2f", report.getTotalBusinessIncome()));
        addTableRow(incomeTable, "Capital Gains (Net)", "Rs. " + String.format("%,.2f", report.getTotalCapitalGains()));
        addTableRow(incomeTable, "Other Sources", "Rs. " + String.format("%,.2f", report.getTotalOtherIncome()));

        document.add(incomeTable);

        // Advisory
        Font advisoryFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.RED);
        Paragraph advisory = new Paragraph("\nNote: This is an estimated report. Please consult a CA for final filing.", advisoryFont);
        advisory.setSpacingBefore(30);
        document.add(advisory);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new Color(139, 92, 246)); // Theme Purple
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle, font));
        header.setPadding(8);
        table.addCell(header);
    }

    private void addTableRow(PdfPTable table, String key, String value) {
        table.addCell(key);
        table.addCell(value);
    }
}
