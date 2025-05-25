package br.com.divulgaifback.common.exceptions.custom;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Unauthorized");
    }
}