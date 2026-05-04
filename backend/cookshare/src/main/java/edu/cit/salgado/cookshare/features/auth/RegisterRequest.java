package edu.cit.salgado.cookshare.features.auth;

import lombok.Data;

@Data
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

}