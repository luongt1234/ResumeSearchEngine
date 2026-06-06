package com.luontd.authservice.domain.repository;

import com.luontd.authservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {
    public  User FindUserByUserName(String userName);
}
