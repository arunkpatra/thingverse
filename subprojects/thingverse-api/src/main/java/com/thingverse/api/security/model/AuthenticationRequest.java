package com.thingverse.api.security.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "AuthenticationRequest", description = "The Authentication request")
public class AuthenticationRequest implements Serializable {

    @ApiModelProperty(value = "User name")
    private String username;

    @ApiModelProperty(value = "Password", position = 1)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
