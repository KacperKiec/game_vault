package io.kk.userservice.repository.impl;

import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository{

}
