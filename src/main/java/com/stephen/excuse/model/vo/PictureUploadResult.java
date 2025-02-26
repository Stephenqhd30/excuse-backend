package com.stephen.excuse.model.vo;

import lombok.Data;

/**
 * 图片上传结果
 *
 * @author stephen qiu
 */
@Data
public class PictureUploadResult {
	
	/**
	 * 图片地址
	 */
	private String url;
	
	/**
	 * 图片名称
	 */
	private String picName;
	
	/**
	 * 文件体积
	 */
	private Long picSize;
	
	/**
	 * 图片宽度
	 */
	private int picWidth;
	
	/**
	 * 图片高度
	 */
	private int picHeight;
	
	/**
	 * 图片宽高比
	 */
	private Double picScale;
	
	/**
	 * 图片格式
	 */
	private String picFormat;
	
}
