package com.pmt.exception;

/** Levee lorsqu'une operation entre en conflit avec l'etat courant HTTP 409 */
public class ConflitException extends RuntimeException {

    public ConflitException(String message) {
        super(message);
    }
}
