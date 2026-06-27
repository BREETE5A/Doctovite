package view.medecin;

import controller.GestionUtilisateur;
import model.Medecin;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PageProfilMedecin extends JPanel {

    private Utilisateur utilisateur;
    private Medecin medecin;

    public PageProfilMedecin(Utilisateur utilisateur, Medecin medecin) {
        this.utilisateur = utilisateur;
        this.medecin = medecin;
        setLayout(new BorderLayout());
        setBackground(Couleurs.FOND_PAGE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        construire();
    }

    private void construire() {
        JLabel titre = Composants.titre("Mon Profil");
        JPanel titreRow = new JPanel(new BorderLayout());
        titreRow.setOpaque(false);
        titreRow.setBorder(new EmptyBorder(0, 0, 20, 0));
        titreRow.add(titre, BorderLayout.WEST);

        // Carte : avatar a gauche + infos+form a droite (identique admin/secretaire)
        JPanel carte = new JPanel(new BorderLayout(28, 0));
        carte.setBackground(Color.WHITE);
        carte.setBorder(new EmptyBorder(30, 30, 30, 30));

        carte.add(creerAvatar(), BorderLayout.WEST);
        carte.add(creerColonneDroite(), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(carte, BorderLayout.NORTH);

        add(titreRow, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

    private JPanel creerAvatar() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(160, 0));

        String ini = "";
        if (!utilisateur.getPrenom().isEmpty()) ini += utilisateur.getPrenom().charAt(0);
        if (!utilisateur.getNom().isEmpty()) ini += utilisateur.getNom().charAt(0);
        final String initiales = ini.toUpperCase();

        JLabel av = new JLabel(initiales, SwingConstants.CENTER) {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Couleurs.VERT_MED, getWidth(), getHeight(), Couleurs.VERT_FONCE));
                g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                super.paintComponent(g);
            }
        };
        av.setFont(new Font("Segoe UI", Font.BOLD, 44));
        av.setForeground(Color.WHITE);
        av.setPreferredSize(new Dimension(120, 120));
        av.setOpaque(false);

        JLabel roleLbl = new JLabel("Medecin", SwingConstants.CENTER);
        roleLbl.setFont(Couleurs.GRAS);
        roleLbl.setForeground(Couleurs.VERT_MED);

        String spec = (medecin != null) ? medecin.getSpecialite() : "";
        JLabel specLbl = new JLabel(spec, SwingConstants.CENTER);
        specLbl.setFont(Couleurs.PETIT);
        specLbl.setForeground(Couleurs.TEXTE_GRIS);

        JPanel avPan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avPan.setOpaque(false);
        avPan.add(av);

        p.add(avPan, BorderLayout.NORTH);
        p.add(roleLbl, BorderLayout.CENTER);
        p.add(specLbl, BorderLayout.SOUTH);
        return p;
    }

    private JPanel creerColonneDroite() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        // Infos en lecture seule
        JPanel grille = new JPanel(new GridBagLayout());
        grille.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        String spec = (medecin != null) ? medecin.getSpecialite() : "-";
        String hor  = (medecin != null) ? medecin.getHoraires()   : "-";

        String[][] lignes = {
            {"Nom complet :",         "Dr. " + utilisateur.getNomComplet()},
            {"CIN :",                 utilisateur.getCin()},
            {"Specialite :",          spec},
            {"Horaires de travail :", hor},
            {"Email :",               utilisateur.getEmail() != null ? utilisateur.getEmail() : "-"},
            {"Telephone :",           utilisateur.getTelephone() != null ? utilisateur.getTelephone() : "-"},
        };

        for (int i = 0; i < lignes.length; i++) {
            g.gridy = i; g.gridx = 0; g.weightx = 0;
            JLabel k = new JLabel(lignes[i][0]);
            k.setFont(Couleurs.GRAS); k.setForeground(Couleurs.TEXTE_GRIS);
            grille.add(k, g);
            g.gridx = 1; g.weightx = 1;
            JLabel v = new JLabel(lignes[i][1]);
            v.setFont(Couleurs.CORPS); v.setForeground(Couleurs.TEXTE_NOIR);
            grille.add(v, g);
        }

        JSeparator sep = new JSeparator();
        sep.setForeground(Couleurs.BORDURE);

        p.add(grille, BorderLayout.NORTH);
        p.add(sep, BorderLayout.CENTER);
        p.add(creerFormModif(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel creerFormModif() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 0, 0, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JLabel titreMod = new JLabel("Modifier mes informations");
        titreMod.setFont(Couleurs.SOUS_TITRE);
        titreMod.setForeground(Couleurs.TEXTE_NOIR);

        JTextField txtEmail = Composants.champTexte("Adresse email");
        txtEmail.setPreferredSize(new Dimension(0, 40));
        if (utilisateur.getEmail() != null) txtEmail.setText(utilisateur.getEmail());

        JTextField txtTel = Composants.champTexte("Numero de telephone");
        txtTel.setPreferredSize(new Dimension(0, 40));
        if (utilisateur.getTelephone() != null) txtTel.setText(utilisateur.getTelephone());

        JPasswordField txtAncMdp = Composants.champMotDePasse();
        txtAncMdp.setPreferredSize(new Dimension(0, 40));

        JPasswordField txtNouMdp = Composants.champMotDePasse();
        txtNouMdp.setPreferredSize(new Dimension(0, 40));

        JButton btnSauv = Composants.boutonPrincipal("Enregistrer les modifications");
        btnSauv.setPreferredSize(new Dimension(0, 42));
        btnSauv.addActionListener(e -> enregistrer(
            txtEmail.getText().trim(),
            txtTel.getText().trim(),
            new String(txtAncMdp.getPassword()),
            new String(txtNouMdp.getPassword())
        ));

        g.gridy = 0; g.gridx = 0; g.gridwidth = 2; g.weightx = 1;
        p.add(titreMod, g);
        g.gridwidth = 1;

        g.gridy = 1; g.gridx = 0; g.weightx = 0;
        p.add(Composants.etiquette("Email :"), g);
        g.gridx = 1; g.weightx = 1; p.add(txtEmail, g);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        p.add(Composants.etiquette("Telephone :"), g);
        g.gridx = 1; g.weightx = 1; p.add(txtTel, g);

        g.gridy = 3; g.gridx = 0; g.weightx = 0;
        p.add(Composants.etiquette("Ancien mot de passe :"), g);
        g.gridx = 1; g.weightx = 1; p.add(txtAncMdp, g);

        g.gridy = 4; g.gridx = 0; g.weightx = 0;
        p.add(Composants.etiquette("Nouveau mot de passe :"), g);
        g.gridx = 1; g.weightx = 1; p.add(txtNouMdp, g);

        g.gridy = 5; g.gridx = 1; g.weightx = 0;
        p.add(btnSauv, g);

        return p;
    }

    private void enregistrer(String email, String tel, String ancMdp, String nouMdp) {
        if (!ancMdp.isEmpty() || !nouMdp.isEmpty()) {
            if (!ancMdp.equals(utilisateur.getMotDePasse())) {
                JOptionPane.showMessageDialog(this, "Ancien mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nouMdp.length() < 4) {
                JOptionPane.showMessageDialog(this, "Le nouveau mot de passe doit avoir au moins 4 caracteres.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            GestionUtilisateur gestion = new GestionUtilisateur();
            gestion.changerMotDePasse(utilisateur.getId(), nouMdp);
            utilisateur.setMotDePasse(nouMdp);
        }
        utilisateur.setEmail(email.isEmpty() ? null : email);
        utilisateur.setTelephone(tel.isEmpty() ? null : tel);
        GestionUtilisateur gestion = new GestionUtilisateur();
        if (gestion.modifier(utilisateur)) {
            JOptionPane.showMessageDialog(this, "Profil mis a jour avec succes !");
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise a jour.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
