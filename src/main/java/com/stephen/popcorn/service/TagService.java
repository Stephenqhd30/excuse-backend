package com.stephen.popcorn.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.popcorn.model.dto.tag.TagDTO;
import com.stephen.popcorn.model.dto.tag.TagQueryRequest;
import com.stephen.popcorn.model.entity.Tag;
import com.stephen.popcorn.model.vo.TagVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 标签服务
 *
 * @author stephen qiu
 */
public interface TagService extends IService<Tag> {
	
	/**
	 * 校验数据
	 *
	 * @param tag tag
	 * @param add 对创建的数据进行校验
	 */
	void validTag(Tag tag, boolean add);
	
	/**
	 * 获取查询条件
	 *
	 * @param tagQueryRequest tagQueryRequest
	 * @return {@link QueryWrapper<Tag>}
	 */
	QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest);
	
	/**
	 * 获取标签封装
	 *
	 * @param tag     tag
	 * @param request request
	 * @return {@link TagVO}
	 */
	TagVO getTagVO(Tag tag, HttpServletRequest request);
	
	/**
	 * 分页获取标签封装
	 *
	 * @param tagPage tagPage
	 * @param request request
	 * @return {@link Page<TagVO>}
	 */
	Page<TagVO> getTagVOPage(Page<Tag> tagPage, HttpServletRequest request);
	
	/**
	 * 递归将 Tag 实体转换为 TagDTO，并填充子标签
	 *
	 * @param tag 父标签
	 * @return {@link TagDTO}
	 */
	TagDTO getTagDTO(Tag tag);
}