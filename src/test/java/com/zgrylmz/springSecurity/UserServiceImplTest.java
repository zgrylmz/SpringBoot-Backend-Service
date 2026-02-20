package com.zgrylmz.springSecurity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.AgeWithUsernameDto.CallAgeWithUsernameDto;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.entity.Users;
import com.zgrylmz.springSecurity.repository.IUsersRepository;
import com.zgrylmz.springSecurity.security.JwtTokenProvider;
import com.zgrylmz.springSecurity.service.Impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	private IUsersRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private UserServiceImpl userService;

//	private Users existingUser;
//	private UserDtoIU updateRequest;
//	private AuthRequest authLogin;

	@BeforeAll
	static void setupAll() {
		System.out.println("UserServiceImplTest is starting");
	}

//	@BeforeEach
//	void setup() {
//		existingUser = new Users();
//		existingUser.setId(1L);
//		existingUser.setAge(31);
//		existingUser.setJob("ögretmen");
//		existingUser.setUsername("selin");
//		existingUser.setPassword("ENCODED");
//
//		updateRequest = new UserDtoIU();
//		authLogin = new AuthRequest();
//	}

	@Nested
	class NewUserRegisterTests {
		@Test
		void newUserRegister_createsUser_whenUsernameIsFree() {

			UserDtoIU input = new UserDtoIU();
			input.setUsername("selin");
			input.setPassword("123456");
			input.setAge(31);
			input.setJob("ögretmen");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.empty());
			when(passwordEncoder.encode("123456")).thenReturn("ENCODED");

			Users saved = new Users();
			saved.setId(1L);
			saved.setUsername("selin");
			saved.setPassword("ENCODED");
			saved.setAge(31);
			saved.setJob("ögretmen");

			when(userRepository.save(any(Users.class))).thenReturn(saved);
			ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

			UserDto result = userService.newUserRegister(input);

			verify(userRepository).findByUsername("selin");
			verify(passwordEncoder).encode("123456");
			verify(userRepository).save(userCaptor.capture());
			verifyNoMoreInteractions(userRepository, passwordEncoder);

			Users toSave = userCaptor.getValue();

			assertEquals("selin", toSave.getUsername());
			assertEquals(31, toSave.getAge());
			assertEquals("ögretmen", toSave.getJob());
			assertEquals("ENCODED", toSave.getPassword());

			assertNotNull(result);
			assertEquals(1L, result.getId());
			assertEquals("selin", result.getUsername());
			assertEquals(31, result.getAge());
			assertEquals("ögretmen", result.getJob());
		}

		@Test
		void newUserRegister_createsUser_whenUsernameIsNotFree() {
			UserDtoIU input = new UserDtoIU();
			input.setAge(31);
			input.setJob("ögretmen");
			input.setUsername("selin");
			input.setPassword("123456");

			Users existingUser = new Users();
			existingUser.setId(1L);
			existingUser.setUsername("selin");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.of(existingUser));

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.newUserRegister(input);
			});

			verify(userRepository).findByUsername("selin");
			verifyNoMoreInteractions(userRepository, passwordEncoder);

			assertEquals("Username is already taken", exception.getMessage());

		}
	}

	@Nested
	class UserLoginTests {
		@Test
		void User_Login_username_and_password_true() {
			AuthRequest authLogin = new AuthRequest();
			authLogin.setUsername("selin");
			authLogin.setPassword("123456");

			Users registeredUser = new Users();
			registeredUser.setUsername("selin");
			registeredUser.setPassword("123456");
			registeredUser.setRole("USER");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.of(registeredUser));
			when(passwordEncoder.matches("123456", "123456")).thenReturn(true);
			when(jwtTokenProvider.generateAccessToken("selin", "USER")).thenReturn("AccessToken");
			when(jwtTokenProvider.generateRefreshToken("selin")).thenReturn("RefreshToken");

			AuthResponse response = userService.userLogin(authLogin);

			assertNotNull(response);
			assertEquals("AccessToken", response.getAccessToken());
			assertEquals("RefreshToken", response.getRefreshToken());

			verify(userRepository).findByUsername("selin");
			verify(passwordEncoder).matches("123456", "123456");
			verify(jwtTokenProvider).generateAccessToken("selin", "USER");
			verify(jwtTokenProvider).generateRefreshToken("selin");
			verifyNoMoreInteractions(userRepository, jwtTokenProvider, passwordEncoder);
		}

		@Test
		void userLogin_throwsException_whenUserDoesNotExist() {

			AuthRequest authLogin = new AuthRequest();
			authLogin.setUsername("zgrylmz");
			authLogin.setPassword("6990148");

			when(userRepository.findByUsername("zgrylmz")).thenReturn(Optional.empty());

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.userLogin(authLogin);
			});

			assertEquals("User doesnt exist", exception.getMessage());
			verify(userRepository).findByUsername("zgrylmz");
			verifyNoMoreInteractions(userRepository, passwordEncoder, jwtTokenProvider);

		}

		@Test
		void userLogin_UserExist_Password_false() {

			AuthRequest authLogin = new AuthRequest();
			authLogin.setUsername("selin");
			authLogin.setPassword("wrong");

			Users existingUser = new Users();
			existingUser.setUsername("selin");
			existingUser.setPassword("ENCODED");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.of(existingUser));
			when(passwordEncoder.matches("wrong", "ENCODED")).thenReturn(false);

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.userLogin(authLogin);
			});

			assertEquals("Username or password not valid", exception.getMessage());

			verify(userRepository).findByUsername("selin");
			verify(passwordEncoder).matches("wrong", "ENCODED");
			verifyNoMoreInteractions(userRepository, passwordEncoder);

		}

		@Test
		void call2Infos_usernameAndAge_IdDoesntExist() {
			Users userRequest = new Users();
			userRequest.setId(1L);
			userRequest.setAge(31);
			userRequest.setUsername("selin");

			when(userRepository.findById(1L)).thenReturn(Optional.empty());

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.callJust2Infos(1L);
			});

			assertEquals("data not found", exception.getMessage());
			verify(userRepository).findById(1L);
			verifyNoMoreInteractions(userRepository);

		}
	}

	@Nested
	class Call2InfosTests {
		@Test
		void call2Infos_usernameAndAge() {

			Users userCallResponse = new Users();
			userCallResponse.setUsername("selin");
			userCallResponse.setAge(31);
			userCallResponse.setId(1L);

			when(userRepository.findById(1L)).thenReturn(Optional.of(userCallResponse));

			CallAgeWithUsernameDto calledData = userService.callJust2Infos(1L);

			assertNotNull(calledData);
			assertEquals("selin", calledData.getUsername());
			assertEquals(31, calledData.getAge());

			verify(userRepository).findById(1L);
			verifyNoMoreInteractions(userRepository);

		}

	}

	@Nested
	class UserUpdateInfos {

		@Test
		void updateYourInfos_throwsException_whenUserDoesntExist() {

			UserDtoIU updateRequest = new UserDtoIU();

			when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.updateYourInfos(updateRequest, "username");
			});

			assertEquals("User doesnt exist", exception.getMessage());
			verify(userRepository).findByUsername("username");
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		void updateYourInfos_throwsException_whenUsernameAlreadyTaken() {

			UserDtoIU updateRequest = new UserDtoIU();
			updateRequest.setUsername("newUsernameRequest");

			Users takenUser = new Users();
			takenUser.setUsername("newUsernameRequest");

			Users loggedUser = new Users();
			loggedUser.setUsername("oldUserName");

			when(userRepository.findByUsername("oldUserName")).thenReturn(Optional.of(loggedUser));
			when(userRepository.findByUsername("newUsernameRequest")).thenReturn(Optional.of(takenUser));

			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				userService.updateYourInfos(updateRequest, "oldUserName");
			});

			assertEquals("This username is already taken", exception.getMessage());

			verify(userRepository).findByUsername("oldUserName");
			verify(userRepository).findByUsername("newUsernameRequest");
			verifyNoMoreInteractions(userRepository);
		}

		@Test
		void updateYourInfos_update_AgeAndJob() {

			UserDtoIU updateRequest = new UserDtoIU();
			updateRequest.setAge(31);
			updateRequest.setJob("örtmen");

			Users existingUser = new Users();
			existingUser.setId(1L);
			existingUser.setAge(25);
			existingUser.setJob("tesisatci");
			existingUser.setPassword("old_encoded");
			existingUser.setUsername("selin");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.of(existingUser));

			Users savedUser = new Users();
			savedUser.setId(1L);
			savedUser.setAge(31);
			savedUser.setJob("örtmen");
			savedUser.setPassword("old_encoded");
			savedUser.setUsername("selin");

			when(userRepository.save(any(Users.class))).thenReturn(savedUser);

			ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

			UserDto result = userService.updateYourInfos(updateRequest, "selin");

			verify(userRepository).findByUsername("selin");
			verify(userRepository).save(userCaptor.capture());
			verifyNoMoreInteractions(userRepository, jwtTokenProvider, passwordEncoder);

			Users updatedEntity = userCaptor.getValue();
			assertEquals(1L, updatedEntity.getId());
			assertEquals(31, updatedEntity.getAge());
			assertEquals("örtmen", updatedEntity.getJob());
			assertEquals("old_encoded", updatedEntity.getPassword());
			assertEquals("selin", updatedEntity.getUsername());

			assertNotNull(result);
			assertEquals(1L, result.getId());
			assertEquals(31, result.getAge());
			assertEquals("örtmen", result.getJob());
			assertEquals("selin", result.getUsername());

		}

		@Test
		void updateYourInfoy_update_usernameAndPassword() {
			UserDtoIU updateRequest = new UserDtoIU();
			updateRequest.setUsername("new_Username");
			updateRequest.setPassword("new_Password");

			Users existingUser = new Users();
			existingUser.setId(1L);
			existingUser.setAge(31);
			existingUser.setJob("örtmen");
			existingUser.setPassword("old_encoded_password");
			existingUser.setUsername("selin");

			when(userRepository.findByUsername("selin")).thenReturn(Optional.of(existingUser));
			when(userRepository.findByUsername("new_Username")).thenReturn(Optional.empty());
			when(passwordEncoder.encode("new_Password")).thenReturn("new_Password_encoded");

			Users saveduser = new Users();
			saveduser.setId(1L);
			saveduser.setAge(31);
			saveduser.setJob("örtmen");
			saveduser.setPassword("new_Password");
			saveduser.setUsername("new_Username");

			when(userRepository.save(any(Users.class))).thenReturn(saveduser);

			ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

			UserDto result = userService.updateYourInfos(updateRequest, "selin");

			verify(userRepository).findByUsername("selin");
			verify(userRepository).findByUsername("new_Username");
			verify(userRepository).save(userCaptor.capture());// verify(...capture()) çağrısı sayesinde save() metoduna
																// gönderilen güncellenmiş newUser nesnesi captor’a
																// setleniyor ve biz captor.getValue() ile onu
																// okuyabiliyoruz.
			verify(passwordEncoder).encode("new_Password");

			Users updatedEntity = userCaptor.getValue();
			assertEquals(1L, updatedEntity.getId());
			assertEquals(31, updatedEntity.getAge());
			assertEquals("örtmen", updatedEntity.getJob());
			assertEquals("new_Password_encoded", updatedEntity.getPassword());
			assertEquals("new_Username", updatedEntity.getUsername());

			assertNotNull(result);
			assertEquals(1L, result.getId());
			assertEquals(31, result.getAge());
			assertEquals("örtmen", result.getJob());
			assertEquals("new_Username", result.getUsername());

		}
	}

}
