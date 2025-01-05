package com.stephen.popcorn.model.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * TagDTO 封装类
 *
 * @author: stephen qiu
 * @create: 2024-09-22 20:22
 **/
@Data
public class TagDTO implements Serializable {
	
	private static final long serialVersionUID = 5969809940905979450L;
	/**
	 * 标签 id
	 */
	private Long id;
	/**
	 * 标签名称
	 */
	private String tagName;
	/**
	 * 标签子节点
	 */
	private List<TagChildren> children;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TagChildren implements Serializable{
		private static final long serialVersionUID = -795301363889010532L;
		/**
		 * 标签子节点 id
		 */
		private Long id;
		/**
		 * 标签名称
		 */
		private String tagName;
		
		/**
		 * 标签子节点子节点
		 */
		private List<TagChildren> children;
	}
}
