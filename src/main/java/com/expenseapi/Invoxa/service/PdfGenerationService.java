package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.model.Invoice;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class PdfGenerationService {

    private static final String PDF_DIRECTORY = "invoices/pdfs/";

    public String generateInvoicePdf(Invoice invoice) {
        try {
            Path directory = Paths.get(PDF_DIRECTORY);
            Files.createDirectories(directory);

            String fileName = PDF_DIRECTORY + "invoice-" + invoice.getInvoiceNumber() + ".pdf";

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            document.add(new Paragraph("INVOICE", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber(), headerFont));
            document.add(new Paragraph("Client: " + invoice.getClient().getName(), normalFont));
            document.add(new Paragraph("Due Date: " + invoice.getDueDate(), normalFont));
            document.add(new Paragraph("Status: " + invoice.getStatus(), normalFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Line Items:", headerFont));

            for (var item : invoice.getLineItems()) {
                document.add(new Paragraph(
                        item.getDescription() + " x" + item.getQuantity() +
                                " @ " + item.getUnitPrice() + " = " + item.getAmount(),
                        normalFont
                ));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total: " + invoice.getTotalAmount(), headerFont));

            if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Notes: " + invoice.getNotes(), normalFont));
            }

            document.close();
            log.info("PDF generated for invoice: {}", invoice.getInvoiceNumber());
            return fileName;

        } catch (IOException e) {
            log.error("Failed to generate PDF for invoice: {}", invoice.getInvoiceNumber(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}