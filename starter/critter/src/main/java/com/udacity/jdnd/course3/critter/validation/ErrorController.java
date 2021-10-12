package com.udacity.jdnd.course3.critter.validation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
@ControllerAdvice is a specialization of the @Component annotation which allows
to handle exceptions across the whole application in one global handling component.
It can be viewed as an interceptor of exceptions thrown by methods annotated with @RequestMapping and similar.
ResponseEntityExceptionHandler is a convenient base class for @ControllerAdvice classes
that wish to provide centralized exception handling across all @RequestMapping methods
through @ExceptionHandler methods. It provides an methods for handling internal Spring MVC exceptions.
It returns a ResponseEntity in contrast to DefaultHandlerExceptionResolver which returns a ModelAndView.
 */

@ControllerAdvice
public class ErrorController{

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    protected List<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(
                        Collectors.toList());

        return errors;
    }
}

