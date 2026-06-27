package view.secretaire;

import controller.GestionMedecin;
import model.Medecin;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class PageMedecins extends JPanel {

    private boolean lectureSeule;
    private Runnable onMaj;
    private GestionMedecin gestion = new GestionMedecin();
    private DefaultTableModel modele;
    private JTable tableau;
    private List<Medecin> medecins;
    private JComboBox<String> cmbSpec;
    private JTextField champRecherche;
    private boolean enChargement = false;

    public PageMedecins(boolean lectureSeule) {
        this(lectureSeule, null);
    }
    public PageMedecins(boolean lectureSeule, Runnable onMaj) {
        this.onMaj = onMaj;
        this.lectureSeule = lectureSeule;
        setLayout(new BorderLayout());
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        charger();
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout(10, 0));
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 14, 0));
        entete.add(Composants.titre("Medecins de la Clinique"), BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);

        champRecherche = Composants.champTexte("Rechercher par nom, CIN...");
        champRecherche.setPreferredSize(new Dimension(200, 36));
        champRecherche.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { if (!enChargement) filtrer(); }
        });

        cmbSpec = new JComboBox<>();
        cmbSpec.setFont(Couleurs.CORPS);
        cmbSpec.setPreferredSize(new Dimension(200, 36));
        cmbSpec.addActionListener(e -> { if (!enChargement) filtrer(); });

        JButton btnActu = Composants.boutonGris("Actualiser");
        btnActu.addActionListener(e -> charger());

        droite.add(champRecherche);
        droite.add(Composants.etiquette("Specialite :"));
        droite.add(cmbSpec);
        droite.add(btnActu);

        if (!lectureSeule) {
            JButton btnNouv = Composants.boutonSucces("+ Nouveau Medecin");
            btnNouv.addActionListener(e -> ouvrirFormulaire(null));
            droite.add(btnNouv);
        }

        JButton btnPdf  = Composants.boutonPDF("PDF");
        JButton btnXlsx = Composants.boutonExcel("Excel");
        btnPdf.addActionListener(e -> controller.ServiceExport.exporterPDF(
            this, modele, "Medecins", "medecins"));
        btnXlsx.addActionListener(e -> controller.ServiceExport.exporterExcel(
            this, modele, "Medecins", "medecins"));
        droite.add(btnPdf);
        droite.add(btnXlsx);
        entete.add(droite, BorderLayout.EAST);

        String[] cols = {"ID", "CIN", "Nom complet", "Specialite", "Horaires", "Statut", "Email"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele);
        Composants.styliserTableau(tableau);
        tableau.getColumnModel().getColumn(0).setMaxWidth(50);
        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lectureSeule) {
                    int r = tableau.getSelectedRow();
                    if (r >= 0) ouvrirFormulaire(medecins.get(r));
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setOpaque(false);
        if (!lectureSeule) {
            JButton btnMod = Composants.boutonPrincipal("Modifier");
            btnMod.addActionListener(e -> {
                int r = tableau.getSelectedRow();
                if (r < 0) { JOptionPane.showMessageDialog(this, "Selectionnez un medecin."); return; }
                ouvrirFormulaire(medecins.get(r));
            });
            actions.add(btnMod);
        }

        add(entete, BorderLayout.NORTH);
        add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    public void charger() {
        enChargement = true;
        String specActuelle = (String) cmbSpec.getSelectedItem();
        cmbSpec.removeAllItems();
        cmbSpec.addItem("Toutes les specialites");
        for (String s : gestion.listerSpecialites()) cmbSpec.addItem(s);
        if (specActuelle != null && !specActuelle.startsWith("Toutes")) cmbSpec.setSelectedItem(specActuelle);
        enChargement = false;
        medecins = gestion.listerTous();
        remplir();
    }

    // Ne recharge que si aucune recherche n'est active
    public void rafraichir() {
        if (champRecherche == null || champRecherche.getText().trim().isEmpty()) {
            charger();
        }
    }

    private void filtrer() {
        String spec = (String) cmbSpec.getSelectedItem();
        String rech = champRecherche.getText().trim().toLowerCase();
        if (spec != null && !spec.startsWith("Toutes")) {
            medecins = gestion.listerParSpecialite(spec);
        } else {
            medecins = gestion.listerTous();
        }
        if (!rech.isEmpty()) {
            medecins = medecins.stream()
                .filter(m -> m.getNomComplet().toLowerCase().contains(rech)
                          || m.getSpecialite().toLowerCase().contains(rech)
                          || m.getCin().toLowerCase().contains(rech))
                .collect(Collectors.toList());
        }
        remplir();
    }

    private void remplir() {
        modele.setRowCount(0);
        for (Medecin m : medecins) {
            modele.addRow(new Object[]{
                m.getId(), m.getCin(), m.getNomComplet(),
                m.getSpecialite(), m.getHoraires(),
                m.getStatut() != null ? m.getStatut() : "-",
                m.getEmail() != null ? m.getEmail() : "-"
            });
        }
    }

    private void ouvrirFormulaire(Medecin m) {
        DialogueMedecin dlg = new DialogueMedecin((Frame) SwingUtilities.getWindowAncestor(this), m, gestion);
        dlg.setVisible(true);
        if (dlg.estModifie()) { charger(); if (onMaj != null) onMaj.run(); }
    }
}
