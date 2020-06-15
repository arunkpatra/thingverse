package com.thingverse.common.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static io.grpc.Status.Code.OK;

public class GrpcStatus {
    private Status.Code statusCode = OK;
    private int statusCodeValue = 0;
    private String description = "OK";
    private String errorCauseDescription = "";

    public Status.Code getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Status.Code statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCodeValue() {
        return statusCodeValue;
    }

    public void setStatusCodeValue(int statusCodeValue) {
        this.statusCodeValue = statusCodeValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorCauseDescription() {
        return errorCauseDescription;
    }

    public void setErrorCauseDescription(String errorCauseDescription) {
        this.errorCauseDescription = errorCauseDescription;
    }

    public static GrpcStatus from(StatusRuntimeException sre) {
        GrpcStatus gs = new GrpcStatus();
        gs.setStatusCode(sre.getStatus().getCode());
        gs.setStatusCodeValue(sre.getStatus().getCode().value());
        gs.setDescription(sre.getStatus().getDescription());
        if (null != sre.getCause()) {
            gs.setErrorCauseDescription(sre.getCause().getMessage());
        }
        return gs;
    }
    @Override
    public String toString() {
        return "GrpcStatus{" +
                "statusCode=" + statusCode +
                ", statusCodeValue=" + statusCodeValue +
                ", description='" + description + '\'' +
                ", causeDescription='" + errorCauseDescription + '\'' +
                '}';
    }
}
