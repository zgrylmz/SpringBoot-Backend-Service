package com.zgrylmz.springSecurity.controller.UsersControllerImpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zgrylmz.springSecurity.controller.IUsersController;
import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.AgeWithUsernameDto.CallAgeWithUsernameDto;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.service.IUsersService;

@RestController
@RequestMapping("/api/v1")
public class UsersController implements IUsersController {

	@Autowired
	private IUsersService userService;

	@PostMapping("/register")
	public ResponseEntity<UserDto> newUserRegister(@RequestBody UserDtoIU user) {
		UserDto dtoUser = userService.newUserRegister(user);
		return ResponseEntity.ok(dtoUser);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> userLogin(@RequestBody AuthRequest loginUser) {
		AuthResponse answer = userService.userLogin(loginUser);
		return ResponseEntity.ok().body(answer);
	}

	@GetMapping("/getUserWithId/{id}")
	public ResponseEntity<CallAgeWithUsernameDto> callJust2Infos(@PathVariable(name = "id") Long id) {
		CallAgeWithUsernameDto resultData = userService.callJust2Infos(id);
		return ResponseEntity.ok().body(resultData);
	}

	@PostMapping("/newToken")
	public String regenerateNewAccessToken(@RequestBody Map<String, String> body) {

		String result = userService.regenerateNewAccessToken(body.get("refreshToken"));
		return result;
	}

	@GetMapping("/getUser")
	public String getUsername() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return "all users" + " " + "Welcome: " + username;
	}

	@GetMapping("/adminOnly")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminOnly() {

		return "This endpoint just for admin";
	}

	@GetMapping("/userOnly")
	@Secured("ROLE_USER")
	public String userOnly() {
		return "This endpoint just for users";
	}

//	@PostMapping("/updateInfos")
//	@PreAuthorize("#updateInfos.username == authentication.name or hasRole('ADMIN')")
//	public UserDto updateYourInfos(@RequestBody UserDtoIU updateInfos) {
//
//		return userService.updateYourInfos(updateInfos);
//	}

	@PostMapping("/updateInfos")
	@PreAuthorize("isAuthenticated() or hasRole('ADMIN')") // sadece giriş yapmış kullanıcılar
	public UserDto updateYourInfos(@RequestBody UserDtoIU updateInfos,
			@AuthenticationPrincipal String currentUsername) {

		// JWT’den gelen username'i service'e gönderiyoruz
		return userService.updateYourInfos(updateInfos, currentUsername);
	}

	@DeleteMapping("/deleteUserWithId/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable(name = "id") Long id) {
		userService.deleteUser(id);
		return ResponseEntity.status(200).body("User has been deleted");
	}

	@GetMapping("/getAllusersForRedis")
	public List<UserDto> getAllUsersForRedis() {
		List<UserDto> alluser = userService.getAllUsers();
		return alluser;
	}

	@Override
	@GetMapping("/getOneUserForRedis/{id}")
	public ResponseEntity<UserDto> getUserById(@PathVariable(name = "id") Long id) {
		UserDto userDto = userService.getUserById(id);
		return ResponseEntity.status(200).body(userDto);
	}

	@GetMapping("/home")
	public String home() {
		return "OK";
	}

	@Override
	@GetMapping("/getAllUsers")
	public List<UserDto> getAllUsers() {
		List<UserDto> users = userService.getAllUsers();
		return users;
	}

}
