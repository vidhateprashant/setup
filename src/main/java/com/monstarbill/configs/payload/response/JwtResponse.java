package com.monstarbill.configs.payload.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class JwtResponse {
	private Long id;
	private String token;
	private String type = "Basic";
	private String username;
	private String email;
	private List<String> roles;
	private Long employeeId;
	private Long supplierId;
	private boolean isFirstTimeLogin;

	public JwtResponse(String token, Long id, String username, String email, List<String> roles, Long employeeId, Long supplierId, boolean isFirstTimeLogin) {
		this.token = token;
		this.id = id;
		this.username = username;
		this.email = email;
		this.roles = roles;
		this.employeeId = employeeId;
		this.supplierId = supplierId;
		this.isFirstTimeLogin = isFirstTimeLogin;
	}
}
