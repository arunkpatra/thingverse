package com.thingverse.api.controllers;

import com.thingverse.common.exception.ThingverseBackendException;
import com.thingverse.common.exception.ThingverseBadRequestException;
import com.thingverse.api.models.ErrorResponse;
import com.thingverse.common.grpc.GrpcStatus;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.ExecutionException;

@RequestMapping("/api")
public abstract class AbstractThingverseRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThingverseRestController.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ThingverseBadRequestException.class)
    public ErrorResponse handleBadRequestException(ThingverseBadRequestException e) {
        String message = extractMessage(e);
        LOGGER.error("Error: {}", message);
        return new ErrorResponse("Invalid request.", message, "");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ThingverseBackendException.class)
    public ErrorResponse handleThingverseBackendException(ThingverseBackendException e) {
        String message = "Backend Unavailable";
        String errorDetail = "";
        if ((null != e.getCause()) && (e.getCause() instanceof ExecutionException)) {
            ExecutionException ee = (ExecutionException) e.getCause();
            if ((null != ee.getCause()) && (ee.getCause() instanceof StatusRuntimeException)) {
                StatusRuntimeException sre = (StatusRuntimeException) ee.getCause();
                GrpcStatus grpcStatus = GrpcStatus.from(sre);
                errorDetail = grpcStatus.toString();
            }
        }
        //message = extractMessage(e);
        LOGGER.error("Error occurred: description={}, detail={}", message, errorDetail);
        return new ErrorResponse("An error was encountered.", message, errorDetail);
    }

    private String extractMessage(Exception e) {
        String message = "";
        if (null != e.getMessage()) {
            message = e.getMessage();
        } else {
            // does it have a cause?
            if (null != e.getCause()) {
                if (null != e.getCause().getMessage()) {
                    message = e.getCause().getMessage();
                }
            }
        }
        return message;
    }
}
