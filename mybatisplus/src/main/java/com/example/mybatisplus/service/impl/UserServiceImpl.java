package com.example.mybatisplus.service.impl;

import com.example.mybatisplus.Common.GlobalException;
import com.example.mybatisplus.Common.R;
import com.example.mybatisplus.Common.Response;
import com.example.mybatisplus.enenty.User;
import com.example.mybatisplus.mapper.UserMapper;
import com.example.mybatisplus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;


    @Override
    public R findAllUser() {
        List<User> allUser = userMapper.findAllUser();
        return R.ok(allUser);
    }

    /**
     * 新增加用户
     * @param user
     * @return
     */
    @Override
    public R addUser(User user) {
        Integer age = user.getAge();
        if (age == null) {
            throw new GlobalException("年龄不能为空");
        }
        int count = userMapper.insert(user);
        if (count > 0) {
            return R.ok("新增成功");
        }

        return R.fail("新增失败");
    }

    /**
     * 修改用户
     * @param user
     * @return
     */
    @Override
    public Response updateUser(User user) {
        Optional.ofNullable(user.getId()).orElseThrow(() -> new GlobalException("id不能为空"));
        int count = userMapper.updateById(user);
        if (count > 0) {
            return Response.ok("修改成功");
        }

        return Response.error("修改失败");
    }

}
