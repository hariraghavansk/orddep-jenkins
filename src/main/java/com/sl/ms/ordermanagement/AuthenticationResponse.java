package com.sl.ms.ordermanagement;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AuthenticationResponse implements Serializable {

    private final String jwt;

    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}
