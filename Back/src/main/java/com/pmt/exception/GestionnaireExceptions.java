package com.pmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduit les exceptions metier en reponses HTTP coherentes,
 * pour toute l'application.
 */
@RestControllerAdvice
public class GestionnaireExceptions {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> introuvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<Map<String, Object>> conflit(ConflitException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<Map<String, Object>> accesRefuse(AccesRefuseException e) {
        return reponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Erreurs de validation des DTO @Valid. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> champs.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> corps = corpsDeBase(HttpStatus.BAD_REQUEST, "Donnees invalides");
        corps.put("champs", champs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(corpsDeBase(statut, message));
    }

    private Map<String, Object> corpsDeBase(HttpStatus statut, String message) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("timestamp", LocalDateTime.now());
        corps.put("status", statut.value());
        corps.put("error", statut.getReasonPhrase());
        corps.put("message", message);
        return corps;
    }
}
