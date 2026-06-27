package view.auth;

import controller.GestionUtilisateur;
import model.Utilisateur;
import view.Composants;
import view.Couleurs;
import view.admin.FenetreAdmin;
import view.medecin.FenetreMedecin;
import view.secretaire.FenetreSecretaire;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class FenetreConnexion extends JFrame {

    private JTextField champCin;
    private JPasswordField champMotDePasse;
    private JLabel labelErreur;

    public FenetreConnexion() {
        setTitle("DoctoVite");
        setSize(430, 540);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        construire();
    }

    private void construire() {
        // Fond simple sans Graphics2D
        JPanel fond = new JPanel(new BorderLayout());
        fond.setBackground(Couleurs.VERT_FONCE);

        fond.add(construireLogo(), BorderLayout.NORTH);
        fond.add(construireFormulaire(), BorderLayout.CENTER);
        setContentPane(fond);
    }

    private JPanel construireLogo() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 20, 14, 20));

        // Chargement direct depuis les ressources — sans fallback dessiné
        JLabel logoLabel = chargerLogo(80);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titre = new JLabel("Connexion");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setForeground(Color.WHITE);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sous = new JLabel("DoctoVite");
        sous.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sous.setForeground(new Color(255, 255, 255, 190));
        sous.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(logoLabel);
        p.add(Box.createVerticalStrut(10));
        p.add(titre);
        p.add(Box.createVerticalStrut(3));
        p.add(sous);
        return p;
    }

    private JPanel construireFormulaire() {
        // Carte blanche sans Graphics2D — bordure arrondie via composant standard
        JPanel carte = new JPanel();
        carte.setBackground(Color.WHITE);
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(BorderFactory.createCompoundBorder(
            new Composants.BordureArrondie(Color.WHITE, 0, 24),
            new EmptyBorder(24, 34, 24, 34)
        ));

        JLabel lblCin = etiqForm("Identifiant (CIN)");
        champCin = Composants.champTexte("Entrez votre CIN");
        champCin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        champCin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMdp = etiqForm("Mot de passe");
        champMotDePasse = Composants.champMotDePasse();
        champMotDePasse.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        champMotDePasse.setAlignmentX(Component.LEFT_ALIGNMENT);

        labelErreur = new JLabel(" ");
        labelErreur.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelErreur.setForeground(Couleurs.ROUGE);
        labelErreur.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bouton sans Graphics2D
        JButton btnConnexion = Composants.boutonPrincipal("Se connecter");
        btnConnexion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConnexion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnConnexion.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnConnexion.addActionListener(e -> seConnecter());
        champMotDePasse.addActionListener(e -> seConnecter());
        champCin.addActionListener(e -> champMotDePasse.requestFocus());

        carte.add(lblCin);
        carte.add(Box.createVerticalStrut(6));
        carte.add(champCin);
        carte.add(Box.createVerticalStrut(14));
        carte.add(lblMdp);
        carte.add(Box.createVerticalStrut(6));
        carte.add(champMotDePasse);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelErreur);
        carte.add(Box.createVerticalStrut(6));
        carte.add(btnConnexion);

        JPanel enveloppe = new JPanel(new BorderLayout());
        enveloppe.setOpaque(false);
        enveloppe.setBorder(new EmptyBorder(0, 14, 22, 14));
        enveloppe.add(carte, BorderLayout.CENTER);
        return enveloppe;
    }

    private JLabel etiqForm(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Couleurs.TEXTE_GRIS);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // Charge uniquement depuis /logo.png dans les ressources — pas de dessin
    private JLabel chargerLogo(int taille) {
        try {
            InputStream is = getClass().getResourceAsStream("/logo.png");
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaled = img.getScaledInstance(taille, taille, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            System.out.println("Logo non trouve dans les ressources : " + e.getMessage());
        }
        // Aucun dessin — label vide si logo absent
        return new JLabel();
    }

    private void seConnecter() {
        String cin = champCin.getText().trim();
        String mdp = new String(champMotDePasse.getPassword());

        if (cin.isEmpty() || mdp.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            labelErreur.setForeground(Couleurs.ROUGE);
            return;
        }

        labelErreur.setText("Connexion en cours...");
        labelErreur.setForeground(Couleurs.VERT_MED);

        GestionUtilisateur gestion = new GestionUtilisateur();
        Utilisateur u = gestion.connecter(cin, mdp);

        if (u != null) {
            dispose();
            switch (u.getRole()) {
                case "ADMINISTRATEUR":
                    new FenetreAdmin(u).setVisible(true);
                    break;
                case "MEDECIN":
                    new FenetreMedecin(u).setVisible(true);
                    break;
                default:
                    new FenetreSecretaire(u).setVisible(true);
                    break;
            }
        } else {
            labelErreur.setText("Identifiant ou mot de passe incorrect.");
            labelErreur.setForeground(Couleurs.ROUGE);
            champMotDePasse.setText("");
        }
    }
}
