package com.monstarbill.configs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.UserRoles;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRoles, Long> {
	
	public List<UserRoles> findByUsername(String username);

	public void deleteAllByUsername(String username);
}