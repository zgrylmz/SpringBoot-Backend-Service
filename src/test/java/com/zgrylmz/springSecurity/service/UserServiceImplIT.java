package com.zgrylmz.springSecurity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.entity.Users;
import com.zgrylmz.springSecurity.integration.BaseIntegrationTest;
import com.zgrylmz.springSecurity.repository.IUsersRepository;
import com.zgrylmz.springSecurity.security.JwtTokenProvider;
import com.zgrylmz.springSecurity.service.Impl.UserServiceImpl;

@SpringBootTest
public class UserServiceImplIT extends BaseIntegrationTest {

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private IUsersRepository usersRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanDb() {
		usersRepository.deleteAll();
	}

	public class UserFixtures {
//		BUNA Fixture Strategy DENIYOR YANI BU NESNELERE TESTLERDE IHTIYACIMIZ OLACAK O 
//  YÜZDEN SADECE BIRKERE BIR METOD OLUSTURUP O METOD ÜZERINDEN CAGIRIYORUZ DOLAYISIYLA TEKRAR TEKRAR YAZMANA GEREK KALMIYOR

		public static Users createUser() {
			Users u = new Users();
			u.setUsername("selin");
			u.setPassword("123456");
			u.setAge(25);
			u.setJob("Öğretmen");
			return u;
		}

		public static AuthRequest createLoginRequest() {
			AuthRequest req = new AuthRequest();
			req.setUsername("selin");
			req.setPassword("123456");
			return req;
		}

		public static UserDtoIU createUpdateRequest() {
			UserDtoIU dto = new UserDtoIU();
			dto.setUsername("ozgur");
			dto.setAge(30);
			dto.setJob("Developer");
			return dto;
		}
	}

	Users u = UserFixtures.createUser();

	@Test
	void newUserRegister_shouldCreateUser_andEncodePassword() {

		UserDtoIU dto = new UserDtoIU();
		dto.setUsername("selin");
		dto.setPassword("mySecret");
		dto.setJob("örtmen");
		dto.setAge(31);

		UserDto result = userService.newUserRegister(dto);

		Optional<Users> userDb = usersRepository.findById(result.getId());
		assertThat(userDb).isPresent();
		assertThat(userDb.get().getPassword()).isNotEqualTo("mySecret");
		assertThat(userDb.get().getRole()).isEqualTo("USER");

	}

	@Test
	void newUserRegister_shouldFail_whenUsernameAlreadyExists() {
		// given
		UserDtoIU dto1 = new UserDtoIU(null, "selin", "pass1", "Teacher", 28);
		UserDtoIU dto2 = new UserDtoIU(null, "selin", "pass2", "Lawyer", 35);

		userService.newUserRegister(dto1);

		// when / then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.newUserRegister(dto2));

		assertThat(exception.getMessage()).isEqualTo("Username is already taken");
	}

	@Test
	void userLogin_shouldReturnAccessAndRefreshToken() {

		UserDtoIU userRegister = new UserDtoIU();
		userRegister.setUsername("selin");
		userRegister.setPassword("mySecret");
		userRegister.setJob("örtmen");
		userRegister.setAge(31);

		userService.newUserRegister(userRegister);

		AuthRequest loginRequest = new AuthRequest();
		loginRequest.setUsername(userRegister.getUsername());
		loginRequest.setPassword(userRegister.getPassword());

		AuthResponse loginResponse = userService.userLogin(loginRequest);

		Optional<Users> loginUser = usersRepository.findByUsername(loginRequest.getUsername());

		assertThat(loginUser).isPresent();
		assertThat(loginUser.get().getPassword()).isNotEqualTo("mySecret");
		assertThat(passwordEncoder.matches(loginRequest.getPassword(), loginUser.get().getPassword())).isTrue();
		assertThat(loginResponse.getAccessToken()).isNotNull();
		assertThat(loginResponse.getRefreshToken()).isNotNull();
		assertThat(loginResponse.getAccessToken()).contains(".");

		String usernameFromAccess = jwtTokenProvider.getUsernameFromAccessToken(loginResponse.getAccessToken());
		assertThat(usernameFromAccess).isEqualTo("selin");

	}

	@Test
	void userLogin_shouldFail_UsernameTrue_passwordFalse() {
		UserDtoIU userRegister = new UserDtoIU();
		userRegister.setUsername("selin");
		userRegister.setPassword("mySecret");
		userRegister.setJob("örtmen");
		userRegister.setAge(31);

		userService.newUserRegister(userRegister);

		AuthRequest loginRequest = new AuthRequest();
		loginRequest.setUsername(userRegister.getUsername());
		loginRequest.setPassword("falsePassword");

		Optional<Users> loginUser = usersRepository.findByUsername(loginRequest.getUsername());

		assertThat(loginUser).isPresent();
		Boolean checkPassword = passwordEncoder.matches(userRegister.getPassword(), loginRequest.getPassword());
		assertThat(checkPassword).isFalse();

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			userService.userLogin(loginRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("Username or password not valid");

	}

	@Test
	void update_allFieldsAreUpdated() {

		UserDtoIU userRegister = new UserDtoIU();
		userRegister.setUsername("selin");
		userRegister.setPassword("mySecret");
		userRegister.setJob("örtmen");
		userRegister.setAge(31);

		userService.newUserRegister(userRegister);

		AuthRequest loginRequest = new AuthRequest();
		loginRequest.setUsername(userRegister.getUsername());
		loginRequest.setPassword(userRegister.getPassword());

		Optional<Users> loginUser = usersRepository.findByUsername(loginRequest.getUsername());
		assertThat(loginUser).isPresent();
		assertThat(passwordEncoder.matches(loginRequest.getPassword(), loginUser.get().getPassword())).isTrue();

		String currentUserName = loginUser.get().getUsername();

		UserDtoIU updateData = new UserDtoIU();

		updateData.setUsername("newUserName");
		updateData.setAge(30);
		updateData.setJob("abulkat");

		Boolean checkNewUserName = usersRepository.existsByUsername(updateData.getUsername());
		assertThat(checkNewUserName).isFalse();

		UserDto result = userService.updateYourInfos(updateData, currentUserName);
		assertThat(result.getUsername()).isEqualTo("newUserName");
		assertThat(result.getAge()).isEqualTo(30);
		assertThat(result.getJob()).isEqualTo("abulkat");

	}

	@Test
	void update_shouldFail_usernameAlreadyTaken() {

		UserDtoIU userRegister = new UserDtoIU();
		userRegister.setUsername("selin");
		userRegister.setPassword("mySecret");
		userRegister.setJob("örtmen");
		userRegister.setAge(31);

		UserDtoIU secondUserRegister = new UserDtoIU(null, "selinNew", "mySecret", "mühendis", 31);

		userService.newUserRegister(userRegister);
		userService.newUserRegister(secondUserRegister);

		AuthRequest loginRequest = new AuthRequest();
		loginRequest.setUsername(userRegister.getUsername());
		loginRequest.setPassword(userRegister.getPassword());

		Optional<Users> loginUser = usersRepository.findByUsername(loginRequest.getUsername());
		assertThat(loginUser).isPresent();
		assertThat(passwordEncoder.matches(loginRequest.getPassword(), loginUser.get().getPassword())).isTrue();

		UserDtoIU updateData = new UserDtoIU();
		updateData.setUsername("selinNew");
		updateData.setJob("yeni meslek");

		RuntimeException ex = assertThrows(RuntimeException.class, () -> {
			userService.updateYourInfos(updateData, "selin");
		});

		assertThat(ex.getMessage()).isEqualTo("This username is already taken");

	}

}
