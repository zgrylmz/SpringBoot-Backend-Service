package com.zgrylmz.springSecurity.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zgrylmz.springSecurity.dto.UserDto;
import com.zgrylmz.springSecurity.dto.UserDtoIU;
import com.zgrylmz.springSecurity.dto.AgeWithUsernameDto.CallAgeWithUsernameDto;
import com.zgrylmz.springSecurity.dto.Auth.AuthRequest;
import com.zgrylmz.springSecurity.dto.Auth.AuthResponse;
import com.zgrylmz.springSecurity.entity.Users;
import com.zgrylmz.springSecurity.exception.specialExceptionHandler.BaseException;
import com.zgrylmz.springSecurity.exception.specialExceptionHandler.ErrorMessage;
import com.zgrylmz.springSecurity.exception.specialExceptionHandler.MessageType;
import com.zgrylmz.springSecurity.repository.IUsersRepository;
import com.zgrylmz.springSecurity.security.JwtTokenProvider;
import com.zgrylmz.springSecurity.service.IUsersService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // sadece final kisimlar icin constructor üretir yani gerekli null olmayan
							// kisimlar icin ve inject
public class UserServiceImpl implements IUsersService {

	private final IUsersRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtTokenProvider jwtProvider;

	@Override
	@CacheEvict(value = "Users", allEntries = true)
	public UserDto newUserRegister(UserDtoIU userRegister) {
		Optional<Users> findUser = userRepository.findByUsername(userRegister.getUsername());

		if (findUser.isPresent()) {
			throw new BaseException(new ErrorMessage(MessageType.Username_Exist, " "));
		}

		Users user = new Users();
		// BeanUtils.copyProperties(userRegister, user);
		// user.setPassword(passwordEncoder.encode(UserRegister.getPassword()))
		// register gibi passwordlü islemlerde beanutils kullanma manuel setle daha
		// güvenliymis cünkü önce raw passwordü user a yaziyosun sonra tekrar passwordü
		// setliyosun passwordEncoder ile
		user.setAge(userRegister.getAge());
		user.setJob(userRegister.getJob());
		user.setUsername(userRegister.getUsername());

		String encodedPassword = passwordEncoder.encode(userRegister.getPassword());
		user.setPassword(encodedPassword);

		Users savedUser = userRepository.save(user);

		UserDto dtoUser = new UserDto();
		BeanUtils.copyProperties(savedUser, dtoUser);

		return dtoUser;
	}

	@Override
	public AuthResponse userLogin(AuthRequest loginUser) {
		Optional<Users> checkUserName = userRepository.findByUsername(loginUser.getUsername());

		if (checkUserName.isEmpty()) {
			throw new RuntimeException("User doesnt exist");
		}

		Users userDb = checkUserName.get();
		boolean control = passwordEncoder.matches(loginUser.getPassword(), userDb.getPassword());

		if (!control) {
			throw new RuntimeException("Username or password not valid");
		}

		String accessToken = jwtProvider.generateAccessToken(userDb.getUsername(), userDb.getRole());
		String refreshToken = jwtProvider.generateRefreshToken(userDb.getUsername());
		return new AuthResponse(accessToken, refreshToken);
	}

	@Override
	public CallAgeWithUsernameDto callJust2Infos(Long id) {
		Optional<Users> callData = userRepository.findById(id);

		if (callData.isEmpty()) {
			throw new RuntimeException("data not found");
		}

		Users setData = callData.get();

		CallAgeWithUsernameDto responseDto = new CallAgeWithUsernameDto();

		responseDto.setAge(setData.getAge());
		responseDto.setUsername(setData.getUsername());

		return responseDto;
	}

	@Override
	public String regenerateNewAccessToken(String refreshToken) {
		String newAccessToken = jwtProvider.regenerateAccessToken(refreshToken);
		return newAccessToken;
	}

	@Override
	@CacheEvict(value = "Users", allEntries = true)
	@CachePut(value = "User", key = "#result.id")
	public UserDto updateYourInfos(UserDtoIU infosUpdate, String username) {

		Optional<Users> userFromDb = userRepository.findByUsername(username);

		if (!userFromDb.isPresent()) {
			throw new RuntimeException("User doesnt exist");
		}

		Users newUser = userFromDb.get();

		if (infosUpdate.getAge() != null) {
			newUser.setAge(infosUpdate.getAge());
		}
		if (infosUpdate.getJob() != null) {
			newUser.setJob(infosUpdate.getJob());

		}

		if (infosUpdate.getUsername() != null && !infosUpdate.getUsername().isEmpty()) {

			if (infosUpdate.getUsername().equals(newUser.getUsername())) {
				throw new RuntimeException("New username must be different from the current username");
			}

			Optional<Users> existingUser = userRepository.findByUsername(infosUpdate.getUsername());

			if (existingUser.isPresent()) {
				throw new RuntimeException("This username is already taken");
			}

			newUser.setUsername(infosUpdate.getUsername());
		}

		if (infosUpdate.getPassword() != null && !infosUpdate.getPassword().isEmpty()) {
			String newPassword = passwordEncoder.encode(infosUpdate.getPassword());
			newUser.setPassword(newPassword);
		}

		Users savedInfos = userRepository.save(newUser);

		UserDto userDto1 = new UserDto();
		BeanUtils.copyProperties(savedInfos, userDto1);

		return userDto1;
	}

	@Override
	@Caching(evict = { @CacheEvict(value = "Users", allEntries = true), @CacheEvict(value = "User", key = "#id")

	})
	public void deleteUser(Long id) {
		Optional<Users> findUser = userRepository.findById(id); // findById hep optional formatinda döner
		findUser.orElseThrow(() -> new RuntimeException("User not found with id" + id)); // orElseThrow optional
																							// sinifinin bir metodudur
																							// uzun uzun if
																							// findUser.ispresent
																							// yazmazsin varsa kullanici
																							// get döner yoksa da hata
																							// firlatir

		Users setUser = new Users();
		setUser = findUser.get();

		userRepository.delete(setUser);

	}

//	@Cacheable(value = "Users", key = "#id")
//	@Override
//	public UserDto getUserPerId(Long id) {
//		Optional<Users> findUser = userRepository.findById(id);
//
//		findUser.orElseThrow(() -> new RuntimeException("User not found"));
//
//		UserDto userDto = new UserDto();
//
//		BeanUtils.copyProperties(findUser, userDto);
//
//		return userDto;
//
//	}

	@Cacheable(value = "Users")
	public List<UserDto> getAllUsers() {
		System.out.println("Data is called from DB");

		List<Users> allUsers = userRepository.findAll();

		List<UserDto> dtoEntities = new ArrayList<UserDto>();

		if (!allUsers.isEmpty()) {
			for (Users user : allUsers) {
				UserDto userDto = new UserDto();
				BeanUtils.copyProperties(user, userDto);
				dtoEntities.add(userDto);
			}
		}

		return dtoEntities;
	}

	@Cacheable(value = "User", key = "#id")
	public UserDto getUserById(Long id) {

		Optional<Users> getUser = userRepository.findById(id);
		getUser.orElseThrow(() -> new RuntimeException("User not found"));

		Users setUser = getUser.get();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(setUser, userDto);

		return userDto;
	}

}
