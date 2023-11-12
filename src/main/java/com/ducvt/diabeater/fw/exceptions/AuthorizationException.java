package com.ducvt.diabeater.fw.exceptions;

public class AuthorizationException extends ApplicationException{
    public AuthorizationException(String code) {
        super(code);
    }
}
