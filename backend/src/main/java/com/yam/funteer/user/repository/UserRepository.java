package com.yam.funteer.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yam.funteer.common.code.UserType;
import com.yam.funteer.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    List<User>findAllByUserType(UserType userType);
	Optional<User> findByNameAndPhone(String name, String phone);
	Optional<User> findByPhone(String phone);
}
