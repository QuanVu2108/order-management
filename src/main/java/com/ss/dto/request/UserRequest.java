package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class UserRequest {
	@NotBlank
	private String userName;

	@NotBlank
	private String fullName;

	@NotBlank
	private String password;

	@NotBlank
	private UUID permissionGroupId;

	@NotBlank
	private String position;

	@NotBlank
	private String email;

	@NotNull
	private List<UUID> storeIds;

	private Boolean isActive;
}
