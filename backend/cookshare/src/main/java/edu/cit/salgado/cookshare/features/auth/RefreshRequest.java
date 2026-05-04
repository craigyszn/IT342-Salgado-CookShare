package edu.cit.salgado.cookshare.features.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {
    private String refreshToken;
}