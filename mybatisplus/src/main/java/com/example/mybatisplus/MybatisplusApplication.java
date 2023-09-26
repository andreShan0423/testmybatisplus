package com.example.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.**.mapper")
public class MybatisplusApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisplusApplication.class, args);

        System.out.println(" ▄︻┻┳═一…… ☆项目启动成功 ⍢⃝ ⍤⃝ ⍨⃝∵⃝♡⍢⃝ ⍤⃝ ⍨⃝∵⃝♡⍢⃝♡ ⍤⃝⍨⃝♡");

    }

}
