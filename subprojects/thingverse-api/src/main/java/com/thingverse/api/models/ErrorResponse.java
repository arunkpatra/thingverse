package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    @JsonProperty("error")
    private final String error;

    @JsonProperty("error_description")
    private final String errorDescription;

    @JsonProperty("error_detail")
    private final String errorDetail;

    @JsonCreator
    public ErrorResponse(String error, String errorDescription, String errorDetail) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.errorDetail = errorDetail;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorDetail() {
        return errorDetail;
    }
}
