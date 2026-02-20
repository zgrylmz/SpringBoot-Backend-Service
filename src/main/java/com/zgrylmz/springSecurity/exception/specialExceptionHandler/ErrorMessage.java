package com.zgrylmz.springSecurity.exception.specialExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

	private MessageType messageType;

	private String errorDetail;

	public String prepareErrorMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(messageType.getMessage());

		if (!errorDetail.isEmpty() && !errorDetail.isBlank()) {
			builder.append(" : ").append(errorDetail);
		}

		return builder.toString();
	}
}
