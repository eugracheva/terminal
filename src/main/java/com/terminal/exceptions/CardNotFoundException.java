package com.terminal.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Карта не найдена")
public class CardNotFoundException extends RuntimeException {
}
