package com.stephen.excuse.elasticsearch.datasources;

import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.elasticsearch.annotation.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源注册器
 *
 * @author: stephen qiu
 **/
@Component
@Slf4j
public class DataSourceRegistry {
	@Resource
	private UserDataSource userDataSource;
	
	
	private final Map<String, DataSource<?>> typeDataSourceMap = new ConcurrentHashMap<>();
	
	/**
	 * 初始化的时候执行一次，扫描并注册数据源
	 */
	@PostConstruct
	public void doInit() {
		log.info("开始注册数据源...");
		// 扫描所有数据源实例，并根据 @DataSourceType 注解注册它们
		registerDataSource(userDataSource);
		log.info("已注册数据源类型: {}", typeDataSourceMap.keySet());
	}
	
	/**
	 * 注册数据源到数据源映射
	 *
	 * @param dataSource 数据源实例
	 */
	private void registerDataSource(DataSource<?> dataSource) {
		// 获取数据源类上的 DataSourceType 注解
		DataSourceType dataSourceTypeAnnotation = dataSource.getClass().getAnnotation(DataSourceType.class);
		if (dataSourceTypeAnnotation != null) {
			// 获取注解中定义的类型
			String type = dataSourceTypeAnnotation.value().getValue();
			typeDataSourceMap.put(type, dataSource);
		} else {
			// 如果没有标注注解，输出警告
			log.warn("未为数据源 {} 标注 @DataSourceType 注解", dataSource.getClass().getSimpleName());
		}
	}
	
	
	/**
	 * 根据类型获取数据源
	 *
	 * @param type 数据源类型
	 * @return {@link DataSource}}
	 */
	public DataSource<?> getDataSourceByType(String type) {
		DataSource<?> dataSource = typeDataSourceMap.get(type);
		if (dataSource == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到指定类型的数据源: " + type);
		}
		return dataSource;
	}
}
