package com.stephen.excuse.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑空间请求
 *
 * @author stephen qiu
 */
@Data
public class SpaceEditRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 空间名称
	 */
	private String spaceName;
	
	private static final long serialVersionUID = 1L;
}