package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class Composants {

    // Bouton vert principal
    public static JButton boutonPrincipal(String texte) {
        return creerBouton(texte, Couleurs.VERT_MED, Couleurs.VERT_FONCE);
    }

    // Bouton rouge danger
    public static JButton boutonDanger(String texte) {
        return creerBouton(texte, Couleurs.ROUGE, Couleurs.ROUGE_FONCE);
    }

    // Bouton vert succes
    public static JButton boutonSucces(String texte) {
        return creerBouton(texte, Couleurs.VERT_BTN, new Color(0x1E, 0x7E, 0x34));
    }

    // Bouton gris neutre
    public static JButton boutonGris(String texte) {
        JButton b = new JButton(texte);
        b.setBackground(new Color(0xEC, 0xF0, 0xF1));
        b.setForeground(Couleurs.TEXTE_NOIR);
        b.setFont(Couleurs.GRAS);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Bouton export PDF (rouge)
    public static JButton boutonPDF(String texte) {
        return creerBouton(texte, new Color(0xC0, 0x39, 0x2B), new Color(0xA0, 0x2E, 0x22));
    }

    // Bouton export Excel (vert)
    public static JButton boutonExcel(String texte) {
        return creerBouton(texte, new Color(0x1E, 0x8B, 0x4C), new Color(0x17, 0x6A, 0x3A));
    }

    // Bouton export PDF / Excel (orange discret) — conserve pour compatibilite
    public static JButton boutonExport(String texte) {
        return creerBouton(texte, new Color(0xE6, 0x7E, 0x22), new Color(0xCA, 0x6F, 0x1E));
    }

    // Création bouton simple sans Graphics2D
    private static JButton creerBouton(String texte, Color normal, Color survol) {
        JButton b = new JButton(texte);
        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setFont(Couleurs.GRAS);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(survol); }
            public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }

    // Champ de texte stylisé sans Graphics2D
    public static JTextField champTexte(String indicateur) {
        JTextField tf = new JTextField() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(Couleurs.TEXTE_PALE);
                    g.setFont(Couleurs.CORPS);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(indicateur, 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            }
        };
        tf.setFont(Couleurs.CORPS);
        tf.setForeground(Couleurs.TEXTE_NOIR);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(Couleurs.VERT_MED);
        tf.setBorder(new CompoundBorder(
            new BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 12, 8, 12)
        ));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(new BordureArrondie(Couleurs.VERT_MED, 2, 8), new EmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(new BordureArrondie(Couleurs.BORDURE, 1, 8), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return tf;
    }

    // Champ mot de passe sans Graphics2D
    public static JPasswordField champMotDePasse() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Couleurs.CORPS);
        pf.setForeground(Couleurs.TEXTE_NOIR);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(Couleurs.VERT_MED);
        pf.setBorder(new CompoundBorder(
            new BordureArrondie(Couleurs.BORDURE, 1, 8),
            new EmptyBorder(8, 12, 8, 12)
        ));
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                pf.setBorder(new CompoundBorder(new BordureArrondie(Couleurs.VERT_MED, 2, 8), new EmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(FocusEvent e) {
                pf.setBorder(new CompoundBorder(new BordureArrondie(Couleurs.BORDURE, 1, 8), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return pf;
    }

    // Tableau stylisé — sans Graphics2D
    public static void styliserTableau(JTable tableau) {
        tableau.setFont(Couleurs.CORPS);
        tableau.setRowHeight(40);
        tableau.setShowGrid(false);
        tableau.setIntercellSpacing(new Dimension(0, 0));
        tableau.setSelectionBackground(Couleurs.VERT_CLAIR);
        tableau.setSelectionForeground(Couleurs.VERT_FONCE);
        tableau.setBackground(Color.WHITE);
        tableau.setFillsViewportHeight(true);

        JTableHeader entete = tableau.getTableHeader();
        entete.setFont(Couleurs.GRAS);
        entete.setBackground(Couleurs.ENTETE_TBL);
        entete.setForeground(Couleurs.TEXTE_NOIR);
        entete.setPreferredSize(new Dimension(0, 44));
        entete.setReorderingAllowed(false);
        entete.setBorder(new MatteBorder(0, 0, 2, 0, Couleurs.VERT_MED));

        tableau.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int lig, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, lig, col);
                if (!sel) {
                    c.setBackground(lig % 2 == 0 ? Color.WHITE : new Color(0xF3, 0xFD, 0xFB));
                    c.setForeground(Couleurs.TEXTE_NOIR);
                }
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return c;
            }
        });
    }

    public static JScrollPane enveloperTableau(JTable tableau) {
        JScrollPane sp = new JScrollPane(tableau);
        sp.setBorder(new BordureArrondie(Couleurs.BORDURE, 1, 10));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // Carte statistique — sans Graphics2D (fond plein via setBackground)
    public static JPanel carteStatistique(String valeur, String libelle, Color couleur, Color couleurF) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(couleur);
        p.setOpaque(true);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel textes = new JPanel(new GridLayout(2, 1, 0, 2));
        textes.setOpaque(false);

        JLabel lVal = new JLabel(valeur);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lVal.setForeground(Color.WHITE);

        JLabel lLib = new JLabel(libelle);
        lLib.setFont(Couleurs.PETIT);
        lLib.setForeground(new Color(255, 255, 255, 200));

        textes.add(lVal);
        textes.add(lLib);
        p.add(textes, BorderLayout.CENTER);
        return p;
    }

    // Titre de page
    public static JLabel titre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Couleurs.TITRE);
        l.setForeground(Couleurs.TEXTE_NOIR);
        return l;
    }

    // Etiquette de champ
    public static JLabel etiquette(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Couleurs.GRAS);
        l.setForeground(Couleurs.TEXTE_GRIS);
        return l;
    }

    // Panneau couleur unie (sans dégradé Graphics2D)
    public static JPanel panneauDegrade(Color c1, Color c2) {
        JPanel p = new JPanel();
        p.setBackground(c1);
        p.setOpaque(true);
        return p;
    }

    // Bordure arrondie (utilise Graphics2D mais uniquement pour la BORDURE, pas le fond)
    public static class BordureArrondie extends AbstractBorder {
        private Color couleur;
        private int epaisseur;
        private int rayon;

        public BordureArrondie(Color couleur, int epaisseur, int rayon) {
            this.couleur = couleur;
            this.epaisseur = epaisseur;
            this.rayon = rayon;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(epaisseur));
            g2.drawRoundRect(x, y, w - 1, h - 1, rayon, rayon);
            g2.dispose();
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(epaisseur + 3, epaisseur + 3, epaisseur + 3, epaisseur + 3);
        }
    }
}
