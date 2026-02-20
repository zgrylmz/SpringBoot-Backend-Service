package com.zgrylmz.springSecurity.exception.specialExceptionHandler;

public class BaseException extends RuntimeException {

	public BaseException() {

	}

	public BaseException(ErrorMessage errorMessage) {
		super(errorMessage.prepareErrorMessage());
	}
}
