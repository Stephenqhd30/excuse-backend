package com.stephen.excuse.model.dto.picture;

import com.stephen.excuse.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询图片请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * id
	 */
	private Long notId;
	
	/**
	 * 搜索词
	 */
	private String searchText;
	
	/**
	 * 图片 url
	 */
	private String url;
	
	/**
	 * 图片名称
	 */
	private String name;
	
	/**
	 * 简介
	 */
	private String introduction;
	
	/**
	 * 分类
	 */
	private String category;
	
	/**
	 * 标签（JSON 数组）
	 */
	private List<String> tags;
	
	/**
	 * 图片体积
	 */
	private Long picSize;
	
	/**
	 * 图片宽度
	 */
	private Integer picWidth;
	
	/**
	 * 图片高度
	 */
	private Integer picHeight;
	
	/**
	 * 图片宽高比例
	 */
	private Double picScale;
	
	/**
	 * 图片格式
	 */
	private String picFormat;
	
	/**
	 * 创建用户 id
	 */
	private Long userId;
	
	
	private static final long serialVersionUID = 1L;
}