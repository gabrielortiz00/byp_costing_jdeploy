package com.github.gabrielortiz00.byp_costing_jdeploy.utils;

import com.github.gabrielortiz00.byp_costing_jdeploy.ResultsModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfExportUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** utility class for exporting results as pdf **/
    public static void exportResultsToPdf(ResultsModel model, String filePath) throws IOException {
        //create document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        //new content stream for adding content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        //fonts
        PDFont standardFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont italicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        //positioning constants
        float margin = 50;
        float pageWidth = page.getMediaBox().getWidth();
        float contentWidth = pageWidth - (2 * margin);
        float yStart = page.getMediaBox().getHeight() - margin;
        float yPosition = yStart;
        float lineHeight = 15;

        //load logo
        PDImageXObject logo = null;
        try {
            InputStream logoStream = PdfExportUtil.class.getResourceAsStream("/com/ia/bypcosting/files/LOGO.jpeg");
            if (logoStream != null) {
                //read input stream into byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = logoStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                logoStream.close();

                //image object
                logo = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "BYP Logo");
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }

        //logo dimensions
        float logoWidth = 80;
        float logoHeight = logo.getHeight() * (logoWidth / logo.getWidth());

        //center logo
        float logoX = margin + (contentWidth - logoWidth) / 2;

        //draw logo
        contentStream.drawImage(logo, logoX, yStart - logoHeight + 10, logoWidth, logoHeight);

        //title
        contentStream.beginText();
        contentStream.setFont(boldFont, 18);
        String title = "COST ANALYSIS RESULTS";
        float titleWidth = boldFont.getStringWidth(title) / 1000 * 18;
        float titleX = margin + (contentWidth - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yStart - logoHeight - 15);
        contentStream.showText(title);
        contentStream.endText();

        //move y-position down
        yPosition = yStart - logoHeight - 15 - lineHeight * 2;

        //date created
        contentStream.beginText();
        contentStream.setFont(italicFont, 10);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER));
        contentStream.endText();
        yPosition -= lineHeight * 2;

        //project info title
        yPosition = drawSection(contentStream, "PROJECT INFORMATION", boldFont, margin, yPosition, lineHeight);

        //project info data
        float sectionIndent = 10;
        float labelWidth = 120;
        yPosition = addTableRow(contentStream, "Project Name:", model.getProjectName(), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Product Name:", model.getProductName(), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Created At:", model.getCreatedAt().format(DATE_FORMATTER), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition -= lineHeight;

        //input parameter section
        yPosition = drawSection(contentStream, "INPUT PARAMETERS", boldFont, margin, yPosition, lineHeight);

        yPosition = addTableRow(contentStream, "Unit Cost (C):", "$" + CURRENCY_FORMAT.format(model.getUnitCost()) + " USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Incremental Cost (I):", "$" + CURRENCY_FORMAT.format(model.getIncrementalCost()) + " USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Shipping Cost (L):", "$" + CURRENCY_FORMAT.format(model.getShippingCost()) + " USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Units Per Container (Q):", String.valueOf(model.getUnitsPerContainer()), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Tax Rate (T):", PERCENT_FORMAT.format(model.getTaxRate()), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Exchange Rate (R):", CURRENCY_FORMAT.format(model.getExchangeRate()) + " MXN/USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Exchange Buffer (B):", PERCENT_FORMAT.format(model.getExchangeBuffer()), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition -= lineHeight;

        //results section
        yPosition = drawSection(contentStream, "CALCULATION RESULTS", boldFont, margin, yPosition, lineHeight);

        yPosition = addTableRow(contentStream, "Container Cost USD:", "$" + CURRENCY_FORMAT.format(model.getContainerCostUsd()) + " USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Container Cost with Tax:", "$" + CURRENCY_FORMAT.format(model.getContainerCostWithTax()) + " USD", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Container Cost MXN:", "$" + CURRENCY_FORMAT.format(model.getContainerCostMxn()) + " MXN", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        yPosition = addTableRow(contentStream, "Cost per Unit MXN:", "$" + CURRENCY_FORMAT.format(model.getCostPerUnitMxn()) + " MXN", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);

        //highlight desired value
        if (model.isCalculatedFromMargin()) {
            // price from margin
            yPosition = addTableRow(contentStream, "Selling Price (P):", "$" + CURRENCY_FORMAT.format(model.getSellingPrice()) + " MXN (CALCULATED)", boldFont, boldFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
            yPosition = addTableRow(contentStream, "Profit Margin (M):", PERCENT_FORMAT.format(model.getProfitMargin()), boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        } else {
            //margin from price
            yPosition = addTableRow(contentStream, "Selling Price (P):", "$" + CURRENCY_FORMAT.format(model.getSellingPrice()) + " MXN", boldFont, standardFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
            yPosition = addTableRow(contentStream, "Profit Margin (M):", PERCENT_FORMAT.format(model.getProfitMargin()) + " (CALCULATED)", boldFont, boldFont, margin + sectionIndent, yPosition, labelWidth, lineHeight);
        }

        //calculation type
        String calculationType = model.isCalculatedFromMargin() ? "Calculation Type: From Margin to Price" : "Calculation Type: From Price to Margin";
        contentStream.beginText();
        contentStream.setFont(italicFont, 10);
        contentStream.newLineAtOffset(margin + sectionIndent, yPosition - lineHeight);
        contentStream.showText(calculationType);
        contentStream.endText();
        yPosition -= lineHeight * 2;

        //breakdown chart
        yPosition = drawSection(contentStream, "COST BREAKDOWN", boldFont, margin, yPosition, lineHeight);
        yPosition = drawCostBreakdownChart(contentStream, model, margin + sectionIndent, yPosition, contentWidth - sectionIndent * 2, boldFont, standardFont, lineHeight);

        //footer
        contentStream.beginText();
        contentStream.setFont(boldFont, 10);
        contentStream.newLineAtOffset(margin, 30);
        contentStream.showText("BYP Costing Program © " + LocalDate.now().getYear());
        contentStream.endText();

        //close and save
        contentStream.close();
        document.save(filePath);
        document.close();
    }


    private static float drawSection(PDPageContentStream contentStream, String title, PDFont font, float x, float y, float lineHeight) throws IOException {
        //draw section title
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(title);
        contentStream.endText();

        //find line position
        float lineY = y - (lineHeight * 0.3f);

        //draw underline
        contentStream.setLineWidth(1.0f);
        contentStream.moveTo(x, lineY);
        contentStream.lineTo(x + 200, lineY);
        contentStream.stroke();

        return y - lineHeight * 1.2f;
    }


    private static float addTableRow(PDPageContentStream contentStream, String label, String value, PDFont labelFont, PDFont valueFont, float x, float y,
                                     float labelWidth, float lineHeight) throws IOException {
        //label
        contentStream.beginText();
        contentStream.setFont(labelFont, 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        //value
        contentStream.beginText();
        contentStream.setFont(valueFont, 10);
        contentStream.newLineAtOffset(x + labelWidth, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - lineHeight;
    }


    private static float drawCostBreakdownChart(PDPageContentStream contentStream, ResultsModel model, float x, float y, float width, PDFont boldFont,
                                                PDFont regularFont, float lineHeight) throws IOException {

        //compute cost components
        double unitCostTotal = model.getUnitCost() * model.getUnitsPerContainer();
        double incrementalCostTotal = model.getIncrementalCost() * model.getUnitsPerContainer();
        double shippingCost = model.getShippingCost();
        double baseCostUsd = unitCostTotal + incrementalCostTotal + shippingCost;
        double taxAmount = baseCostUsd * model.getTaxRate();

        double totalCost = baseCostUsd + taxAmount;

        //compute percentages
        double unitCostPerc = unitCostTotal / totalCost;
        double incrementalCostPerc = incrementalCostTotal / totalCost;
        double shippingCostPerc = shippingCost / totalCost;
        double taxPerc = taxAmount / totalCost;

        //chart positioning
        float chartHeight = 130;
        float barHeight = 20;
        float barSpacing = 25; // Space between bars
        float valueWidth = 150; // Width for value labels
        float chartWidth = width - valueWidth;

        //draw chart title
        contentStream.beginText();
        contentStream.setFont(boldFont, 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText("Container Cost Components (USD)");
        contentStream.endText();
        y -= lineHeight * 1.5f;

        //total cost displayed
        contentStream.beginText();
        contentStream.setFont(boldFont, 9);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText("Total: $" + CURRENCY_FORMAT.format(totalCost) + " USD");
        contentStream.endText();

        //bar positioning
        float unitCostY = y - barHeight - barSpacing;
        float incrementalCostY = unitCostY - barHeight - barSpacing;
        float shippingCostY = incrementalCostY - barHeight - barSpacing;
        float taxY = shippingCostY - barHeight - barSpacing;

        //unit cost bar
        float unitCostBarWidth = (float) (chartWidth * unitCostPerc);
        contentStream.setNonStrokingColor(0.2f, 0.2f, 0.5f); // Dark blue
        contentStream.addRect(x, unitCostY, unitCostBarWidth, barHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x, unitCostY + barHeight + 5);
        contentStream.showText("Unit Cost");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x + unitCostBarWidth + 10, unitCostY + 5);
        contentStream.showText("$" + CURRENCY_FORMAT.format(unitCostTotal) + " (" + PERCENT_FORMAT.format(unitCostPerc) + ")");
        contentStream.endText();

        //incremental cost bar
        float incrementalCostBarWidth = (float) (chartWidth * incrementalCostPerc);
        contentStream.setNonStrokingColor(0.5f, 0.2f, 0.2f); // Dark red
        contentStream.addRect(x, incrementalCostY, incrementalCostBarWidth, barHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x, incrementalCostY + barHeight + 5);
        contentStream.showText("Incremental Cost");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x + incrementalCostBarWidth + 10, incrementalCostY + 5);
        contentStream.showText("$" + CURRENCY_FORMAT.format(incrementalCostTotal) + " (" + PERCENT_FORMAT.format(incrementalCostPerc) + ")");
        contentStream.endText();

        //shipping cost bar
        float shippingCostBarWidth = (float) (chartWidth * shippingCostPerc);
        contentStream.setNonStrokingColor(0.2f, 0.5f, 0.2f); // Dark green
        contentStream.addRect(x, shippingCostY, shippingCostBarWidth, barHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x, shippingCostY + barHeight + 5);
        contentStream.showText("Shipping Cost");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x + shippingCostBarWidth + 10, shippingCostY + 5);
        contentStream.showText("$" + CURRENCY_FORMAT.format(shippingCost) + " (" + PERCENT_FORMAT.format(shippingCostPerc) + ")");
        contentStream.endText();

        //tax bar
        float taxBarWidth = (float) (chartWidth * taxPerc);
        contentStream.setNonStrokingColor(0.8f, 0.4f, 0.0f); // Orange
        contentStream.addRect(x, taxY, taxBarWidth, barHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x, taxY + barHeight + 5);
        contentStream.showText("Import Tax");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(regularFont, 9);
        contentStream.newLineAtOffset(x + taxBarWidth + 10, taxY + 5);
        contentStream.showText("$" + CURRENCY_FORMAT.format(taxAmount) + " (" + PERCENT_FORMAT.format(taxPerc) + ")");
        contentStream.endText();

        return taxY - barSpacing;
    }
}