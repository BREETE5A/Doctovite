package controller;

import model.Medecin;
import model.Notification;
import model.RendezVous;
import model.Utilisateur;

import java.util.List;

/**
 * ServiceNotificationRdv — Envoie automatiquement des notifications
 * quand un rendez-vous est cree, modifie, ou supprime.
 *
 * Regles d'envoi :
 *   - Si un MEDECIN agit  : on notifie tous les ADMINISTRATEURS et la SECRETAIRE
 *   - Si une SECRETAIRE agit : on notifie le medecin concerne et les ADMINISTRATEURS
 *   - Si un ADMINISTRATEUR agit : on notifie le medecin concerne et la SECRETAIRE
 *
 * On evite d'envoyer une notification a la personne qui a fait l'action elle-meme.
 */
public class ServiceNotificationRdv {

    private GestionNotification gestionNotif = new GestionNotification();
    private GestionMedecin gestionMedecin = new GestionMedecin();
    private GestionUtilisateur gestionUtil = new GestionUtilisateur();

    // ── NOTIFIER UNE CREATION DE RDV ──────────────────────────────────────────
    public void notifierCreation(RendezVous rdv, Utilisateur expediteur) {
        String message = "Nouveau RDV : " + rdv.getNomPatient() +
                         " le " + rdv.getDateRdv() + " a " + rdv.getHeureRdv();
        if (rdv.getMotif() != null && !rdv.getMotif().isEmpty()) {
            message = message + " - Motif : " + rdv.getMotif();
        }
        envoyerSelonRole(rdv, expediteur, message, "RDV");
    }

    // ── NOTIFIER UNE MODIFICATION DE RDV ──────────────────────────────────────
    public void notifierModification(RendezVous rdv, Utilisateur expediteur) {
        String message = "RDV modifie : " + rdv.getNomPatient() +
                         " le " + rdv.getDateRdv() + " a " + rdv.getHeureRdv() +
                         " - Statut : " + rdv.getStatut();
        envoyerSelonRole(rdv, expediteur, message, "RDV");
    }

    // ── NOTIFIER UNE SUPPRESSION OU ANNULATION DE RDV ────────────────────────
    public void notifierSuppression(RendezVous rdv, Utilisateur expediteur) {
        String message = "RDV annule : " + rdv.getNomPatient() +
                         " le " + rdv.getDateRdv() + " a " + rdv.getHeureRdv();
        envoyerSelonRole(rdv, expediteur, message, "RDV");
    }

    // ── METHODE INTERNE : choisir les destinataires selon le role ────────────
    private void envoyerSelonRole(RendezVous rdv, Utilisateur expediteur,
                                  String message, String type) {
        if (expediteur == null) return;

        int idExpediteur = expediteur.getId();
        String role = expediteur.getRole();

        // 1) Notifier le medecin concerne (sauf si c'est lui qui agit)
        Medecin medConcerne = gestionMedecin.trouverParId(rdv.getMedecinId());
        if (medConcerne != null && medConcerne.getUtilisateurId() != idExpediteur) {
            Notification n = new Notification(
                medConcerne.getUtilisateurId(), idExpediteur, message, type
            );
            gestionNotif.envoyer(n);
        }

        // 2) Notifier tous les administrateurs (sauf l'expediteur)
        List<Utilisateur> admins = gestionUtil.listerParRole("ADMINISTRATEUR");
        for (Utilisateur admin : admins) {
            if (admin.getId() != idExpediteur) {
                Notification n = new Notification(
                    admin.getId(), idExpediteur, message, type
                );
                gestionNotif.envoyer(n);
            }
        }

        // 3) Notifier les secretaires (seulement si un medecin ou un admin agit)
        if ("MEDECIN".equals(role) || "ADMINISTRATEUR".equals(role)) {
            List<Utilisateur> secretaires = gestionUtil.listerParRole("SECRETAIRE");
            for (Utilisateur sec : secretaires) {
                if (sec.getId() != idExpediteur) {
                    Notification n = new Notification(
                        sec.getId(), idExpediteur, message, type
                    );
                    gestionNotif.envoyer(n);
                }
            }
        }
    }
}
