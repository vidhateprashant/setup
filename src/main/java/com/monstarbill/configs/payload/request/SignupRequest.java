package com.monstarbill.configs.payload.request;

import java.util.Set;

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
public class SignupRequest {
	private String username;
	private String email;
	private String password;
	private Set<String> role;
}
