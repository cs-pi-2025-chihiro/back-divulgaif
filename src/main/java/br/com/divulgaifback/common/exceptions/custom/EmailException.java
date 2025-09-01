package br.com.divulgaifback.common.exceptions.custom;

public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
}
