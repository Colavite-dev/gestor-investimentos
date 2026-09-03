package com.colavite.gestor_investimento.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "Requisição inválida", request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            int separator = field.lastIndexOf('.');
            fieldErrors.put(separator >= 0 ? field.substring(separator + 1) : field, violation.getMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "Parâmetro inválido", request, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Parâmetro inválido: " + exception.getName(),
                request,
                Map.of(exception.getName(), "Valor incompatível com o tipo esperado")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", request, Map.of());
    }

    @ExceptionHandler(CorretoraNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            CorretoraNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AcaoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAcaoNotFound(AcaoNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CnpjDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            CnpjDuplicadoException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AcaoDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleAcaoDuplicada(AcaoDuplicadaException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CarteiraNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCarteiraNotFound(CarteiraNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CarteiraDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleCarteiraDuplicada(CarteiraDuplicadaException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(OperacaoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOperacaoNotFound(OperacaoNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(SaldoInsuficienteParaVendaException.class)
    public ResponseEntity<ApiErrorResponse> handleSaldoInsuficienteParaVenda(SaldoInsuficienteParaVendaException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(TickerAmbiguoException.class)
    public ResponseEntity<ApiErrorResponse> handleTickerAmbiguo(TickerAmbiguoException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(StockTickerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStockTickerNotFound(StockTickerNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidStockDataResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStockData(InvalidStockDataResponseException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(StockProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleStockUnavailable(StockProviderUnavailableException exception, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidQuoteHistoryRangeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQuoteHistoryRange(InvalidQuoteHistoryRangeException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CnpjNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCnpjNotFound(
            CnpjNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCnpjResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCnpjResponse(
            InvalidCnpjResponseException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CnpjProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleCnpjProviderUnavailable(
            CnpjProviderUnavailableException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CepNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCepNotFound(CepNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCepResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCepResponse(InvalidCepResponseException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CepProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleCepProviderUnavailable(CepProviderUnavailableException exception, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CvmParticipantNotAcceptedException.class)
    public ResponseEntity<ApiErrorResponse> handleCvmParticipantNotAccepted(CvmParticipantNotAcceptedException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCvmResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCvmResponse(InvalidCvmResponseException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CvmProviderUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleCvmProviderUnavailable(CvmProviderUnavailableException exception, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
