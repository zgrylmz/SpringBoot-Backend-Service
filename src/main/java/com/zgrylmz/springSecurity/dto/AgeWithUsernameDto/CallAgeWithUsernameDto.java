package com.zgrylmz.springSecurity.dto.AgeWithUsernameDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallAgeWithUsernameDto {

	private String username;
	private Integer age;
}
