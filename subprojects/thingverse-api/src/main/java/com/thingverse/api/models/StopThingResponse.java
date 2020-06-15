package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "StopThingResponse", description = "Result of thing stoppage")
public class StopThingResponse {

    @ApiModelProperty(value = "A message from the backend")
    private final String message;

    @JsonCreator
    public StopThingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
