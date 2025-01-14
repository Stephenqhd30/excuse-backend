package com.stephen.excuse.model.dto.picture;

import lombok.Data;

/**
 * 批量上传图片请求
 *
 * @author stephen qiu
 */
@Data
public class PictureUploadByBatchRequest {
	
	/**
	 * 搜索词
	 */
	private String searchText;
	
	/**
	 * 抓取数量
	 */
	private Integer count = 10;
}
