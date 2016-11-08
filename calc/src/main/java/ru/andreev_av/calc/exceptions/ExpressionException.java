package ru.andreev_av.calc.exceptions;

/**
 * Created by Tolik on 05.11.2016.
 */

public class ExpressionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExpressionException() {
    }
    public ExpressionException(String message) {
        super(message);
    }
}
