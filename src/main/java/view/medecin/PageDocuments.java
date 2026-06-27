package view.medecin;

import controller.GestionMedecin;
import controller.GestionOrdonnance;
import controller.GestionPatient;
import model.Medecin;
import model.Ordonnance;
import model.Patient;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;

// ── iText 5 pour le PDF ordonnance ───────────────────────────────────────────
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * PageDocuments — Gestion des ordonnances.
 * Bouton Excel supprime.
 * Bouton "Ouvrir" genere une vraie feuille d'ordonnance PDF avec iText.
 */
public class PageDocuments extends JPanel {

    private Utilisateur utilisateur;
    private boolean lectureSeule;
    private GestionOrdonnance gestion = new GestionOrdonnance();
    private DefaultTableModel modele;
    private JTextField champRecherche;
    private List<Ordonnance> listeOrd;
    private JTable tableau;

    private Medecin medecinConnecte;

    public PageDocuments(Utilisateur utilisateur, boolean lectureSeule) {
        this.utilisateur = utilisateur;
        this.lectureSeule = lectureSeule;

        if ("MEDECIN".equals(utilisateur.getRole())) {
            medecinConnecte = new GestionMedecin().trouverParUtilisateurId(utilisateur.getId());
        }

        setLayout(new BorderLayout(0, 14));
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        charger("");
    }

