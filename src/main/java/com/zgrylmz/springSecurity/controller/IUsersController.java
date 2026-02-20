package com.zgrylmz.springSecurity.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.AgeWithUsernameDto.CallAgeWithUsernameDto;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;

public interface IUsersController {

	public ResponseEntity<UserDto> newUserRegister(UserDtoIU userRegister);

	public ResponseEntity<AuthResponse> userLogin(AuthRequest loginUser);

	public ResponseEntity<CallAgeWithUsernameDto> callJust2Infos(Long id);

	public String regenerateNewAccessToken(Map<String, String> body);

	public UserDto updateYourInfos(UserDtoIU infosUpdate, String username);

	public ResponseEntity<String> deleteUser(Long id);

	public List<UserDto> getAllUsersForRedis();

	public ResponseEntity<UserDto> getUserById(Long id);

	public List<UserDto> getAllUsers();

}
