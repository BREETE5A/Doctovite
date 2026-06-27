import view.auth.FenetreConnexion;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Look and feel non disponible : " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            FenetreConnexion fenetre = new FenetreConnexion();
            fenetre.setVisible(true);
        });
    }
}
