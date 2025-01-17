package com.stephen.excuse.model.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.stephen.excuse.model.entity.Picture;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片视图
 *
 * @author stephen
 */
@Data
public class PictureVO implements Serializable {
	
	/**
	 * id
	 */
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
	 * 空间 id（为空表示公共空间）
	 */
	private Long spaceId;
	
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
	 * 标签列表
	 */
	private List<String> tagList;
	
	/**
	 * 创建用户信息
	 */
	private UserVO userVO;
	
	/**
	 * 封装类转对象
	 *
	 * @param pictureVO pictureVO
	 * @return {@link Picture}
	 */
	public static Picture voToObj(PictureVO pictureVO) {
		if (pictureVO == null) {
			return null;
		}
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureVO, picture);
		List<String> tagList = pictureVO.getTagList();
		if (CollUtil.isNotEmpty(tagList)) {
			picture.setTags(JSONUtil.toJsonStr(tagList));
		}
		return picture;
	}
	
	/**
	 * 对象转封装类
	 *
	 * @param picture picture
	 * @return {@link PictureVO}
	 */
	public static PictureVO objToVo(Picture picture) {
		if (picture == null) {
			return null;
		}
		PictureVO pictureVO = new PictureVO();
		BeanUtils.copyProperties(picture, pictureVO);
		if (StringUtils.isNotBlank(picture.getTags())) {
			pictureVO.setTagList(JSONUtil.toList(picture.getTags(), String.class));
		}
		return pictureVO;
	}
}
