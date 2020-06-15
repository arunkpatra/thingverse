package com.thingverse.backend.models;

import java.util.Map;

public class CreateThing {

    public final String thingID;
    public final Map<String, Object> attributes;

    public CreateThing(String thingID, Map<String, Object> attributes) {
        this.thingID = thingID;
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "CreateThing{" +
                "thingID='" + thingID + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
