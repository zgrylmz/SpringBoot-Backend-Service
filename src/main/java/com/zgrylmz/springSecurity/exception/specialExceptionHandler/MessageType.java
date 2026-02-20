package com.zgrylmz.springSecurity.exception.specialExceptionHandler;

import lombok.Getter;

@Getter
public enum MessageType {

	Username_Exist("Username is already taken");

	private final String message;

	MessageType(String message) {
		this.message = message;
	}

}
