package com.stephen.excuse.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.excuse.common.*;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.constants.UserConstant;
import com.stephen.excuse.model.dto.picture.*;
import com.stephen.excuse.model.entity.Picture;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.ReviewStatusEnum;
import com.stephen.excuse.model.enums.file.FileUploadBizEnum;
import com.stephen.excuse.model.vo.PictureVO;
import com.stephen.excuse.service.LogFilesService;
import com.stephen.excuse.service.PictureService;
import com.stephen.excuse.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 图片接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
	
	@Resource
	private PictureService pictureService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private LogFilesService logFilesService;
	
	// region 增删改查
	
	/**
	 * 创建图片
	 *
	 * @param pictureAddRequest pictureAddRequest
	 * @param request           request
	 * @return {@link BaseResponse<Long>}
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addPicture(@RequestBody PictureAddRequest pictureAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(pictureAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureAddRequest, picture);
		List<String> tags = pictureAddRequest.getTags();
		if (ObjectUtils.isNotEmpty(tags)) {
			picture.setTags(JSONUtil.toJsonStr(tags));
		}
		// 数据校验
		pictureService.validPicture(picture, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		picture.setUserId(loginUser.getId());
		// 更新审核信息
		Picture newPicture = pictureService.fillReviewParams(picture, loginUser);
		// 写入数据库
		boolean result = pictureService.save(newPicture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newPictureId = newPicture.getId();
		return ResultUtils.success(newPictureId);
	}
	
	/**
	 * 删除图片
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (oldPicture.getUserId().equals(user.getId()) || userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = pictureService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新图片（仅管理员可用）
	 *
	 * @param pictureUpdateRequest pictureUpdateRequest
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/update")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
		if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureUpdateRequest, picture);
		List<String> tags = pictureUpdateRequest.getTags();
		if (ObjectUtils.isNotEmpty(tags)) {
			picture.setTags(JSONUtil.toJsonStr(tags));
		}
		// 数据校验
		pictureService.validPicture(picture, false);
		
		// 判断是否存在
		long id = pictureUpdateRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 更新审核信息
		User loginUser = userService.getLoginUser(request);
		pictureService.fillReviewParams(picture, loginUser);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取图片（封装类）
	 *
	 * @param id id
	 * @return {@link BaseResponse<PictureVO>}
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVO(picture, request));
	}
	
	/**
	 * 分页获取图片列表（仅管理员可用）
	 *
	 * @param pictureQueryRequest pictureQueryRequest
	 * @return {@link BaseResponse<Page<Picture>>}
	 */
	@PostMapping("/list/page")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		return ResultUtils.success(picturePage);
	}
	
	/**
	 * 分页获取图片列表（封装类）
	 *
	 * @param pictureQueryRequest pictureQueryRequest
	 * @param request             request
	 * @return {@link BaseResponse<Page<PictureVO>>}
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
	                                                         HttpServletRequest request) {
		ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 限制只能查询过审的图片
		pictureQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的图片列表
	 *
	 * @param pictureQueryRequest pictureQueryRequest
	 * @param request             request
	 * @return {@link BaseResponse<Page<PictureVO>>}
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<PictureVO>> listMyPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
	                                                           HttpServletRequest request) {
		ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		pictureQueryRequest.setUserId(loginUser.getId());
		// 限制只能查询过审的图片
		pictureQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}
	
	/**
	 * 编辑图片（给用户使用）
	 *
	 * @param pictureEditRequest pictureEditRequest
	 * @param request            request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
		if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureEditRequest, picture);
		List<String> tags = pictureEditRequest.getTags();
		if (ObjectUtils.isNotEmpty(tags)) {
			picture.setTags(JSONUtil.toJsonStr(tags));
		}
		// 数据校验
		pictureService.validPicture(picture, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = pictureEditRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (oldPicture.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 更新审核信息
		pictureService.fillReviewParams(picture, loginUser);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
	
	
	/**
	 * 审核图片（给管理员使用）
	 *
	 * @param pictureReviewRequest pictureReviewRequest
	 * @param request              request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/review")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
	                                             HttpServletRequest request) {
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.doPictureReview(pictureReviewRequest, loginUser);
		return ResultUtils.success(true);
	}
	
	/**
	 * 图片上传(使用COS对象存储)
	 *
	 * @param multipartFile        multipartFile
	 * @param pictureUploadRequest pictureUploadRequest
	 * @param request              request
	 * @return BaseResponse<String>
	 */
	@PostMapping("/upload")
	public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
	                                             PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
		ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "上传请求参数不能为空");
		String biz = pictureUploadRequest.getBiz();
		FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
		ThrowUtils.throwIf(fileUploadBizEnum == null, ErrorCode.PARAMS_ERROR, "文件上传有误");
		
		// 校验图片类型
		logFilesService.validPicture(multipartFile, fileUploadBizEnum);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 直接上传文件
		try {
			PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
			return ResultUtils.success(pictureVO);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
		}
		
	}
	
}
