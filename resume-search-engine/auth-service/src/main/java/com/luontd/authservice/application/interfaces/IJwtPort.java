package com.luontd.authservice.application.interfaces;

import java.util.Collection;
import java.util.UUID;

public interface IJwtPort {
    String generateToken(UUID userId, String username, Collection<String> roles);
}
