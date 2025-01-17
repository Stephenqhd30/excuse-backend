package com.stephen.excuse.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求
 *
 * @author stephen qiu
 */
@Data
public class PictureUploadRequest implements Serializable {
	
	/**
	 * 图片 id（用于修改）
	 */
	private Long id;
	
	/**
	 * 业务
	 */
	private String biz;
	
	/**
	 * 文件地址
	 */
	private String fileUrl;
	
	/**
	 * 空间 id（为空表示公共空间）
	 */
	private Long spaceId;
	
	private static final long serialVersionUID = 1L;
}
