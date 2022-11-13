package org.zihub.routingservice.config;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

//@RestControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ExceptionResponse> handleException(ServiceException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ExceptionResponse(e.getMessage(), e.getStatus().value()));
    }

    public static class ExceptionResponse {

        private final String message;
        public String getMessage() { return message; }

        private final Integer code;
        public Integer getCode() { return code; }

        public ExceptionResponse(String message, Integer code) {
            this.message = message;
            this.code = code;
        }

    }
}
