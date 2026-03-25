package deception.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("error_stacktrace.txt"), stackTrace);
        } catch (Exception ex) {
            // ignore
        }
        
        return ResponseEntity.status(500).body(stackTrace);
    }
}
