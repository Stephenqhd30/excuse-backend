package com.stephen.excuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.excuse.model.dto.space.SpaceQueryRequest;
import com.stephen.excuse.model.entity.Space;
import com.stephen.excuse.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 空间服务
 *
 * @author stephen qiu
 */
public interface SpaceService extends IService<Space> {

    /**
     * 校验数据
     *
     * @param space space
     * @param add 对创建的数据进行校验
     */
    void validSpace(Space space, boolean add);
	
	/**
	 * 根据空间等级补充空间参数
	 *
	 * @param space space
	 */
	void fillSpaceBySpaceLevel(Space space);
	
	/**
     * 获取查询条件
     *
     * @param spaceQueryRequest spaceQueryRequest
     * @return {@link QueryWrapper<Space>}
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间封装
     *
     * @param space space
     * @param request request
     * @return {@link SpaceVO}
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间封装
     *
     * @param spacePage spacePage
     * @param request request
     * @return {@link Page<SpaceVO>}
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);
}