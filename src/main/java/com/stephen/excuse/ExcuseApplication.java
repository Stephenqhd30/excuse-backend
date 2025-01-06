package com.stephen.excuse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author stephen qiu
 */
@SpringBootApplication
@MapperScan("com.stephen.excuse.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class ExcuseApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ExcuseApplication.class, args);
	}
	
}
