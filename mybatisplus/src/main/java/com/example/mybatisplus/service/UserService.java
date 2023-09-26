package com.example.mybatisplus.service;

import com.example.mybatisplus.Common.R;
import com.example.mybatisplus.Common.Response;
import com.example.mybatisplus.enenty.User;


public interface UserService {
    R findAllUser();

    R addUser(User user);

    Response updateUser(User user);
}
