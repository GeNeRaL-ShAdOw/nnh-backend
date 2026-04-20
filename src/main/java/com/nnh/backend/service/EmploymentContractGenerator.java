package com.nnh.backend.service;

import com.nnh.backend.persistence.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class EmploymentContractGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final float MARGIN = 60f;
    private static final float LINE_HEIGHT = 18f;

    public byte[] generate(Employee employee) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font italic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            float width = page.getMediaBox().getWidth() - 2 * MARGIN;
            float y     = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // Header
                y = writeLine(cs, bold, 18, "NANDA NURSING HOME", MARGIN, y, width);
                y = writeLine(cs, regular, 9, "A5, Jagriti Vihar Lane 2, Badripur, Dehradun, Uttarakhand 248005", MARGIN, y - 4, width);
                y = writeLine(cs, regular, 9, "care@nandanursinghome.in  |  +91 89378 53000", MARGIN, y, width);

                y -= 10;
                drawLine(cs, MARGIN, y, MARGIN + width);
                y -= 16;

                // Title
                y = writeCentered(cs, bold, 14, "LETTER OF APPOINTMENT", MARGIN, y, page.getMediaBox().getWidth());
                y -= 16;

                // Date
                y = writeLine(cs, regular, 10, "Date: " + LocalDate.now().format(FMT), MARGIN, y, width);
                y -= 8;

                // Recipient
                y = writeLine(cs, bold, 10, employee.getName(), MARGIN, y, width);
                y = writeLine(cs, regular, 10, "Employee ID: " + employee.getId(), MARGIN, y, width);
                y = writeLine(cs, regular, 10, employee.getEmail(), MARGIN, y, width);
                y -= 14;

                // Salutation
                y = writeLine(cs, regular, 10, "Dear " + employee.getName().split(" ")[0] + ",", MARGIN, y, width);
                y -= 8;

                // Body
                y = writeWrapped(cs, regular, 10, "We are pleased to offer you the position of Care Staff at Nanda Nursing Home, Dehradun, with effect from " + LocalDate.now().format(FMT) + ". This letter serves as your official letter of appointment.", MARGIN, y, width);
                y -= 8;

                y = writeLine(cs, bold, 10, "Terms of Employment", MARGIN, y, width);
                y -= 4;

                String[][] terms = {
                    { "Employee ID",    employee.getId() },
                    { "Designation",    "Care Staff" },
                    { "Date of Joining", LocalDate.now().format(FMT) },
                    { "Place of Work",  "Nanda Nursing Home, Dehradun" },
                    { "Working Hours",  "As per clinic schedule and roster" },
                    { "Probation Period", "3 months from date of joining" },
                };
                for (String[] term : terms) {
                    y = writeLine(cs, regular, 10, "  \u2022  " + term[0] + ":  " + term[1], MARGIN, y, width);
                }
                y -= 10;

                y = writeWrapped(cs, regular, 10, "You are required to maintain strict confidentiality of all patient records and sensitive clinic information. Any breach of confidentiality may result in immediate termination.", MARGIN, y, width);
                y -= 8;

                y = writeWrapped(cs, regular, 10, "This appointment is subject to the rules and regulations of Nanda Nursing Home, which may be amended from time to time. You will be required to change your temporary system password upon first login.", MARGIN, y, width);
                y -= 14;

                y = writeLine(cs, regular, 10, "We look forward to your valuable contribution to our team.", MARGIN, y, width);
                y -= 20;

                // Signature block
                y = writeLine(cs, bold, 10, "Dr. Vaishnavi Purohit", MARGIN, y, width);
                y = writeLine(cs, italic, 10, "MBBS, DGO, DNB", MARGIN, y, width);
                y = writeLine(cs, regular, 10, "Director, Nanda Nursing Home", MARGIN, y, width);
                y -= 4;
                drawLine(cs, MARGIN, y, MARGIN + 160);

                // Footer
                float footerY = MARGIN - 10;
                writeLine(cs, italic, 8, "This is a system-generated document issued by Nanda Nursing Home.", MARGIN, footerY, width);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate employment contract PDF: {}", e.getMessage());
            return new byte[0];
        }
    }

    private float writeLine(PDPageContentStream cs, PDType1Font font, float size,
                             String text, float x, float y, float maxWidth) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text.length() * size * 0.6f > maxWidth ? text.substring(0, (int)(maxWidth / (size * 0.6f))) + "…" : text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private float writeCentered(PDPageContentStream cs, PDType1Font font, float size,
                                 String text, float margin, float y, float pageWidth) throws Exception {
        float textWidth = font.getStringWidth(text) / 1000 * size;
        float x = (pageWidth - textWidth) / 2;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private float writeWrapped(PDPageContentStream cs, PDType1Font font, float size,
                                String text, float x, float y, float maxWidth) throws Exception {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            float w = font.getStringWidth(test) / 1000 * size;
            if (w > maxWidth && !line.isEmpty()) {
                y = writeLine(cs, font, size, line.toString(), x, y, maxWidth);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) y = writeLine(cs, font, size, line.toString(), x, y, maxWidth);
        return y;
    }

    private void drawLine(PDPageContentStream cs, float x1, float y, float x2) throws Exception {
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }
}
