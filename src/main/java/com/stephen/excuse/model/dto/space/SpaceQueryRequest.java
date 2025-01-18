package com.stephen.excuse.model.dto.space;

import com.stephen.excuse.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询空间请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * notId
	 */
	private Long notId;
	
	/**
	 * 搜索词
	 */
	private String searchText;
	
	/**
	 * 用户 id
	 */
	private Long userId;
	
	/**
	 * 空间名称
	 */
	private String spaceName;
	
	/**
	 * 空间级别：0-普通版 1-专业版 2-旗舰版
	 */
	private Integer spaceLevel;
	
	private static final long serialVersionUID = 1L;
}
