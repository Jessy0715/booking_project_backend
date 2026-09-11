package com.jessy.booking_project.exception;

/** 所有「找不到 XX」的父類。GlobalExceptionHandler 只認這一個，子類負責帶自己的中文訊息。 */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
