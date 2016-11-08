package ru.andreev_av.calc.exceptions;

/**
 * Created by Tolik on 04.11.2016.
 */

public class DivisionByZeroException extends ArithmeticException {

    private static final long serialVersionUID = 1L;

    public DivisionByZeroException() {
    }

    public DivisionByZeroException(String message) {
        super(message);
    }
}
