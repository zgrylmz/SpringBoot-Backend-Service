package com.zgrylmz.springSecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoIU {

	private Long id;
	private String username;
	private String password;
	private String job;
	private Integer age;
}
