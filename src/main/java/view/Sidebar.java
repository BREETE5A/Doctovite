package view;

import model.Utilisateur;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Sidebar extends JPanel {

    private Utilisateur utilisateur;
    private JPanel zoneContenu;
    private JPanel boiteNav;
    private List<ElementNav> elements = new ArrayList<>();
    private ElementNav elementActif;

    public Sidebar(Utilisateur utilisateur, JPanel zoneContenu) {
        this.utilisateur = utilisateur;
        this.zoneContenu = zoneContenu;
        setPreferredSize(new Dimension(220, 0));
        setBackground(Couleurs.FOND_SIDEBAR);
        setLayout(new BorderLayout());
        construire();
    }

    private void construire() {
        add(construireEntete(), BorderLayout.NORTH);

        boiteNav = new JPanel();
        boiteNav.setOpaque(false);
        boiteNav.setLayout(new BoxLayout(boiteNav, BoxLayout.Y_AXIS));
        boiteNav.setBorder(new EmptyBorder(10, 8, 10, 8));

        JPanel centre = new JPanel(new BorderLayout());
        centre.setOpaque(false);
        centre.add(boiteNav, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);

        add(construireBoutonDeconnexion(), BorderLayout.SOUTH);
    }

    private JPanel construireEntete() {
        JPanel entete = new JPanel();
        entete.setBackground(new Color(0x00, 0x52, 0x4A));
        entete.setOpaque(true);
        entete.setLayout(new BoxLayout(entete, BoxLayout.Y_AXIS));
        entete.setBorder(new EmptyBorder(22, 16, 18, 16));

        String initiales = "";
        if (!utilisateur.getPrenom().isEmpty()) initiales += utilisateur.getPrenom().charAt(0);
        if (!utilisateur.getNom().isEmpty())    initiales += utilisateur.getNom().charAt(0);

        JLabel avatar = new JLabel(initiales.toUpperCase(), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        avatar.setForeground(Color.WHITE);
        avatar.setBackground(Couleurs.VERT_MED);
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(60, 60));
        avatar.setMaximumSize(new Dimension(60, 60));
        avatar.setBorder(BorderFactory.createLineBorder(Couleurs.VERT_CLAIR, 2));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avPanel.setOpaque(false);
        avPanel.add(avatar);

        String nomAffiche = utilisateur.getRole().equals("MEDECIN")
            ? "Dr. " + utilisateur.getNomComplet() : utilisateur.getNomComplet();

        JLabel nom = new JLabel(nomAffiche, SwingConstants.CENTER);
        nom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nom.setForeground(Color.WHITE);
        nom.setAlignmentX(Component.CENTER_ALIGNMENT);

        String roleLib;
        switch (utilisateur.getRole()) {
            case "ADMINISTRATEUR": roleLib = "Administrateur"; break;
            case "MEDECIN":        roleLib = "Medecin"; break;
            default:               roleLib = "Secretaire";
        }
        JLabel role = new JLabel(roleLib, SwingConstants.CENTER);
        role.setFont(Couleurs.PETIT);
        role.setForeground(Couleurs.TEXTE_NAV);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        entete.add(avPanel);
        entete.add(Box.createVerticalStrut(8));
        entete.add(nom);
        entete.add(Box.createVerticalStrut(3));
        entete.add(role);
        entete.add(Box.createVerticalStrut(14));
        entete.add(sep);
        return entete;
    }

    private JPanel construireBoutonDeconnexion() {
        JPanel pied = new JPanel(new BorderLayout());
        pied.setOpaque(false);
        pied.setBorder(new EmptyBorder(8, 14, 18, 14));

        JButton btn = new JButton("Se deconnecter");
        btn.setFont(Couleurs.PETIT);
        btn.setForeground(Couleurs.TEXTE_NAV);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Couleurs.ROUGE); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(Couleurs.TEXTE_NAV); }
        });
        btn.addActionListener(e -> deconnecter());
        pied.add(btn, BorderLayout.CENTER);
        return pied;
    }

    public void ajouterPage(String libelle, JPanel panneau) {
        ElementNav el = new ElementNav(libelle, panneau);
        elements.add(el);
        boiteNav.add(el);
        boiteNav.add(Box.createVerticalStrut(2));
        el.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { selectionner(el); }
        });
        if (elements.size() == 1) selectionner(el);
    }

    public void selectionner(ElementNav el) {
        if (elementActif != null) elementActif.setActif(false);
        elementActif = el;
        el.setActif(true);
        if (zoneContenu != null && el.panneau != null) {
            // Si le panneau a une methode "rafraichir", on l'appelle
            // pour recharger ses donnees a chaque fois qu'on clique dessus.
            essayerRafraichir(el.panneau);

            zoneContenu.removeAll();
            zoneContenu.add(el.panneau, BorderLayout.CENTER);
            zoneContenu.revalidate();
            zoneContenu.repaint();
        }
    }

    /**
     * Rafraichit TOUS les panneaux enregistres dans la sidebar.
     * Appele apres chaque ajout/modification pour que le dashboard
     * et toutes les pages soient immediatement a jour.
     */
    public void rafraichirTout() {
        for (ElementNav el : elements) {
            essayerRafraichir(el.panneau);
        }
        // Forcer le repaint de la zone active
        if (zoneContenu != null) {
            zoneContenu.revalidate();
            zoneContenu.repaint();
        }
    }

    /**
     * Cherche une methode "rafraichir()" dans le panneau et l'appelle si elle existe.
     * Cela permet de recharger les donnees a chaque navigation.
     */
    private void essayerRafraichir(JPanel panneau) {
        try {
            Method m = panneau.getClass().getMethod("rafraichir");
            m.invoke(panneau);
        } catch (NoSuchMethodException e) {
            // Le panneau n'a pas de methode rafraichir : ce n'est pas grave
        } catch (Exception e) {
            System.out.println("Erreur rafraichissement : " + e.getMessage());
        }
    }

    private void deconnecter() {
        int rep = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment vous deconnecter ?",
            "Deconnexion", JOptionPane.YES_NO_OPTION);
        if (rep == JOptionPane.YES_OPTION) {
            SwingUtilities.getWindowAncestor(this).dispose();
            new view.auth.FenetreConnexion().setVisible(true);
        }
    }

    public static class ElementNav extends JPanel {
        JPanel panneau;
        private JLabel libelle;
        private boolean actif;

        ElementNav(String texte, JPanel panneau) {
            this.panneau = panneau;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 9));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            libelle = new JLabel(texte);
            libelle.setFont(Couleurs.NAV);
            libelle.setForeground(Couleurs.TEXTE_NAV);
            add(libelle);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!actif) { setOpaque(true); setBackground(new Color(0x00, 0x52, 0x4D)); repaint(); }
                }
                public void mouseExited(MouseEvent e) {
                    if (!actif) { setOpaque(false); repaint(); }
                }
            });
        }

        void setActif(boolean a) {
            actif = a;
            setOpaque(a);
            setBackground(a ? Couleurs.SIDEBAR_ACT : null);
            libelle.setForeground(a ? Color.WHITE : Couleurs.TEXTE_NAV);
            libelle.setFont(a ? Couleurs.GRAS : Couleurs.NAV);
            repaint();
        }
    }
}
