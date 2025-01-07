package com.stephen.excuse.model.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签表
 *
 * @author stephen qiu
 * @TableName tag
 */
@TableName(value = "tag")
@Data
public class Tag implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@ExcelIgnore
	private Long id;
	
	/**
	 * 标签名称
	 */
	@ExcelProperty("标签名称")
	private String tagName;
	
	/**
	 * 用户id
	 */
	@ExcelIgnore
	private Long userId;
	
	/**
	 * 父标签id
	 */
	@ExcelIgnore
	private Long parentId;
	
	/**
	 * 0-不是父标签，1-是父标签
	 */
	@ExcelIgnore
	private Integer isParent;
	
	/**
	 * 创建时间
	 */
	@ExcelIgnore
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@ExcelIgnore
	private Date updateTime;
	
	/**
	 * 是否删除
	 */
	@ExcelIgnore
	@TableLogic
	private Integer isDelete;
	
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}