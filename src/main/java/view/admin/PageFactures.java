package view.admin;

import controller.GestionFacture;
import controller.GestionPatient;
import model.Facture;
import model.Patient;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;

// iText pour le PDF recu
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PageFactures extends JPanel {

    private Utilisateur utilisateur;
    private GestionFacture gestion = new GestionFacture();
    private DefaultTableModel modele;
    private JTable tableau;
    private List<Facture> listeFactures;
    private Runnable onMaj; // callback pour rafraichir le dashboard apres ajout

    public PageFactures(Utilisateur utilisateur) {
        this(utilisateur, null);
    }

    public PageFactures(Utilisateur utilisateur, Runnable onMaj) {
        this.utilisateur = utilisateur;
        this.onMaj = onMaj;
        setLayout(new BorderLayout(0, 14));
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        charger();
    }

    // Appelee automatiquement par la Sidebar a chaque navigation
    public void rafraichir() {
        charger();
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 12, 0));
        entete.add(Composants.titre("Factures & Paiements"), BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);

        JButton btnActu = Composants.boutonGris("Actualiser");
        btnActu.addActionListener(e -> charger());
        JButton btnNouv = Composants.boutonSucces("+ Nouvelle Facture");
        btnNouv.addActionListener(e -> ouvrirFormulaire());

        // Bouton PDF : imprime le recu de la facture selectionnee
        JButton btnPdf = Composants.boutonPDF("Imprimer Recu");
        btnPdf.addActionListener(e -> {
            int sel = tableau.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Selectionnez une facture pour imprimer son recu.",
                    "Aucune selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            imprimerRecu(listeFactures.get(sel));
        });

        JButton btnXlsx = Composants.boutonExcel("Excel");
        btnXlsx.addActionListener(e -> controller.ServiceExport.exporterExcel(
            this, modele, "Factures", "factures"));

        droite.add(btnActu);
        droite.add(btnNouv);
        droite.add(btnPdf);
        droite.add(btnXlsx);
        entete.add(droite, BorderLayout.EAST);

        String[] cols = {"N° Facture", "Date", "Patient", "Dossier",
                         "Total MAD", "Paye MAD", "Restant", "Mode", "Statut"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int lig, int col) {
                Component c = super.prepareRenderer(r, lig, col);
                if (!isRowSelected(lig)) {
                    Object v = getValueAt(lig, 8);
                    if (v != null) {
                        String s = v.toString();
                        if ("SOLDE".equals(s)) {
                            c.setBackground(new Color(0xE8, 0xF8, 0xF5));
                            c.setForeground(Couleurs.VERT_FONCE);
                        } else if ("PARTIEL".equals(s)) {
                            c.setBackground(new Color(0xFF, 0xF8, 0xE1));
                            c.setForeground(Couleurs.ORANGE);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    }
                }
                return c;
            }
        };
        Composants.styliserTableau(tableau);

        // Double-clic pour imprimer le recu
        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sel = tableau.getSelectedRow();
                    if (sel >= 0) imprimerRecu(listeFactures.get(sel));
                }
            }
        });

        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12));
        carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);

        JLabel aide = new JLabel("  Double-clic sur une facture pour imprimer son recu PDF");
        aide.setFont(Couleurs.PETIT);
        aide.setForeground(Couleurs.TEXTE_PALE);

        add(entete, BorderLayout.NORTH);
        add(carte, BorderLayout.CENTER);
        add(aide, BorderLayout.SOUTH);
    }

    private void charger() {
        if (modele == null) return;
        modele.setRowCount(0);
        listeFactures = gestion.listerTous();
        for (Facture f : listeFactures) {
            modele.addRow(new Object[]{
                f.getNumeroFacture(),
                f.getDateCree() != null ? f.getDateCree().substring(0, 10) : "-",
                f.getNomPatient(),
                f.getNumeroDossier() != null ? f.getNumeroDossier() : "-",
                String.format("%.2f", f.getMontantTotal()),
                String.format("%.2f", f.getMontantPaye()),
                String.format("%.2f", f.getMontantRestant()),
                f.getModePaiement(),
                f.getStatut()
            });
        }
    }

    // ── PDF RECU ──────────────────────────────────────────────────────────────
    private void imprimerRecu(Facture f) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer le recu");
        chooser.setSelectedFile(new File("Recu_" + f.getNumeroFacture() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File fichier = chooser.getSelectedFile();
        if (!fichier.getName().toLowerCase().endsWith(".pdf"))
            fichier = new File(fichier.getAbsolutePath() + ".pdf");

        // Taille demi-A4 (recu de caisse)
        Rectangle taille = new Rectangle(PageSize.A5);
        Document doc = new Document(taille, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fichier));
            doc.open();

            BaseColor VERT   = new BaseColor(0x00, 0x6B, 0x5F);
            BaseColor GRIS   = new BaseColor(0xF5, 0xF5, 0xF5);
            BaseColor NOIR   = BaseColor.BLACK;
            BaseColor GDARK  = new BaseColor(0x55, 0x55, 0x55);

            Font fTitre  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, VERT);
            Font fSous   = FontFactory.getFont(FontFactory.HELVETICA, 9, GDARK);
            Font fNum    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NOIR);
            Font fLabel  = FontFactory.getFont(FontFactory.HELVETICA, 9, GDARK);
            Font fVal    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NOIR);
            Font fMontant= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, VERT);
            Font fStatut = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
                "SOLDE".equals(f.getStatut()) ? VERT : new BaseColor(0xE6, 0x7E, 0x22));
            Font fPied   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, GDARK);

            // ── EN-TETE ────────────────────────────────────────────────────────
            Paragraph titreApp = new Paragraph("DoctoVite", fTitre);
            titreApp.setAlignment(Element.ALIGN_CENTER);
            titreApp.setSpacingAfter(2);
            doc.add(titreApp);

            Paragraph sousTitre = new Paragraph("Cabinet Médical — Reçu de paiement", fSous);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            sousTitre.setSpacingAfter(10);
            doc.add(sousTitre);

            doc.add(new Chunk(new LineSeparator(1.5f, 100, VERT, Element.ALIGN_CENTER, -2)));
            doc.add(new Paragraph("\n", fLabel));

            // ── NUMERO ET DATE ─────────────────────────────────────────────────
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            PdfPCell cNum = new PdfPCell(new Paragraph("N° " + f.getNumeroFacture(), fNum));
            cNum.setBorder(Rectangle.NO_BORDER);
            cNum.setPaddingBottom(4);

            String dateAff = formaterDate(f.getDateCree() != null ? f.getDateCree().substring(0, 10) : "");
            PdfPCell cDate = new PdfPCell(new Paragraph("Date : " + dateAff, fSous));
            cDate.setBorder(Rectangle.NO_BORDER);
            cDate.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cDate.setPaddingBottom(4);

            infoTable.addCell(cNum);
            infoTable.addCell(cDate);
            doc.add(infoTable);

            // ── PATIENT ────────────────────────────────────────────────────────
            PdfPTable patTable = new PdfPTable(1);
            patTable.setWidthPercentage(100);
            patTable.setSpacingBefore(6);
            patTable.setSpacingAfter(10);

            Paragraph contenuPat = new Paragraph();
            contenuPat.add(new Chunk("Patient : ", fLabel));
            contenuPat.add(new Chunk(f.getNomPatient() != null ? f.getNomPatient() : "-", fVal));
            if (f.getNumeroDossier() != null) {
                contenuPat.add(new Chunk("   Dossier : " + f.getNumeroDossier(), fLabel));
            }

            PdfPCell cellPat = new PdfPCell(contenuPat);
            cellPat.setBackgroundColor(GRIS);
            cellPat.setBorderColor(new BaseColor(0xDD, 0xDD, 0xDD));
            cellPat.setBorderWidth(0.5f);
            cellPat.setPadding(8);
            patTable.addCell(cellPat);
            doc.add(patTable);

            // ── DETAILS PAIEMENT ───────────────────────────────────────────────
            PdfPTable detTable = new PdfPTable(2);
            detTable.setWidthPercentage(100);
            detTable.setWidths(new int[]{55, 45});
            detTable.setSpacingAfter(10);

            ajouterLigneDet(detTable, "Description", f.getDescription() != null && !f.getDescription().isEmpty()
                ? f.getDescription() : "Consultation médicale", fLabel, fVal);
            ajouterLigneDet(detTable, "Mode de paiement", f.getModePaiement(), fLabel, fVal);
            ajouterLigneDet(detTable, "Montant total", String.format("%.2f MAD", f.getMontantTotal()), fLabel, fVal);
            ajouterLigneDet(detTable, "Montant payé", String.format("%.2f MAD", f.getMontantPaye()), fLabel, fVal);
            if (f.getMontantRestant() > 0) {
                ajouterLigneDet(detTable, "Reste à payer",
                    String.format("%.2f MAD", f.getMontantRestant()), fLabel,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(0xC0, 0x39, 0x2B)));
            }
            doc.add(detTable);

            // ── MONTANT TOTAL EN GRAND ─────────────────────────────────────────
            doc.add(new Chunk(new LineSeparator(0.5f, 100, new BaseColor(0xCC, 0xCC, 0xCC), Element.ALIGN_CENTER, -2)));

            Paragraph pMontant = new Paragraph(String.format("%.2f MAD", f.getMontantPaye()), fMontant);
            pMontant.setAlignment(Element.ALIGN_CENTER);
            pMontant.setSpacingBefore(10);
            pMontant.setSpacingAfter(4);
            doc.add(pMontant);

            Paragraph pStatut = new Paragraph("● " + f.getStatut(), fStatut);
            pStatut.setAlignment(Element.ALIGN_CENTER);
            pStatut.setSpacingAfter(14);
            doc.add(pStatut);

            doc.add(new Chunk(new LineSeparator(1f, 100, VERT, Element.ALIGN_CENTER, -2)));

            // ── PIED ───────────────────────────────────────────────────────────
            Paragraph pied = new Paragraph(
                "Merci de votre confiance — DoctoVite Cabinet Médical\ncontact@doctovite.ma",
                fPied);
            pied.setAlignment(Element.ALIGN_CENTER);
            pied.setSpacingBefore(8);
            doc.add(pied);

            doc.close();

            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fichier);
            JOptionPane.showMessageDialog(this,
                "Recu genere !\n" + fichier.getAbsolutePath(),
                "PDF Recu", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            if (doc.isOpen()) doc.close();
            JOptionPane.showMessageDialog(this,
                "Erreur PDF : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterLigneDet(PdfPTable t, String label, String val, Font fL, Font fV) {
        PdfPCell cl = new PdfPCell(new Paragraph(label, fL));
        cl.setBorder(Rectangle.BOTTOM);
        cl.setBorderColor(new BaseColor(0xEE, 0xEE, 0xEE));
        cl.setPadding(6);
        PdfPCell cv = new PdfPCell(new Paragraph(val, fV));
        cv.setBorder(Rectangle.BOTTOM);
        cv.setBorderColor(new BaseColor(0xEE, 0xEE, 0xEE));
        cv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cv.setPadding(6);
        t.addCell(cl);
        t.addCell(cv);
    }

    private String formaterDate(String d) {
        try {
            return LocalDate.parse(d).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
        } catch (Exception e) { return d; }
    }

    // ── FORMULAIRE CREATION FACTURE ────────────────────────────────────────────
    private void ouvrirFormulaire() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nouvelle Facture", true);
        dlg.setSize(520, 370);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel("Nouvelle Facture");
        t.setFont(Couleurs.SOUS_TITRE); t.setForeground(Color.WHITE); hdr.add(t);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 14, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        List<Patient> patients = new GestionPatient().listerTous();
        JComboBox<Patient> cmbPat = new JComboBox<>(patients.toArray(new Patient[0]));
        cmbPat.setFont(Couleurs.CORPS);
        cmbPat.setPreferredSize(new Dimension(0, 40));

        JTextField txtMontant = Composants.champTexte("Ex: 350.00");
        txtMontant.setPreferredSize(new Dimension(0, 40));
        JTextField txtPaye = Composants.champTexte("0.00 si non paye");
        txtPaye.setPreferredSize(new Dimension(0, 40));
        JComboBox<String> cmbMode = new JComboBox<>(new String[]{"ESPECES", "CARTE", "VIREMENT", "ASSURANCE"});
        cmbMode.setFont(Couleurs.CORPS);
        cmbMode.setPreferredSize(new Dimension(0, 40));
        JTextField txtDesc = Composants.champTexte("Description (optionnel)");
        txtDesc.setPreferredSize(new Dimension(0, 40));

        g.gridy=0; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Patient *"), g);
        g.gridx=1; g.weightx=1; form.add(cmbPat, g);
        g.gridy=1; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Montant total (MAD) *"), g);
        g.gridx=1; g.weightx=1; form.add(txtMontant, g);
        g.gridy=2; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Montant paye (MAD)"), g);
        g.gridx=1; g.weightx=1; form.add(txtPaye, g);
        g.gridy=3; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Mode paiement"), g);
        g.gridx=1; g.weightx=1; form.add(cmbMode, g);
        g.gridy=4; g.gridx=0; g.weightx=0; form.add(Composants.etiquette("Description"), g);
        g.gridx=1; g.weightx=1; form.add(txtDesc, g);

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton ann  = Composants.boutonGris("Annuler");
        ann.addActionListener(e -> dlg.dispose());
        JButton sauv = Composants.boutonSucces("Enregistrer");
        sauv.addActionListener(e -> {
            Patient pat = (Patient) cmbPat.getSelectedItem();
            if (pat == null || txtMontant.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Patient et montant sont obligatoires.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double total = Double.parseDouble(txtMontant.getText().trim());
                double paye  = txtPaye.getText().trim().isEmpty() ? 0
                    : Double.parseDouble(txtPaye.getText().trim());
                Facture f = new Facture();
                f.setPatientId(pat.getId());
                f.setNumeroFacture(gestion.genererNumero());
                f.setMontantTotal(total);
                f.setMontantPaye(paye);
                f.setModePaiement((String) cmbMode.getSelectedItem());
                f.setStatut(paye >= total ? "SOLDE" : paye > 0 ? "PARTIEL" : "EN_ATTENTE");
                f.setDescription(txtDesc.getText().trim());
                if (gestion.ajouter(f)) {
                    JOptionPane.showMessageDialog(dlg, "Facture creee avec succes !");
                    dlg.dispose();
                    charger();
                    if (onMaj != null) onMaj.run(); // rafraichir dashboard
                } else {
                    JOptionPane.showMessageDialog(dlg, "Erreur.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        pied.add(ann);
        pied.add(sauv);

        dlg.add(hdr, BorderLayout.NORTH);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pied, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
