package com.example.auth_spring.controller;

import jakarta.servlet.http.HttpServletRequest;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.nio.file.FileStore;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        model.addAttribute("errorMessage", ex.getMessage()==null?"Произошла ошибка, попробуйте снова":ex.getMessage());
        model.addAttribute("errorCode", statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error";
    }



    // Обработка 404
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        model.addAttribute("errorMessage", "Страница не найдена.");
        model.addAttribute("errorCode", statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex,HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        model.addAttribute("errorMessage", "Внутренняя ошибка сервера. Обратитесь в поддержку.");
        model.addAttribute("errorCode", statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error";
    }
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbiddenException(RuntimeException ex, HttpServletRequest request,  Model model) {

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        model.addAttribute("errorMessage", "Ошибка доступа. Недостаточно прав на совершение действия");
        model.addAttribute("errorCode", statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value());

        return "error";
    }
}
