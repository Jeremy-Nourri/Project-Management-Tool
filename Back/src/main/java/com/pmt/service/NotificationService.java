package com.pmt.service;

import com.pmt.entity.Tache;
import com.pmt.entity.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi des notifications par e-mail.
 *
 * <p>Un echec d'envoi ne doit jamais faire echouer l'operation metier :
 * les exceptions de messagerie sont donc journalisees, pas propagees.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String expediteur;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${pmt.mail.expediteur:no-reply@pmt.local}") String expediteur) {
        this.mailSender = mailSender;
        this.expediteur = expediteur;
    }

    /**
     * Previent un utilisateur qu'une tache lui a ete assignee.
     *
     * @param tache   tache concernee
     * @param assigne destinataire de la notification
     */
    public void notifierAssignation(Tache tache, Utilisateur assigne) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(assigne.getEmail());
        message.setSubject("[PMT] Une tache vous a ete assignee : " + tache.getNom());
        message.setText("""
                Bonjour %s,

                La tache "%s" du projet "%s" vous a ete assignee.

                Echeance : %s
                Priorite : %s

                Project Management Tool
                """.formatted(
                assigne.getNom(),
                tache.getNom(),
                tache.getProjet().getNom(),
                tache.getDateEcheance() == null ? "non definie" : tache.getDateEcheance(),
                tache.getPriorite()));

        try {
            mailSender.send(message);
            log.info("Notification d'assignation envoyee a {}", assigne.getEmail());
        } catch (MailException e) {
            log.warn("Echec de l'envoi de la notification a {} : {}",
                    assigne.getEmail(), e.getMessage());
        }
    }
}
