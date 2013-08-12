package com.github.styx.controllers;

import com.github.styx.domain.User;
import com.github.styx.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<User> getAllUsers(@RequestHeader("Authorization") final String token) {
        return userRepository.getAllUsers(token);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void registerUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        userRepository.registerUser(username, password);
    }

}
