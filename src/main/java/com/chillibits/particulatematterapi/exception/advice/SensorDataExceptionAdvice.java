/*
 * Copyright © Marc Auberer 2019 - 2020. All rights reserved
 */

package com.chillibits.particulatematterapi.exception.advice;

import com.chillibits.particulatematterapi.exception.exception.SensorDataException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class SensorDataExceptionAdvice {
    @ResponseBody
    @ExceptionHandler(SensorDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handler(SensorDataException e) { return e.getMessage(); }
}