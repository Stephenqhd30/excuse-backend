package com.stephen.excuse.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.constants.CommonConstant;
import com.stephen.excuse.mapper.SpaceMapper;
import com.stephen.excuse.model.dto.space.SpaceQueryRequest;
import com.stephen.excuse.model.entity.Space;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.SpaceLevelEnum;
import com.stephen.excuse.model.vo.SpaceVO;
import com.stephen.excuse.model.vo.UserVO;
import com.stephen.excuse.service.SpaceService;
import com.stephen.excuse.service.UserService;
import com.stephen.excuse.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 空间服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {
	
	@Resource
	private UserService userService;
	
	/**
	 * 校验数据
	 *
	 * @param space space
	 * @param add   对创建的数据进行校验
	 */
	@Override
	public void validSpace(Space space, boolean add) {
		ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String spaceName = space.getSpaceName();
		Integer spaceLevel = space.getSpaceLevel();
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
			ThrowUtils.throwIf(ObjUtil.isEmpty(spaceLevel), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (ObjUtil.isNotEmpty(spaceLevel) && spaceLevelEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
		}
		if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
		}
	}
	
	/**
	 * 根据空间等级补充空间参数
	 *
	 * @param space space
	 */
	@Override
	public void fillSpaceBySpaceLevel(Space space) {
		// 根据空间级别，自动填充限额
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
		if (spaceLevelEnum != null) {
			long maxSize = spaceLevelEnum.getMaxSize();
			if (space.getMaxSize() == null) {
				space.setMaxSize(maxSize);
			}
			long maxCount = spaceLevelEnum.getMaxCount();
			if (space.getMaxCount() == null) {
				space.setMaxCount(maxCount);
			}
		}
	}
	
	
	/**
	 * 获取查询条件
	 *
	 * @param spaceQueryRequest spaceQueryRequest
	 * @return {@link QueryWrapper<Space>}
	 */
	@Override
	public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
		QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
		if (spaceQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = spaceQueryRequest.getId();
		Long notId = spaceQueryRequest.getNotId();
		String searchText = spaceQueryRequest.getSearchText();
		Long userId = spaceQueryRequest.getUserId();
		String spaceName = spaceQueryRequest.getSpaceName();
		Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
		String sortField = spaceQueryRequest.getSortField();
		String sortOrder = spaceQueryRequest.getSortOrder();
		
		// todo 补充需要的查询条件
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(searchText), "spaceName", searchText);
		queryWrapper.like(StringUtils.isNotBlank(spaceName), "spaceName", spaceName);
		// 精确查询
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取空间封装
	 *
	 * @param space   space
	 * @param request request
	 * @return {@link SpaceVO}
	 */
	@Override
	public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
		// 对象转封装类
		SpaceVO spaceVO = SpaceVO.objToVo(space);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = space.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		spaceVO.setUserVO(userVO);
		
		// endregion
		return spaceVO;
	}
	
	/**
	 * 分页获取空间封装
	 *
	 * @param spacePage spacePage
	 * @param request   request
	 * @return {@link Page<SpaceVO>}
	 */
	@Override
	public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
		List<Space> spaceList = spacePage.getRecords();
		Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
		if (CollUtil.isEmpty(spaceList)) {
			return spaceVOPage;
		}
		// 对象列表 => 封装对象列表
		List<SpaceVO> spaceVOList = spaceList.stream()
				.map(SpaceVO::objToVo)
				.collect(Collectors.toList());
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
		// 填充信息
		if (CollUtil.isNotEmpty(userIdSet)) {
			CompletableFuture<Map<Long, List<User>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId)));
			try {
				Map<Long, List<User>> userIdUserListMap = mapCompletableFuture.get();
				// 填充信息
				spaceVOList.forEach(spaceVO -> {
					Long userId = spaceVO.getUserId();
					User user = null;
					if (userIdUserListMap.containsKey(userId)) {
						user = userIdUserListMap.get(userId).get(0);
					}
					spaceVO.setUserVO(userService.getUserVO(user, request));
				});
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取信息失败" + e.getMessage());
			}
		}
		// endregion
		spaceVOPage.setRecords(spaceVOList);
		return spaceVOPage;
	}
	
}
