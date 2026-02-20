package com.zgrylmz.springSecurity.service;

import java.util.List;

import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.AgeWithUsernameDto.CallAgeWithUsernameDto;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;

public interface IUsersService {

	public UserDto newUserRegister(UserDtoIU userRegister);

	public AuthResponse userLogin(AuthRequest loginUser);

	public CallAgeWithUsernameDto callJust2Infos(Long id);

	public String regenerateNewAccessToken(String refreshToken);

	public UserDto updateYourInfos(UserDtoIU infosUpdate, String username);

	public void deleteUser(Long id);

	public List<UserDto> getAllUsers();

	public UserDto getUserById(Long id);

}
