/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.common.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static io.grpc.Status.Code.OK;

public class GrpcStatus {
    private Status.Code statusCode = OK;
    private int statusCodeValue = 0;
    private String description = "OK";
    private String errorCauseDescription = "";

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
