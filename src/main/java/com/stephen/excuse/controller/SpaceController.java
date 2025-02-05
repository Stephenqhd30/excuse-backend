package com.stephen.excuse.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.excuse.common.*;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.constants.UserConstant;
import com.stephen.excuse.model.dto.space.*;
import com.stephen.excuse.model.entity.Space;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.SpaceLevelEnum;
import com.stephen.excuse.model.vo.SpaceVO;
import com.stephen.excuse.service.SpaceService;
import com.stephen.excuse.service.UserService;
import com.stephen.excuse.utils.redisson.lock.LockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 空间接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {
	
	@Resource
	private SpaceService spaceService;
	
	@Resource
	private UserService userService;
	
	
	// region 增删改查
	
	/**
	 * 创建空间
	 *
	 * @param spaceAddRequest spaceAddRequest
	 * @param request         request
	 * @return {@link BaseResponse<Long>}
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceAddRequest, space);
		spaceService.fillSpaceBySpaceLevel(space);
		// 数据校验
		spaceService.validSpace(space, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		Long userId = loginUser.getId();
		space.setUserId(loginUser.getId());
		space.setSpaceLevel(Optional.ofNullable(spaceAddRequest.getSpaceLevel()).orElse(SpaceLevelEnum.COMMON.getValue()));
		// 权限校验
		if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
		}
		// 针对用户进行加锁
		String key = "space:" + userId;
		Long newSpaceId = LockUtils.lockEvent(key, () -> {
			boolean exists = spaceService.lambdaQuery().eq(Space::getUserId, userId).exists();
			ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
			// 写入数据库
			boolean result = spaceService.save(space);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
			// 返回新写入的数据 id
			return space.getId();
		}, () -> {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建空间失败");
		});
		return ResultUtils.success(newSpaceId);
	}
	
	/**
	 * 删除空间
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldSpace.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = spaceService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新空间（仅管理员可用）
	 *
	 * @param spaceUpdateRequest spaceUpdateRequest
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/update")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
		if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceUpdateRequest, space);
		// 数据校验
		spaceService.validSpace(space, false);
		spaceService.fillSpaceBySpaceLevel(space);
		// 判断是否存在
		long id = spaceUpdateRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取空间（封装类）
	 *
	 * @param id id
	 * @return {@link BaseResponse<SpaceVO>}
	 */
	@GetMapping("/get/vo")
	public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Space space = spaceService.getById(id);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVO(space, request));
	}
	
	/**
	 * 分页获取空间列表（仅管理员可用）
	 *
	 * @param spaceQueryRequest spaceQueryRequest
	 * @return {@link BaseResponse<Page<Space>>}
	 */
	@PostMapping("/list/page")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		return ResultUtils.success(spacePage);
	}
	
	/**
	 * 分页获取空间列表（封装类）
	 *
	 * @param spaceQueryRequest spaceQueryRequest
	 * @param request           request
	 * @return {@link BaseResponse<Page<SpaceVO>>}
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
	                                                     HttpServletRequest request) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的空间列表
	 *
	 * @param spaceQueryRequest spaceQueryRequest
	 * @param request           request
	 * @return {@link BaseResponse<Page<SpaceVO>>}
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<SpaceVO>> listMySpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
	                                                       HttpServletRequest request) {
		ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		spaceQueryRequest.setUserId(loginUser.getId());
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
	}
	
	/**
	 * 编辑空间（给用户使用）
	 *
	 * @param spaceEditRequest spaceEditRequest
	 * @param request          request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
		if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceEditRequest, space);
		// 数据校验
		spaceService.validSpace(space, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = spaceEditRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
	
	/**
	 * 获取空间级别列表
	 *
	 * @return BaseResponse<List < SpaceLevel>>
	 */
	@GetMapping("/list/level")
	public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
		// 获取所有枚举
		List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
				.map(spaceLevelEnum -> new SpaceLevel(
						spaceLevelEnum.getValue(),
						spaceLevelEnum.getText(),
						spaceLevelEnum.getMaxCount(),
						spaceLevelEnum.getMaxSize()))
				.collect(Collectors.toList());
		return ResultUtils.success(spaceLevelList);
	}
	
}