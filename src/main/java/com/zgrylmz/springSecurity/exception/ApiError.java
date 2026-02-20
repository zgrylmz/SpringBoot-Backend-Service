package com.zgrylmz.springSecurity.exception;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {

	private String id;

	private String errorTime;

	private Map<String, List<String>> errors;
}
