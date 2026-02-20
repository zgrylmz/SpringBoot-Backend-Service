package com.zgrylmz.springSecurity.UsersControllerIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.integration.BaseIntegrationTest;
import com.zgrylmz.springSecurity.repository.IUsersRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerITTest extends BaseIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	IUsersRepository usersRepository;

	@BeforeEach
	void setup() {
		usersRepository.deleteAll();
	}

	private MvcResult createUserInSystem() throws Exception {
		// andReturn() → mockMvc.perform() sonucunda oluşan MvcResult nesnesini
		// döndürür.

		UserDtoIU newUser = new UserDtoIU(null, "zgrylmz", "123456", "developer", 30);

		MvcResult result = mockMvc.perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newUser))).andExpect(status().isOk()).andReturn();

		return result;

	}

	public static class createAuhRequest {
		public static AuthRequest createAuth() {
			AuthRequest req = new AuthRequest("zgrylmz", "123546456");

			return req;
		}

	}

	@Test
	void registerUser() throws Exception {

		MvcResult resultCatch = createUserInSystem();
		String json = resultCatch.getResponse().getContentAsString();
		UserDto created = objectMapper.readValue(json, UserDto.class);

		assertThat(created.getUsername()).isEqualTo("zgrylmz");
		assertThat(created.getAge()).isEqualTo(30);
	}

	@Test
	void afterLogin_GetTokens() throws Exception {

		createUserInSystem();
		AuthRequest req = new AuthRequest("zgrylmz", "123456");

		mockMvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.refreshToken").exists())
				.andReturn();

	}

	private String loginGetAccessToken() throws Exception {

		createUserInSystem();
		AuthRequest req = new AuthRequest("zgrylmz", "123456");

		MvcResult result = mockMvc
				.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists()).andReturn();

		String token = result.getResponse().getContentAsString();
		AuthResponse auth = objectMapper.readValue(token, AuthResponse.class);
		return auth.getAccessToken();

	}

	@Test
	void userOnly_withValidJwt_shouldReturn200() throws Exception {
		String token = loginGetAccessToken();
		mockMvc.perform(get("/api/v1/userOnly").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
				.andExpect(content().string("This endpoint just for users"));
	}

	@Test
	void LoginFailed_WrongPassword() throws Exception {
		createUserInSystem();
		AuthRequest req = createAuhRequest.createAuth();
		mockMvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.RuntimeExceptionErrors.[0]").value("Username or password not valid"));

	}

	@Test
	void userOnly_withoutAuth_shouldBeDenied() throws Exception {

		// mockMvc.perform(get("/api/v1/userOnly")).andExpect(status().is(403));// böyle
		// de ifade edebilirsin
		mockMvc.perform(get("/api/v1/userOnly")).andExpect(status().isForbidden());

	}

	@Test
	void getUsers_returnsWelcomeMessageWithUsername() throws Exception {
		String token = loginGetAccessToken();
		mockMvc.perform(get("/api/v1/getUsers").header("Authorization", "Bearer " + token))
				.andExpect(content().string("all users Welcome: zgrylmz"));
	}

	@Test
	void getAdminOnlyContent_WithRoleAdmin_MockTheRole() throws Exception {

		mockMvc.perform(get("/api/v1/adminOnly").with(user("zgrylmz").roles("ADMIN"))).andExpect(status().isOk())
				.andExpect(content().string("This endpoint just for admin"));

	}

}
