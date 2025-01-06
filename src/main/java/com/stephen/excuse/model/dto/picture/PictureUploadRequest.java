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
	
	private static final long serialVersionUID = 1L;
}
