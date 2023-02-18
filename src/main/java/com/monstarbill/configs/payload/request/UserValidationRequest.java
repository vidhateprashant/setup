package com.monstarbill.configs.payload.request;

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
public class UserValidationRequest {
	private boolean isNewRecord;
	private String email;
	private String password;
	private List<Long> roles;
}
