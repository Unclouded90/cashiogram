package com.mauro.cashiogram.exception;

public record ErrorResponse (
    int status,
    String message
){
}
