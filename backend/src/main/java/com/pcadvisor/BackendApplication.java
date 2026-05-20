package com.pcadvisor; // 包名必须与文件夹路径一致

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 必须添加@SpringBootApplication注解，标识为Spring Boot主类
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        // 启动Spring Boot应用，参数为当前类的class对象
        SpringApplication.run(BackendApplication.class, args);
    }
}