package com.monstarbill.configs.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.common.UserComponentUtility;
import com.monstarbill.configs.models.User;
import com.monstarbill.configs.payload.request.UserValidationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins= "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false" )
public class UserController {
	
	@Autowired
	private UserComponentUtility userComponentUtility;
	
	/**
	 * Save the credentials only by Employee and Supplier
	 * @param userRequest
	 * @return
	 */
	@PostMapping("/save-user-credentials")
	public ResponseEntity<User> saveUserCredentials(@RequestBody UserValidationRequest userRequest) {
		log.info("saveUserCredentials is started :: " + userRequest.toString());
		User user = userComponentUtility.saveUserCredentials(userRequest.getEmail(), userRequest.getPassword(), userRequest.getRoles(), userRequest.isNewRecord());
		log.info("saveUserCredentials is Finished");
		return ResponseEntity.ok(user);
	}
	
	/**
	 * Delete the credentials only by Employee and Supplier
	 * @param username
	 * @return
	 */
	@GetMapping("/delete-user-credentials")
	public ResponseEntity<Boolean> deleteUserCredentials(@RequestParam String username) {
		log.info("deleteUserCredentials is started for username :: " + username);
		Boolean isDeleted = userComponentUtility.deleteUserCredentials(username);
		log.info("deleteUserCredentials is Finished for username :: " + username);
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}
	
}
