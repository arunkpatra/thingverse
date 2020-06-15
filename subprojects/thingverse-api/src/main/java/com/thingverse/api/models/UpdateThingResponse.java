package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "UpdateThingResponse", description = "Thing updation response.")
public class UpdateThingResponse {

    @ApiModelProperty(value = "A message from the backend")
    private final String message;

    @JsonCreator
    public UpdateThingResponse(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "message", name = "message", required = false)
    public String getMessage() {
        return message;
    }
}
