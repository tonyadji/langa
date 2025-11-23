package com.langa.backend.domain.users.usecases.getinfo;

public record GetUserInfoQuery(String username) {

    public void validate() {
    }
}
