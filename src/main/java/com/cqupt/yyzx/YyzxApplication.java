package com.cqupt.yyzx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YyzxApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxApplication.class, args);
        System.out.println("==================================================");
        System.out.println("     东软颐养中心管理系统启动成功！");
        System.out.println("     访问地址：http://localhost:8080");
        System.out.println("==================================================");
    }

}
