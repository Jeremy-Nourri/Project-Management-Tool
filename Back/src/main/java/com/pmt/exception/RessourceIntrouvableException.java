package com.pmt.exception;

/** une ressource demandee n'existe pas HTTP 404. */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
