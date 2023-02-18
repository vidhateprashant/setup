package com.monstarbill.configs.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.monstarbill.configs.models.User;
import com.monstarbill.configs.models.UserRoles;
import com.monstarbill.configs.repository.UserRepository;
import com.monstarbill.configs.repository.UserRolesRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Prashant
 */
@Slf4j
@Component
public class UserComponentUtility {

	@Autowired
	private PasswordEncoder encoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserRolesRepository userRolesRepository;
	
	/**
	 * saves the user credentials for the login purpose
	 * @param email
	 * @param password
	 * @param roles
	 * @param isNewRecord 
	 * @return
	 */
	public User saveUserCredentials(String email, String password, List<Long> roles, boolean isNewRecord) {
		Optional<User> user = Optional.of(new User(email, email, encoder.encode(password)));
		if (isNewRecord) {
			if (userRepository.existsByUsername(email)) {
				log.error("Error: Username is already taken!");
				throw new CustomMessageException("Error: Username is already taken!");
			}
	
			if (userRepository.existsByEmail(email)) {
				log.error("Error: Email is already in use!");
				throw new CustomMessageException("Error: Email is already in use!");
			}
		} else {
			log.info("Existing record for user credentials");
			user = userRepository.findByUsername(email);
			if (user.isPresent()) {
				user.get().setPassword(encoder.encode(password));
			} else {
				user = Optional.of(new User(email, email, encoder.encode(password)));
				if (userRepository.existsByUsername(email)) {
					log.error("Error: Username is already taken!");
					throw new CustomMessageException("Error: Username is already taken!");
				}
		
				if (userRepository.existsByEmail(email)) {
					log.error("Error: Email is already in use!");
					throw new CustomMessageException("Error: Email is already in use!");
				}
			}
		}

		user = Optional.of(userRepository.save(user.get()));
		log.info("User is added successfully.");
		
		userRolesRepository.deleteAllByUsername(email);
		log.info("Existing user roles are deleted from user-role table.");
		
		List<UserRoles> userRoles = new ArrayList<UserRoles>();
		for (Long roleId : roles) {
			if (roleId != null) {
				UserRoles userRole = new UserRoles();
				userRole.setUsername(email);
				userRole.setRole(String.valueOf(roleId));
				userRole.setCreatedBy(CommonUtils.getLoggedInUsername());
				userRole.setLastModifiedBy(CommonUtils.getLoggedInUsername());
				userRole = userRolesRepository.save(userRole);
				log.info(" User role :: " + userRole.toString());
				userRoles.add(userRole);
			}
		}
		
		user.get().setUserRoles(userRoles);

		return user.get();
	}
	
	/**
	 * delete the user credentials from the table, user-roles mapping and user
	 * @param username
	 * @return
	 */
	public boolean deleteUserCredentials(String username) {
		userRolesRepository.deleteAllByUsername(username);
		userRepository.deleteByUsername(username);
		return true;
	}
}