    public void rafraichir() {
        // Ne recharge que si aucune recherche n'est en cours
        if (champRecherche == null || champRecherche.getText().trim().isEmpty()) {
            charger("");
        }
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout(0, 10));
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 12, 0));
        entete.add(Composants.titre("Ordonnances"), BorderLayout.NORTH);

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barre.setOpaque(false);

        champRecherche = Composants.champTexte("Rechercher par nom, prenom ou dossier...");
        champRecherche.setPreferredSize(new Dimension(300, 38));
        champRecherche.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { charger(champRecherche.getText().trim()); }
        });

        JButton btnRech = Composants.boutonPrincipal("Rechercher");
        JButton btnActu = Composants.boutonGris("Actualiser");
        btnRech.addActionListener(e -> charger(champRecherche.getText().trim()));
        btnActu.addActionListener(e -> { champRecherche.setText(""); charger(""); });

        barre.add(Composants.etiquette("Chercher :"));
        barre.add(champRecherche);
        barre.add(btnRech);
        barre.add(btnActu);

        if (!lectureSeule) {
            JButton btnNouv = Composants.boutonSucces("+ Ordonnance");
            btnNouv.addActionListener(e -> ouvrirFormulaire());
            barre.add(btnNouv);
        }

        // Bouton Ouvrir (PDF ordonnance vraie feuille) — Excel supprime
        JButton btnPdf = Composants.boutonPDF("Imprimer PDF");
        btnPdf.addActionListener(e -> {
            int sel = tableau.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Selectionnez une ordonnance dans la liste pour l'imprimer.",
                    "Aucune selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            imprimerOrdonnance(listeOrd.get(sel));
        });
        barre.add(btnPdf);

        entete.add(barre, BorderLayout.CENTER);

        String[] cols = {"ID", "Date", "Patient", "Dossier", "Medecin", "Medicaments", "Posologie", "Duree"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele);
        Composants.styliserTableau(tableau);
        tableau.getColumnModel().getColumn(0).setMaxWidth(50);

        // Double-clic pour ouvrir le PDF
        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int sel = tableau.getSelectedRow();
                    if (sel >= 0) imprimerOrdonnance(listeOrd.get(sel));
                }
            }
        });

        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(new Composants.BordureArrondie(Couleurs.BORDURE, 1, 12));
        carte.add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);

        JLabel aide = new JLabel("  Double-clic sur une ordonnance pour l'imprimer en PDF");
        aide.setFont(Couleurs.PETIT);
        aide.setForeground(Couleurs.TEXTE_PALE);

        add(entete, BorderLayout.NORTH);
        add(carte, BorderLayout.CENTER);
        add(aide, BorderLayout.SOUTH);
    }

    private void charger(String recherche) {
        listeOrd = null;
        modele.setRowCount(0);
        List<Ordonnance> liste;

        if (recherche != null && !recherche.isEmpty()) {
            liste = gestion.rechercherParNom(recherche);
        } else if (!lectureSeule && medecinConnecte != null) {
            liste = gestion.listerParMedecin(medecinConnecte.getId());
        } else {
            liste = gestion.listerTous();
        }

        listeOrd = liste;
        for (Ordonnance o : liste) {
            String meds = o.getMedicaments() != null ? o.getMedicaments() : "-";
            if (meds.length() > 40) meds = meds.substring(0, 37) + "...";
            modele.addRow(new Object[]{
                o.getId(), o.getDateEmission(), o.getNomPatient(),
                o.getNumeroDossier() != null ? o.getNumeroDossier() : "-",
                o.getNomMedecin(),
                meds,
                o.getPosologie() != null ? o.getPosologie() : "-",
                o.getDuree() != null ? o.getDuree() : "-"
            });
        }
    }

    // ── GENERATION PDF — Vraie feuille d'ordonnance ───────────────────────────
    /**
     * Genere un PDF qui ressemble a une vraie feuille d'ordonnance medicale :
     * - En-tete medecin a gauche, infos clinique a droite
     * - Nom du patient, date
     * - Liste des medicaments avec posologie
     * - Zone de signature en bas
     */
    private void imprimerOrdonnance(Ordonnance ord) {
        // Recuperer les infos completes du medecin et du patient
        Medecin med = null;
        Patient pat = null;
        try {
            med = new GestionMedecin().trouverParId(ord.getMedecinId());
            pat = new GestionPatient().trouverParId(ord.getPatientId());
        } catch (Exception e) {
            System.out.println("Erreur chargement infos ordonnance : " + e.getMessage());
        }

        final Medecin medFinal = med;
        final Patient patFinal = pat;

        // Choisir l'emplacement du PDF
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer l'ordonnance en PDF");
        String nomFich = "Ordonnance_" + ord.getNomPatient().replace(" ", "_") + "_" + ord.getDateEmission();
        chooser.setSelectedFile(new File(nomFich + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File fichier = chooser.getSelectedFile();
        if (!fichier.getName().toLowerCase().endsWith(".pdf")) {
            fichier = new File(fichier.getAbsolutePath() + ".pdf");
        }

        // ── Generation du PDF ─────────────────────────────────────────────────
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fichier));
            doc.open();

            // ── COULEURS ──────────────────────────────────────────────────────
            BaseColor BLEU_HEADER = new BaseColor(0x1A, 0x5C, 0x9C);  // bleu médical classique
            BaseColor GRIS_LEGER  = new BaseColor(0xF7, 0xF7, 0xF7);
            BaseColor GRIS_TEXTE  = new BaseColor(0x55, 0x55, 0x55);
            BaseColor NOIR        = BaseColor.BLACK;

            // ── POLICES ───────────────────────────────────────────────────────
            Font fontNomMedecin  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BLEU_HEADER);
            Font fontSpecialite  = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BLEU_HEADER);
            Font fontInfoMed     = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS_TEXTE);
            Font fontClinique    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BLEU_HEADER);
            Font fontInfoClin    = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS_TEXTE);
            Font fontDate        = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, GRIS_TEXTE);
            Font fontTitrePatient= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NOIR);
            Font fontInfoPatient = FontFactory.getFont(FontFactory.HELVETICA, 10, NOIR);
            Font fontTitreMed    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, NOIR);
            Font fontMedicament  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NOIR);
            Font fontPosologie   = FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS_TEXTE);
            Font fontSignature   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, GRIS_TEXTE);

            // ── BLOC EN-TETE : Medecin gauche | Clinique droite ───────────────
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new int[]{50, 50});

            // Colonne gauche : infos medecin
            String nomMed = ord.getNomMedecin() != null ? ord.getNomMedecin() : "Dr. Inconnu";
            String spec   = (medFinal != null && medFinal.getSpecialite() != null)
                ? medFinal.getSpecialite() : "";
            String telMed = (medFinal != null && medFinal.getTelephone() != null)
                ? "Tél. : " + medFinal.getTelephone() : "";
            String emailMed = (medFinal != null && medFinal.getEmail() != null)
                ? medFinal.getEmail() : "";
            String horaires = (medFinal != null)
                ? "Horaires : " + medFinal.getHeureDebut() + " — " + medFinal.getHeureFin() : "";

            Paragraph gauche = new Paragraph();
            gauche.add(new Chunk(nomMed + "\n", fontNomMedecin));
            if (!spec.isEmpty()) gauche.add(new Chunk(spec + "\n", fontSpecialite));
            gauche.add(new Chunk("\n", fontInfoMed));
            if (!telMed.isEmpty()) gauche.add(new Chunk(telMed + "\n", fontInfoMed));
            if (!emailMed.isEmpty()) gauche.add(new Chunk(emailMed + "\n", fontInfoMed));
            if (!horaires.isEmpty()) gauche.add(new Chunk(horaires + "\n", fontInfoMed));

            PdfPCell cellGauche = new PdfPCell(gauche);
            cellGauche.setBorder(Rectangle.NO_BORDER);
            cellGauche.setPaddingBottom(8);

            // Colonne droite : infos clinique
            Paragraph droite = new Paragraph();
            droite.setAlignment(Element.ALIGN_RIGHT);
            droite.add(new Chunk("DoctoVite\n", fontClinique));
            droite.add(new Chunk("DoctoVite - Cabinet Médical\n", fontInfoClin));
            droite.add(new Chunk("\n", fontInfoClin));
            droite.add(new Chunk("Tél. : +212 661 001 001\n", fontInfoClin));
            droite.add(new Chunk("contact@doctovite.ma\n", fontInfoClin));

            PdfPCell cellDroite = new PdfPCell(droite);
            cellDroite.setBorder(Rectangle.NO_BORDER);
            cellDroite.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDroite.setPaddingBottom(8);

            headerTable.addCell(cellGauche);
            headerTable.addCell(cellDroite);
            doc.add(headerTable);

            // ── SEPARATEUR HORIZONTAL ─────────────────────────────────────────
            LineSeparator sep = new LineSeparator(1.5f, 100, BLEU_HEADER, Element.ALIGN_CENTER, -2);
            doc.add(new Chunk(sep));
            doc.add(new Paragraph("\n", fontInfoMed));

            // ── DATE D'EMISSION ───────────────────────────────────────────────
            String dateAff = ord.getDateEmission() != null ? formaterDate(ord.getDateEmission()) : "";
            Paragraph pDate = new Paragraph("Le " + dateAff, fontDate);
            pDate.setAlignment(Element.ALIGN_RIGHT);
            pDate.setSpacingAfter(14);
            doc.add(pDate);

            // ── INFOS PATIENT ─────────────────────────────────────────────────
            Paragraph pTitrePat = new Paragraph();
            pTitrePat.setSpacingBefore(4);
            pTitrePat.setSpacingAfter(4);

            String nomPat  = ord.getNomPatient() != null ? ord.getNomPatient() : "-";
            String dossPat = ord.getNumeroDossier() != null ? ord.getNumeroDossier() : "";

            // Infos supplementaires patient si disponibles
            String agePat  = "";
            String grpPat  = "";
            if (patFinal != null) {
                if (patFinal.getAge() > 0) agePat = patFinal.getAge() + " ans";
                if (patFinal.getGroupeSanguin() != null && !patFinal.getGroupeSanguin().isEmpty())
                    grpPat = "Gr. " + patFinal.getGroupeSanguin();
            }

            PdfPTable patTable = new PdfPTable(1);
            patTable.setWidthPercentage(70);
            patTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            Paragraph contenuPat = new Paragraph();
            contenuPat.add(new Chunk(nomPat + "\n", fontTitrePatient));
            if (!dossPat.isEmpty()) contenuPat.add(new Chunk("Dossier : " + dossPat + "   ", fontInfoPatient));
            if (!agePat.isEmpty())  contenuPat.add(new Chunk(agePat + "   ", fontInfoPatient));
            if (!grpPat.isEmpty())  contenuPat.add(new Chunk(grpPat, fontInfoPatient));

            PdfPCell cellPat = new PdfPCell(contenuPat);
            cellPat.setBackgroundColor(GRIS_LEGER);
            cellPat.setBorderColor(new BaseColor(0xCC, 0xCC, 0xCC));
            cellPat.setBorderWidth(0.5f);
            cellPat.setPadding(10);
            patTable.addCell(cellPat);
            doc.add(patTable);

            doc.add(new Paragraph("\n", fontInfoMed));

            // ── TITRE "ORDONNANCE" ────────────────────────────────────────────
            Paragraph titrOrd = new Paragraph("ORDONNANCE", fontTitreMed);
            titrOrd.setAlignment(Element.ALIGN_CENTER);
            titrOrd.setSpacingBefore(6);
            titrOrd.setSpacingAfter(16);
            doc.add(titrOrd);

            // ── LISTE DES MEDICAMENTS ─────────────────────────────────────────
            // Chaque medicament est separe par une virgule ou un saut de ligne
            String[] medicaments = ord.getMedicaments().split("[,\n]+");
            String posologie = ord.getPosologie() != null ? ord.getPosologie() : "";
            String[] posologies = posologie.split("[,\n]+");
            String duree = ord.getDuree() != null ? ord.getDuree() : "";

            for (int i = 0; i < medicaments.length; i++) {
                String med2 = medicaments[i].trim();
                if (med2.isEmpty()) continue;

                Paragraph pMed = new Paragraph();
                pMed.setIndentationLeft(20);
                pMed.setSpacingBefore(6);
                pMed.setSpacingAfter(2);

                // Tiret + nom du médicament en gras
                pMed.add(new Chunk("- " + med2, fontMedicament));
                doc.add(pMed);

                // Posologie correspondante (si disponible)
                String poso = (i < posologies.length) ? posologies[i].trim() : "";
                if (!poso.isEmpty()) {
                    Paragraph pPoso = new Paragraph(poso, fontPosologie);
                    pPoso.setIndentationLeft(36);
                    pPoso.setSpacingAfter(2);
                    doc.add(pPoso);
                }

                // Durée (sur le premier médicament seulement)
                if (i == 0 && !duree.isEmpty()) {
                    Paragraph pDur = new Paragraph("Durée : " + duree, fontPosologie);
                    pDur.setIndentationLeft(36);
                    pDur.setSpacingAfter(4);
                    doc.add(pDur);
                }

                // Petit séparateur entre médicaments
                if (i < medicaments.length - 1) {
                    LineSeparator sepMed = new LineSeparator(0.3f, 80, BaseColor.LIGHT_GRAY, Element.ALIGN_LEFT, -2);
                    doc.add(new Chunk(sepMed));
                }
            }

            // ── OBSERVATIONS ──────────────────────────────────────────────────
            if (ord.getObservations() != null && !ord.getObservations().trim().isEmpty()) {
                doc.add(new Paragraph("\n", fontInfoMed));
                Font fontObs = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, GRIS_TEXTE);
                Paragraph pObs = new Paragraph("Observations : " + ord.getObservations(), fontObs);
                pObs.setIndentationLeft(20);
                doc.add(pObs);
            }

            // ── PIED : ZONE DE SIGNATURE ──────────────────────────────────────
            doc.add(new Paragraph("\n\n\n", fontInfoMed));

            LineSeparator sepPied = new LineSeparator(1f, 100, new BaseColor(0xCC, 0xCC, 0xCC), Element.ALIGN_CENTER, -2);
            doc.add(new Chunk(sepPied));

            PdfPTable piedTable = new PdfPTable(2);
            piedTable.setWidthPercentage(100);
            piedTable.setWidths(new int[]{60, 40});
            piedTable.setSpacingBefore(10);

            // Bas gauche : validite
            Paragraph validite = new Paragraph("Valable 3 mois à compter du " + dateAff, fontSignature);
            PdfPCell cellValid = new PdfPCell(validite);
            cellValid.setBorder(Rectangle.NO_BORDER);
            cellValid.setPaddingTop(6);

            // Bas droite : zone signature
            Paragraph signature = new Paragraph();
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.add(new Chunk("Signature & Cachet\n\n\n\n", fontSignature));
            signature.add(new Chunk(nomMed, fontSignature));
            PdfPCell cellSign = new PdfPCell(signature);
            cellSign.setBorder(Rectangle.NO_BORDER);
            cellSign.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellSign.setPaddingTop(6);

            piedTable.addCell(cellValid);
            piedTable.addCell(cellSign);
            doc.add(piedTable);

            doc.close();

            // Ouvrir le PDF automatiquement
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(fichier);
            }

            JOptionPane.showMessageDialog(this,
                "Ordonnance generee !\n" + fichier.getAbsolutePath(),
                "PDF Ordonnance", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            if (doc.isOpen()) doc.close();
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la generation du PDF :\n" + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ── FORMATER LA DATE ──────────────────────────────────────────────────────
    private String formaterDate(String dateStr) {
        try {
            LocalDate d = LocalDate.parse(dateStr.substring(0, 10));
            return d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
        } catch (Exception e) {
            return dateStr;
        }
    }

    // ── FORMULAIRE D'AJOUT D'ORDONNANCE ──────────────────────────────────────
    private void ouvrirFormulaire() {
        if ("MEDECIN".equals(utilisateur.getRole()) && medecinConnecte == null) {
            medecinConnecte = new GestionMedecin().trouverParUtilisateurId(utilisateur.getId());
            if (medecinConnecte == null) {
                JOptionPane.showMessageDialog(this,
                    "Profil medecin introuvable. Contactez l'administrateur.",
                    "Erreur profil", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Nouvelle Ordonnance", true);
        dlg.setSize(600, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel hdr = Composants.panneauDegrade(Couleurs.VERT_FONCE, Couleurs.VERT_MED);
        hdr.setLayout(new BorderLayout());
        hdr.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel titreLabel = new JLabel("Rediger une Ordonnance");
        titreLabel.setFont(Couleurs.SOUS_TITRE);
        titreLabel.setForeground(Color.WHITE);
        hdr.add(titreLabel);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 26, 14, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        List<Patient> patients = new GestionPatient().listerTous();
        JComboBox<Patient> cmbPat = new JComboBox<>(patients.toArray(new Patient[0]));
        cmbPat.setFont(Couleurs.CORPS);
        cmbPat.setPreferredSize(new Dimension(0, 40));

        boolean estMedecin = "MEDECIN".equals(utilisateur.getRole());
        JComboBox<Medecin> cmbMed = null;
        if (!estMedecin) {
            List<Medecin> medecins = new GestionMedecin().listerTous();
            cmbMed = new JComboBox<>(medecins.toArray(new Medecin[0]));
            cmbMed.setFont(Couleurs.CORPS);
            cmbMed.setPreferredSize(new Dimension(0, 40));
        }

        JTextArea txtMeds = new JTextArea(4, 30);
        txtMeds.setFont(Couleurs.CORPS);
        txtMeds.setLineWrap(true);
        txtMeds.setWrapStyleWord(true);
        txtMeds.setBorder(BorderFactory.createCompoundBorder(
            new Composants.BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 10, 8, 10)));

        JSpinner spinComp = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spinComp.setFont(Couleurs.CORPS);
        spinComp.setPreferredSize(new Dimension(70, 38));
        JCheckBox chkMatin = new JCheckBox("Matin");
        JCheckBox chkAprem = new JCheckBox("Apres-midi");
        JCheckBox chkSoir  = new JCheckBox("Soir");
        chkMatin.setFont(Couleurs.CORPS); chkMatin.setBackground(Color.WHITE);
        chkAprem.setFont(Couleurs.CORPS); chkAprem.setBackground(Color.WHITE);
        chkSoir.setFont(Couleurs.CORPS);  chkSoir.setBackground(Color.WHITE);

        JPanel panelPoso = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelPoso.setBackground(Color.WHITE);
        panelPoso.add(spinComp);
        JLabel lblComp = new JLabel("comprime(s) :");
        lblComp.setFont(Couleurs.CORPS);
        panelPoso.add(lblComp);
        panelPoso.add(chkMatin);
        panelPoso.add(chkAprem);
        panelPoso.add(chkSoir);

        JSpinner spinDuree = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
        spinDuree.setFont(Couleurs.CORPS);
        spinDuree.setPreferredSize(new Dimension(90, 38));
        JPanel panelDuree = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelDuree.setBackground(Color.WHITE);
        panelDuree.add(spinDuree);
        JLabel lblJ = new JLabel("jour(s)");
        lblJ.setFont(Couleurs.CORPS);
        panelDuree.add(lblJ);

        JTextField txtObs = Composants.champTexte("Observations (optionnel)");
        txtObs.setPreferredSize(new Dimension(0, 38));

        int ligne = 0;
        g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Patient *"), g);
        g.gridx = 1; g.weightx = 1; form.add(cmbPat, g);

        if (!estMedecin && cmbMed != null) {
            ligne++;
            g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Medecin *"), g);
            g.gridx = 1; g.weightx = 1; form.add(cmbMed, g);
        }

        ligne++;
        g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Medicaments *"), g);
        g.gridx = 1; g.weightx = 1; form.add(new JScrollPane(txtMeds), g);

        ligne++;
        g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Posologie"), g);
        g.gridx = 1; g.weightx = 1; form.add(panelPoso, g);

        ligne++;
        g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Duree"), g);
        g.gridx = 1; g.weightx = 1; form.add(panelDuree, g);

        ligne++;
        g.gridy = ligne; g.gridx = 0; g.weightx = 0; form.add(Composants.etiquette("Observations"), g);
        g.gridx = 1; g.weightx = 1; form.add(txtObs, g);

        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pied.setBackground(Couleurs.FOND_PAGE);
        JButton btnAnn  = Composants.boutonGris("Annuler");
        JButton btnSauv = Composants.boutonSucces("Enregistrer");
        btnAnn.addActionListener(e -> dlg.dispose());

        final JComboBox<Medecin> cmbMedFinal = cmbMed;

        btnSauv.addActionListener(e -> {
            Patient pat = (Patient) cmbPat.getSelectedItem();
            String meds = txtMeds.getText().trim();
            if (pat == null || meds.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Patient et medicaments sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Medecin med2;
            if (estMedecin) {
                med2 = new GestionMedecin().trouverParUtilisateurId(utilisateur.getId());
                if (med2 == null) {
                    JOptionPane.showMessageDialog(dlg, "Profil medecin introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                med2 = (Medecin) cmbMedFinal.getSelectedItem();
                if (med2 == null) {
                    JOptionPane.showMessageDialog(dlg, "Veuillez selectionner un medecin.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            StringBuilder poso = new StringBuilder();
            poso.append((int) spinComp.getValue()).append(" comprime(s)");
            if (chkMatin.isSelected() || chkAprem.isSelected() || chkSoir.isSelected()) {
                poso.append(" : ");
                if (chkMatin.isSelected()) poso.append("Matin ");
                if (chkAprem.isSelected())  poso.append("Apres-midi ");
                if (chkSoir.isSelected())   poso.append("Soir");
            }

            int jours = (int) spinDuree.getValue();
            String duree = jours + " jour" + (jours > 1 ? "s" : "");

            Ordonnance o = new Ordonnance();
            o.setPatientId(pat.getId());
            o.setMedecinId(med2.getId());
            o.setMedicaments(meds);
            o.setPosologie(poso.toString().trim());
            o.setDuree(duree);
            o.setObservations(txtObs.getText().trim());

            if (gestion.ajouter(o)) {
                JOptionPane.showMessageDialog(dlg, "Ordonnance enregistree avec succes !");
                dlg.dispose();
                charger("");
                if (estMedecin) medecinConnecte = med2;
            } else {
                JOptionPane.showMessageDialog(dlg,
                    "Erreur : " + gestion.getDerniereErreur() + "\n\n" +
                    "Si 'Table prescriptions doesn't exist' : executez creer_prescriptions.sql dans phpMyAdmin.",
                    "Erreur enregistrement", JOptionPane.ERROR_MESSAGE);
            }
        });

        pied.add(btnAnn);
        pied.add(btnSauv);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        dlg.add(hdr, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(pied, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
