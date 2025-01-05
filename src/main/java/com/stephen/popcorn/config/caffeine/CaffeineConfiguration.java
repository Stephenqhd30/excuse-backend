package com.stephen.popcorn.config.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stephen.popcorn.config.caffeine.condition.CaffeineCondition;
import com.stephen.popcorn.config.caffeine.properties.CaffeineProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine配置
 *
 * @author stephen qiu
 */
@Configuration
@Slf4j
@Conditional(CaffeineCondition.class)
public class CaffeineConfiguration {
	
	@Resource
	private CaffeineProperties caffeineProperties;
	
	@Bean("localCache")
	public Cache<String, Object> localCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(caffeineProperties.getExpired(), TimeUnit.SECONDS)
				.expireAfterAccess(caffeineProperties.getExpired(), TimeUnit.SECONDS)
				// 初始的缓存空间大小
				.initialCapacity(caffeineProperties.getInitCapacity())
				// 缓存的最大条数
				.maximumSize(caffeineProperties.getMaxCapacity())
				.build();
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
}