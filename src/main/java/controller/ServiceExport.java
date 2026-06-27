package controller;

// ── iText 5 — PDF ─────────────────────────────────────────────────────────────
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ── Apache POI — Excel ────────────────────────────────────────────────────────
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ── Java standard ─────────────────────────────────────────────────────────────
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ServiceExport — Exporte un tableau (DefaultTableModel) en PDF ou Excel.
 *
 * PDF  : genere un vrai fichier .pdf avec iText 5.
 * Excel: genere un vrai fichier .xlsx avec Apache POI.
 *
 * Les deux bibliotheques sont declarees dans le pom.xml et seront
 * telechargees automatiquement par Maven lors du premier build.
 */
public class ServiceExport {

    // Couleur verte de la clinique
    private static final BaseColor VERT_PDF   = new BaseColor(0x00, 0x6B, 0x5F);
    private static final BaseColor BLANC_PDF  = BaseColor.WHITE;
    private static final BaseColor GRIS_PDF   = new BaseColor(0xF2, 0xF2, 0xF2);

    private static final java.awt.Color VERT_EXCEL = new java.awt.Color(0x00, 0x6B, 0x5F);
    private static final java.awt.Color GRIS_EXCEL = new java.awt.Color(0xF2, 0xF2, 0xF2);

    // ── EXPORT PDF ────────────────────────────────────────────────────────────
    /**
     * Exporte le tableau en fichier PDF via iText 5.
     */
    public static void exporterPDF(JComponent parent, DefaultTableModel modele,
                                    String titre, String nomFichier) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer en PDF");
        chooser.setSelectedFile(new File(nomFichier + "_" + horodatage() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File fichier = chooser.getSelectedFile();
        if (!fichier.getName().toLowerCase().endsWith(".pdf")) {
            fichier = new File(fichier.getAbsolutePath() + ".pdf");
        }

        // Orientation paysage si beaucoup de colonnes
        boolean paysage = modele.getColumnCount() > 5;
        com.itextpdf.text.Rectangle taille = paysage
            ? PageSize.A4.rotate() : PageSize.A4;

        Document doc = new Document(taille, 25, 25, 40, 30);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fichier));
            doc.open();

