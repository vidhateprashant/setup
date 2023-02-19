package com.monstarbill.configs.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monstarbill.configs.feingclients.MasterServiceClient;
import com.monstarbill.configs.models.User;
import com.monstarbill.configs.payload.request.LoginRequest;
import com.monstarbill.configs.payload.request.PasswordRequest;
import com.monstarbill.configs.payload.request.SignupRequest;
import com.monstarbill.configs.payload.response.JwtResponse;
import com.monstarbill.configs.payload.response.MessageResponse;
import com.monstarbill.configs.repository.UserRepository;
import com.monstarbill.configs.repository.UserRolesRepository;
import com.monstarbill.configs.security.jwt.JwtUtils;
import com.monstarbill.configs.security.service.UserDetailsImpl;

@CrossOrigin(origins= "*", allowedHeaders = "*", maxAge = 4800, allowCredentials = "false" )
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	JwtUtils jwtUtils;
	
	@Autowired
	private MasterServiceClient masterServiceClient;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userRolesRepository.findByUsername(loginRequest.getUsername()).stream()
				.map(item -> item.getRole()).collect(Collectors.toList());
		
		if (userDetails.isFirstTimeLogin()) {
			Optional<User> userObject = userRepository.findByUsername(userDetails.getUsername());
			userObject.get().setPasswordUpdated(true);
			userRepository.save(userObject.get());
		}
		
		Long employeeId = this.masterServiceClient.getEmployeeIdByAccessMail(userDetails.getEmail());
		System.out.println("+========================== :: "+ employeeId);
		Long supplierId = null;
		if (employeeId == null) {
			supplierId = this.masterServiceClient.getSupplierIdByAccessMail(userDetails.getEmail());
		}

		System.out.println("Before sending response");

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(),
												 roles,
												 employeeId,
												 supplierId,
												 userDetails.isFirstTimeLogin()));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), 
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@GetMapping(value="/logout")  
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {  
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();  
        if (auth != null){      
           new SecurityContextLogoutHandler().logout(request, response, auth);  
        }  
         return "Logout success";  
     } 
	
	@PostMapping("/change-password")
	public boolean updatePassword(@RequestBody PasswordRequest passwordRequest) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(passwordRequest.getUsername(), passwordRequest.getOldPassword()));
		
		Optional<User> userObject = userRepository.findByUsername(passwordRequest.getUsername());
		userObject.get().setPassword(encoder.encode(passwordRequest.getNewPassword()));
		userRepository.save(userObject.get());
		
		return true; 
	}
}