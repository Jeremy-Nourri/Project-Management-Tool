package com.pmt.exception;

/** Levee lorsque le role de l'utilisateur ne permet pas l'action HTTP 403 */
public class AccesRefuseException extends RuntimeException {

    public AccesRefuseException(String message) {
        super(message);
    }
}