            // ── En-tete du document ───────────────────────────────────────────
            Font fontEnteteDoc = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 16, VERT_PDF);
            Font fontSousTitre = FontFactory.getFont(
                FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);

            Paragraph pTitre = new Paragraph("DoctoVite — " + titre, fontEnteteDoc);
            pTitre.setAlignment(Element.ALIGN_CENTER);
            pTitre.setSpacingAfter(4);
            doc.add(pTitre);

            String dateExport = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm"));
            Paragraph pDate = new Paragraph(
                "Exporte le " + dateExport + "   —   " + modele.getRowCount() + " enregistrement(s)",
                fontSousTitre);
            pDate.setAlignment(Element.ALIGN_CENTER);
            pDate.setSpacingAfter(14);
            doc.add(pDate);

            // ── Tableau ───────────────────────────────────────────────────────
            int nbCols = modele.getColumnCount();
            PdfPTable table = new PdfPTable(nbCols);
            table.setWidthPercentage(100);

            // En-tetes colonnes
            Font fontColTitre = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 8, BLANC_PDF);
            for (int c = 0; c < nbCols; c++) {
                PdfPCell cell = new PdfPCell(
                    new Phrase(modele.getColumnName(c), fontColTitre));
                cell.setBackgroundColor(VERT_PDF);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Lignes de donnees
            Font fontData = FontFactory.getFont(
                FontFactory.HELVETICA, 8, BaseColor.DARK_GRAY);
            for (int r = 0; r < modele.getRowCount(); r++) {
                BaseColor bgLigne = (r % 2 == 0) ? BLANC_PDF : GRIS_PDF;
                for (int c = 0; c < nbCols; c++) {
                    Object val = modele.getValueAt(r, c);
                    String texte = (val != null) ? val.toString() : "";
                    // Tronquer les textes trop longs
                    if (texte.length() > 55) texte = texte.substring(0, 52) + "...";
                    PdfPCell cell = new PdfPCell(new Phrase(texte, fontData));
                    cell.setBackgroundColor(bgLigne);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            doc.add(table);

            // ── Pied du document ──────────────────────────────────────────────
            Font fontPied = FontFactory.getFont(
                FontFactory.HELVETICA_OBLIQUE, 7, BaseColor.GRAY);
            Paragraph pied = new Paragraph(
                "\nDoctoVite © 2026  —  " + modele.getRowCount() + " ligne(s) exportee(s)",
                fontPied);
            pied.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pied);

            doc.close();

            // Ouvrir le PDF automatiquement
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichier);
            }

            JOptionPane.showMessageDialog(parent,
                "PDF exporte avec succes !\n" + fichier.getAbsolutePath(),
                "Export PDF", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            if (doc.isOpen()) doc.close();
            JOptionPane.showMessageDialog(parent,
                "Erreur lors de l'export PDF :\n" + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ── EXPORT EXCEL ──────────────────────────────────────────────────────────
    /**
     * Exporte le tableau en fichier Excel (.xlsx) via Apache POI.
     */
    public static void exporterExcel(JComponent parent, DefaultTableModel modele,
                                      String titre, String nomFichier) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer en Excel");
        chooser.setSelectedFile(new File(nomFichier + "_" + horodatage() + ".xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers Excel (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File fichier = chooser.getSelectedFile();
        if (!fichier.getName().toLowerCase().endsWith(".xlsx")) {
            fichier = new File(fichier.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {

            // Nom de l'onglet (max 31 caracteres dans Excel)
            String nomOnglet = titre.length() > 31 ? titre.substring(0, 31) : titre;
            Sheet sheet = workbook.createSheet(nomOnglet);

            // ── Styles ────────────────────────────────────────────────────────
            // Style titre document
            org.apache.poi.ss.usermodel.Font fontTitre = workbook.createFont();
            fontTitre.setBold(true);
            fontTitre.setFontHeightInPoints((short) 14);
            CellStyle styleTitre = workbook.createCellStyle();
            styleTitre.setFont(fontTitre);

            // Style sous-titre (date)
            org.apache.poi.ss.usermodel.Font fontMeta = workbook.createFont();
            fontMeta.setItalic(true);
            fontMeta.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle styleMeta = workbook.createCellStyle();
            styleMeta.setFont(fontMeta);

            // Style en-tete de colonne (vert + blanc + gras)
            org.apache.poi.ss.usermodel.Font fontEntete = workbook.createFont();
            fontEntete.setBold(true);
            fontEntete.setColor(IndexedColors.WHITE.getIndex());
            CellStyle styleEntete = workbook.createCellStyle();
            styleEntete.setFont(fontEntete);
            styleEntete.setFillForegroundColor(
                new XSSFColor(VERT_EXCEL, null));
            styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleEntete.setAlignment(HorizontalAlignment.CENTER);
            styleEntete.setBorderBottom(BorderStyle.THIN);
            styleEntete.setBorderTop(BorderStyle.THIN);
            styleEntete.setBorderLeft(BorderStyle.THIN);
            styleEntete.setBorderRight(BorderStyle.THIN);

            // Style ligne paire (blanc + bordure)
            CellStyle styleBloc = workbook.createCellStyle();
            styleBloc.setBorderBottom(BorderStyle.THIN);
            styleBloc.setBorderTop(BorderStyle.THIN);
            styleBloc.setBorderLeft(BorderStyle.THIN);
            styleBloc.setBorderRight(BorderStyle.THIN);
            styleBloc.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleBloc.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleBloc.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleBloc.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            // Style ligne impaire (gris clair + bordure)
            CellStyle styleGris = workbook.createCellStyle();
            styleGris.cloneStyleFrom(styleBloc);
            styleGris.setFillForegroundColor(
                new XSSFColor(GRIS_EXCEL, null));
            styleGris.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style total
            org.apache.poi.ss.usermodel.Font fontTotal = workbook.createFont();
            fontTotal.setItalic(true);
            fontTotal.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle styleTotal = workbook.createCellStyle();
            styleTotal.setFont(fontTotal);

            // ── Ligne 0 : Titre du document ───────────────────────────────────
            Row rowTitre = sheet.createRow(0);
            rowTitre.setHeightInPoints(22);
            Cell cellTitre = rowTitre.createCell(0);
            cellTitre.setCellValue("DoctoVite — " + titre);
            cellTitre.setCellStyle(styleTitre);

            // ── Ligne 1 : Date d'export ───────────────────────────────────────
            Row rowDate = sheet.createRow(1);
            Cell cellDate = rowDate.createCell(0);
            cellDate.setCellValue("Exporte le " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            cellDate.setCellStyle(styleMeta);

            // ── Ligne 2 : vide ────────────────────────────────────────────────
            sheet.createRow(2);

            // ── Ligne 3 : En-tetes de colonnes ───────────────────────────────
            int nbCols = modele.getColumnCount();
            Row rowEntete = sheet.createRow(3);
            rowEntete.setHeightInPoints(18);
            for (int c = 0; c < nbCols; c++) {
                Cell cell = rowEntete.createCell(c);
                cell.setCellValue(modele.getColumnName(c));
                cell.setCellStyle(styleEntete);
            }

            // ── Lignes de donnees (a partir de la ligne 4) ────────────────────
            for (int r = 0; r < modele.getRowCount(); r++) {
                Row row = sheet.createRow(r + 4);
                row.setHeightInPoints(16);
                CellStyle styleLigne = (r % 2 == 0) ? styleBloc : styleGris;
                for (int c = 0; c < nbCols; c++) {
                    Object val = modele.getValueAt(r, c);
                    Cell cell = row.createCell(c);
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(styleLigne);
                }
            }

            // ── Ajustement automatique de la largeur des colonnes ─────────────
            for (int c = 0; c < nbCols; c++) {
                sheet.autoSizeColumn(c);
                // Largeur maximale : 50 caracteres (~12 000 unites)
                if (sheet.getColumnWidth(c) > 14000) {
                    sheet.setColumnWidth(c, 14000);
                }
                // Largeur minimale : 8 caracteres
                if (sheet.getColumnWidth(c) < 2000) {
                    sheet.setColumnWidth(c, 2000);
                }
            }

            // ── Figer la ligne d'en-tete ──────────────────────────────────────
            sheet.createFreezePane(0, 4);

            // ── Ligne de total ────────────────────────────────────────────────
            int ligneTotal = modele.getRowCount() + 5;
            Row rowTotal = sheet.createRow(ligneTotal);
            Cell cellTotal = rowTotal.createCell(0);
            cellTotal.setCellValue("Total : " + modele.getRowCount() + " enregistrement(s)");
            cellTotal.setCellStyle(styleTotal);

            // ── Ecrire le fichier ─────────────────────────────────────────────
            try (FileOutputStream fos = new FileOutputStream(fichier)) {
                workbook.write(fos);
            }

            // Ouvrir le fichier Excel automatiquement
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichier);
            }

            JOptionPane.showMessageDialog(parent,
                "Excel exporte avec succes !\n" + fichier.getAbsolutePath(),
                "Export Excel", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Erreur lors de l'export Excel :\n" + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ── Horodatage pour le nom du fichier ─────────────────────────────────────
    private static String horodatage() {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
    }
}
