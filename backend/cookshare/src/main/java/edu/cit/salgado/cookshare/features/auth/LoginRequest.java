package edu.cit.salgado.cookshare.features.auth;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;

}
