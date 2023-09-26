package com.example.mybatisplus.enenty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private Integer id;
    private String name;
    private String sex;
    private Integer age;
    private String pwd;
    private String email;
}
