package com.luontd.authservice.infrastructure.persistence;

import com.luontd.authservice.domain.repository.IUserRepository;
import com.luontd.authservice.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserRepository implements IUserRepository {
    @Override
    public User FindUserByUserName(String userName) {
        return null;
    }

}
