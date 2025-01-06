package com.stephen.excuse.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片
 *
 * @TableName picture
 */
@TableName(value = "picture")
public class Picture implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	
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
	private String tags;
	
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
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 编辑时间
	 */
	private Date editTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	/**
	 * 是否删除
	 */
	private Integer isDelete;
	
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
	
	/**
	 * id
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * 图片 url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * 图片 url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * 图片名称
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 图片名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 简介
	 */
	public String getIntroduction() {
		return introduction;
	}
	
	/**
	 * 简介
	 */
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	
	/**
	 * 分类
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * 分类
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	
	/**
	 * 标签（JSON 数组）
	 */
	public String getTags() {
		return tags;
	}
	
	/**
	 * 标签（JSON 数组）
	 */
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	/**
	 * 图片体积
	 */
	public Long getPicSize() {
		return picSize;
	}
	
	/**
	 * 图片体积
	 */
	public void setPicSize(Long picSize) {
		this.picSize = picSize;
	}
	
	/**
	 * 图片宽度
	 */
	public Integer getPicWidth() {
		return picWidth;
	}
	
	/**
	 * 图片宽度
	 */
	public void setPicWidth(Integer picWidth) {
		this.picWidth = picWidth;
	}
	
	/**
	 * 图片高度
	 */
	public Integer getPicHeight() {
		return picHeight;
	}
	
	/**
	 * 图片高度
	 */
	public void setPicHeight(Integer picHeight) {
		this.picHeight = picHeight;
	}
	
	/**
	 * 图片宽高比例
	 */
	public Double getPicScale() {
		return picScale;
	}
	
	/**
	 * 图片宽高比例
	 */
	public void setPicScale(Double picScale) {
		this.picScale = picScale;
	}
	
	/**
	 * 图片格式
	 */
	public String getPicFormat() {
		return picFormat;
	}
	
	/**
	 * 图片格式
	 */
	public void setPicFormat(String picFormat) {
		this.picFormat = picFormat;
	}
	
	/**
	 * 创建用户 id
	 */
	public Long getUserId() {
		return userId;
	}
	
	/**
	 * 创建用户 id
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	/**
	 * 创建时间
	 */
	public Date getCreateTime() {
		return createTime;
	}
	
	/**
	 * 创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	/**
	 * 编辑时间
	 */
	public Date getEditTime() {
		return editTime;
	}
	
	/**
	 * 编辑时间
	 */
	public void setEditTime(Date editTime) {
		this.editTime = editTime;
	}
	
	/**
	 * 更新时间
	 */
	public Date getUpdateTime() {
		return updateTime;
	}
	
	/**
	 * 更新时间
	 */
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	/**
	 * 是否删除
	 */
	public Integer getIsDelete() {
		return isDelete;
	}
	
	/**
	 * 是否删除
	 */
	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}
}