package com.stephen.excuse.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别
 *
 * @author stephen qiu
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
	
	/**
	 * 空间级别：0-普通版 1-专业版 2-旗舰版
	 */
	private int value;
	
	/**
	 * 空间级别名称
	 */
	private String text;
	
	/**
	 * 最大数量
	 */
	private long maxCount;
	
	/**
	 * 最大总大小
	 */
	private long maxSize;
}
