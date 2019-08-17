package com.github.lolo.ltsv;

public class ParseLtsvException extends RuntimeException {

    public ParseLtsvException(String message) {
        super(message);
    }

    public ParseLtsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
