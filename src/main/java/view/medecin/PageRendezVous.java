package view.medecin;

import controller.GestionRendezVous;
import controller.ServiceNotificationRdv;
import model.RendezVous;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class PageRendezVous extends JPanel {

    private int medecinId;
    private boolean lectureSeule;
    private Utilisateur utilisateurActif;
    private GestionRendezVous gestionRdv = new GestionRendezVous();
    private DefaultTableModel modele;
    private JTable tableau;
    private List<RendezVous> listeRdv;
    private JSpinner spinnerDate;
    private Runnable onMaj;
    private boolean modeVoirTous = false; // true = bouton Tous actif

    public PageRendezVous(int medecinId, boolean lectureSeule) {
        this(medecinId, lectureSeule, null, null);
    }

    public PageRendezVous(int medecinId, boolean lectureSeule, Utilisateur utilisateurActif) {
        this(medecinId, lectureSeule, utilisateurActif, null);
    }

    public PageRendezVous(int medecinId, boolean lectureSeule, Utilisateur utilisateurActif, Runnable onMaj) {
        this.medecinId = medecinId;
        this.lectureSeule = lectureSeule;
        this.utilisateurActif = utilisateurActif;
        this.onMaj = onMaj;
        setLayout(new BorderLayout());
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        construire();
        chargerParDate(LocalDate.now().toString());
    }

    private void construire() {
        JPanel entete = new JPanel(new BorderLayout(10, 0));
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(0, 0, 14, 0));
        entete.add(Composants.titre("Rendez-vous"), BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);

        JLabel lblDate = new JLabel("Date :");
        lblDate.setFont(Couleurs.GRAS);
        lblDate.setForeground(Couleurs.TEXTE_GRIS);

        SpinnerDateModel modelDate = new SpinnerDateModel();
        spinnerDate = new JSpinner(modelDate);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerDate, "dd/MM/yyyy");
        spinnerDate.setEditor(editor);
        spinnerDate.setPreferredSize(new Dimension(130, 36));
        spinnerDate.setValue(new Date());
        spinnerDate.addChangeListener(e -> {
            modeVoirTous = false;
            Date d = (Date) spinnerDate.getValue();
            chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
        });

        JButton btnAujourd = Composants.boutonPrincipal("Aujourd'hui");
        btnAujourd.addActionListener(e -> {
            modeVoirTous = false;
            spinnerDate.setValue(new Date());
            chargerParDate(LocalDate.now().toString());
        });

        JButton btnTous = Composants.boutonGris("Tous");
        btnTous.addActionListener(e -> chargerTous());

        JButton btnActu = Composants.boutonGris("Actualiser");
        btnActu.addActionListener(e -> {
            modeVoirTous = false;
            Date d = (Date) spinnerDate.getValue();
            chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
        });

        droite.add(lblDate);
        droite.add(spinnerDate);
        droite.add(btnAujourd);
        droite.add(btnTous);
        droite.add(btnActu);

        if (!lectureSeule) {
            JButton btnNouv = Composants.boutonSucces("+ Nouveau RDV");
            btnNouv.addActionListener(e -> ouvrirFormulaire(null));
            droite.add(btnNouv);
        }

        JButton btnPdf  = Composants.boutonPDF("PDF");
        JButton btnXlsx = Composants.boutonExcel("Excel");
        btnPdf.addActionListener(e -> controller.ServiceExport.exporterPDF(
            this, modele, "Rendez-vous", "rendez_vous"));
        btnXlsx.addActionListener(e -> controller.ServiceExport.exporterExcel(
            this, modele, "Rendez-vous", "rendez_vous"));
        droite.add(btnPdf);
        droite.add(btnXlsx);
        entete.add(droite, BorderLayout.EAST);

        String[] cols = {"ID", "Date", "Heure", "Patient", "Medecin", "Specialite", "Motif", "Statut"};
        modele = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableau = new JTable(modele) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int lig, int col) {
                Component c = super.prepareRenderer(r, lig, col);
                if (!isRowSelected(lig)) {
                    String s = (String) getValueAt(lig, 7);
                    if ("Urgence".equals(s)) {
                        c.setBackground(Couleurs.URGENCE_FOND);
                        c.setForeground(Couleurs.URGENCE_TXT);
                    }
                }
                return c;
            }
        };
        Composants.styliserTableau(tableau);
        tableau.getColumnModel().getColumn(0).setMaxWidth(50);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setOpaque(false);

        if (!lectureSeule) {
            JButton btnMod = Composants.boutonPrincipal("Modifier");
            JButton btnAnn = Composants.boutonDanger("Annuler");
            JButton btnUrg = Composants.boutonDanger("Urgence");

            btnMod.addActionListener(e -> {
                int sel = tableau.getSelectedRow();
                if (sel < 0) { msg("Selectionnez un rendez-vous."); return; }
                ouvrirFormulaire(listeRdv.get(sel));
            });
            btnAnn.addActionListener(e -> annuler());
            btnUrg.addActionListener(e -> marquerUrgence());

            actions.add(btnMod);
            actions.add(btnAnn);
            actions.add(btnUrg);
        }

        JLabel aide = new JLabel("Double-cliquez sur un RDV pour modifier");
        aide.setFont(Couleurs.PETIT);
        aide.setForeground(Couleurs.TEXTE_PALE);
        actions.add(aide);

        tableau.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !lectureSeule) {
                    int sel = tableau.getSelectedRow();
                    if (sel >= 0) ouvrirFormulaire(listeRdv.get(sel));
                }
            }
        });

        add(entete, BorderLayout.NORTH);
        add(Composants.enveloperTableau(tableau), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    public void chargerParDate(String date) {
        if (medecinId > 0) {
            // Medecin : seulement ses RDV actifs
            listeRdv = gestionRdv.listerActifsParMedecinEtDate(medecinId, date);
        } else {
            // Admin / Secretaire : TOUS les RDV du jour (termines inclus)
            listeRdv = gestionRdv.listerParDate(date);
        }
        remplirTableau();
    }

    // Methode publique appelee par la Sidebar pour rafraichir
    public void rafraichir() {
        if (spinnerDate == null) return;
        // Si on est en mode "Tous", on recharge tous les RDV
        if (modeVoirTous) {
            chargerTous();
            return;
        }
        Date d = (Date) spinnerDate.getValue();
        chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
    }

    private void chargerTous() {
        modeVoirTous = true;
        if (medecinId > 0) {
            listeRdv = gestionRdv.listerParMedecin(medecinId);
        } else {
            listeRdv = gestionRdv.listerTous();
        }
        remplirTableau();
    }

    private void remplirTableau() {
        modele.setRowCount(0);
        for (RendezVous r : listeRdv) {
            modele.addRow(new Object[]{
                r.getId(), r.getDateRdv(), r.getHeureRdv(),
                r.getNomPatient(), r.getNomMedecin(),
                r.getSpecialiteMedecin(),
                r.getMotif() != null ? r.getMotif() : "-",
                r.getStatutAffichage()
            });
        }
    }

    private void annuler() {
        int sel = tableau.getSelectedRow();
        if (sel < 0) { msg("Selectionnez un rendez-vous."); return; }
        int rep = JOptionPane.showConfirmDialog(this, "Annuler ce rendez-vous ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (rep == JOptionPane.YES_OPTION) {
            RendezVous rdv = listeRdv.get(sel);
            gestionRdv.annuler(rdv.getId());
            // Notification automatique
            if (utilisateurActif != null) {
                rdv.setStatut("ANNULE");
                new ServiceNotificationRdv().notifierSuppression(rdv, utilisateurActif);
            }
            Date d = (Date) spinnerDate.getValue();
            chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
        }
    }

    private void marquerUrgence() {
        int sel = tableau.getSelectedRow();
        if (sel < 0) { msg("Selectionnez un rendez-vous."); return; }
        RendezVous rdv = listeRdv.get(sel);
        gestionRdv.changerStatut(rdv.getId(), "URGENCE");
        if (utilisateurActif != null) {
            rdv.setStatut("URGENCE");
            new ServiceNotificationRdv().notifierModification(rdv, utilisateurActif);
        }
        Date d = (Date) spinnerDate.getValue();
        chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
    }

    private void ouvrirFormulaire(RendezVous rdv) {
        DialogueRendezVous dlg = new DialogueRendezVous(
            (Frame) SwingUtilities.getWindowAncestor(this), rdv, utilisateurActif
        );
        dlg.setVisible(true);
        if (dlg.estModifie()) {
            Date d = (Date) spinnerDate.getValue();
            chargerParDate(new SimpleDateFormat("yyyy-MM-dd").format(d));
            if (onMaj != null) onMaj.run(); // rafraichir dashboard et autres pages
        }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
}
