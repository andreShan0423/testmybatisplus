package com.example.mybatisplus.controller;

import com.example.mybatisplus.Common.R;
import com.example.mybatisplus.Common.Response;
import com.example.mybatisplus.enenty.User;
import com.example.mybatisplus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;


    @GetMapping("/findAllUser")
    public R getAllUser() {
        return userService.findAllUser();

    }

    @PostMapping("/addUser")
    public R addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PostMapping("/updateUser")
    public Response updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
}
