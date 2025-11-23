package com.langa.backend.domain.users.valueobjects;

import com.langa.backend.common.utils.KeyGenerator;

public record AccountKey(String value) {

    public static AccountKey newKey(String email) {
        return new AccountKey(KeyGenerator.generateAccountKey(email));
    }
}
