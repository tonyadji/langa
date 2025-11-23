package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.valueobjects.UpdatePassword;

public interface PasswordService {

    String checkAndGetEncoded(UpdatePassword updatePassword);
}
