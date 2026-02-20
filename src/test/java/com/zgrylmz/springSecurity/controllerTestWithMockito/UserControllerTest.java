package com.zgrylmz.springSecurity.controllerTestWithMockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgrylmz.springSecurity.controller.UsersControllerImpl.UsersController;
import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.entity.Users;
import com.zgrylmz.springSecurity.service.IUsersService;

//BU ASLINDA NERDEYSE UNIT TEST KIVAMINDA BIR CONTROLLER TESTI COK IZOLE REPO,DB FALAN 
//HIC TEST EDILMIYOR AYRICA SECURITYFILTER VEYA JWTAUTHENTICATION FILTER'LAR FALAN HIC TEST EDILMIYOR COK IZOLE BIR TEST

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private MockMvc mockMvc;

	@Mock
	private IUsersService usersService;

	@InjectMocks
	private UsersController usersController;

	private ObjectMapper objectMapper = new ObjectMapper();

	public class UserFixtures {
		public static Users createUser() {
			Users u = new Users();
			u.setUsername("zgrylmz");
			u.setPassword("123456");
			u.setAge(30);
			u.setJob("marangoz");
			return u;
		}

		public static AuthRequest createLoginRequest() {
			AuthRequest req = new AuthRequest();
			req.setUsername("zgrylmz");
			req.setPassword("123456");
			return req;
		}

		public static UserDtoIU createRegisterRequest() {
			UserDtoIU dto = new UserDtoIU();
			dto.setUsername("zgrylmz");
			dto.setPassword("123456");
			dto.setAge(30);
			dto.setJob("marangoz");
			return dto;
		}

		public static UserDto createUserDto() {
			UserDto dto = new UserDto();
			dto.setUsername("zgrylmz");
			dto.setAge(30);
			dto.setJob("marangoz");
			return dto;
		}

	}

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(usersController).build();
	}

	@Test
	void userLogin_success() throws Exception {
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("özgür");
		authRequest.setPassword("123456");

		AuthResponse authResponse = new AuthResponse();
		authResponse.setAccessToken("accessToken123");
		authResponse.setRefreshToken("refreshTokenXYZ");

		when(usersService.userLogin(any(AuthRequest.class))).thenReturn(authResponse);

		mockMvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(authRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("accessToken123"))
				.andExpect(jsonPath("$.refreshToken").value("refreshTokenXYZ"));

	}

	@Test
	void userRegister_success() throws Exception {
		UserDtoIU registerRequest = UserFixtures.createRegisterRequest();
		UserDto dto = UserFixtures.createUserDto();
		when(usersService.newUserRegister(any(UserDtoIU.class))).thenReturn(dto);

		mockMvc.perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(dto.getUsername()))
				.andExpect(jsonPath("$.age").value(dto.getAge())).andExpect(jsonPath("$.job").value(dto.getJob()));
	}

	@Test
	void adminOnly_success() throws Exception {

		mockMvc.perform(get("/api/v1/userOnly").with(user("User").roles("admin"))).andExpect(status().isOk())
				.andExpect(content().string("This endpoint just for users"));

	}
}
