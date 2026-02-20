package com.zgrylmz.springSecurity.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.zgrylmz.springSecurity.exception.specialExceptionHandler.BaseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiError> HandleRuntimeExceptionErrors(RuntimeException ex) {

		Map<String, List<String>> errorList = new HashMap<>();

		errorList.put("RuntimeExceptionErrors", List.of(ex.getMessage()));

		return ResponseEntity.badRequest().body(setApiError(errorList));

	}

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiError> HandleBaseExceptionErrors(BaseException ex) {

		Map<String, List<String>> errorsList = new HashMap<>();

		errorsList.put("BaseExceptionError", List.of(ex.getMessage()));

		return ResponseEntity.badRequest().body(setApiError(errorsList));
	}

	public ApiError setApiError(Map<String, List<String>> errors) {

		ApiError apiError = new ApiError();
		apiError.setId(UUID.randomUUID().toString());
		apiError.setErrorTime(LocalDateTime.now().toString().substring(0, 16));
		apiError.setErrors(errors);

		return apiError;

	}

}
