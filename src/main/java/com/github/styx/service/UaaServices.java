package com.github.styx.service;

import com.github.styx.console.domain.User;

import java.util.List;

public interface UaaServices {

    String getApplicationAccessToken();

    String getAccessToken(String username, String password);

    User getUser(String token);

    List<User> appendUserNames(String token, List<User> users);

}
