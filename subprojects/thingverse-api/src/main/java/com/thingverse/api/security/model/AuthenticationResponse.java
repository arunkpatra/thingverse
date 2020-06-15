package com.thingverse.api.security.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "AuthenticationResponse", description = "The Authentication response")
public class AuthenticationResponse implements Serializable {

    @ApiModelProperty(value = "User name")
    private final String username;
    @ApiModelProperty(value = "Authentication Token", position = 1)
    private final String token;

    @JsonCreator
    public AuthenticationResponse(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
