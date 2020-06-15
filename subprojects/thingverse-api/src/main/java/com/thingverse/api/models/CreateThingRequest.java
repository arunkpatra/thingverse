package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ApiModel(value = "CreateThingRequest", description = "Thing creation request.")
public class CreateThingRequest {

    @ApiModelProperty(value = "The attribute map to be set")
    private final Map<String, Object> attributes;

    @JsonCreator(mode = PROPERTIES)
    public CreateThingRequest(@JsonProperty("attributes") Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @ApiModelProperty(value = "attributes", name = "attributes")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
