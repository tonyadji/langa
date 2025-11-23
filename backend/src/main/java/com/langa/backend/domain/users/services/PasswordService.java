package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.valueobjects.UpdatePassword;

public interface PasswordService {

    String encode(String password);
    String checkAndGetEncoded(UpdatePassword updatePassword);
    boolean matches(String rawPassword, String encodedPassword);
}
