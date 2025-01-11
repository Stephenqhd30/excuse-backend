package com.stephen.excuse.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.constants.CommonConstant;
import com.stephen.excuse.mapper.PictureMapper;
import com.stephen.excuse.model.dto.picture.PictureQueryRequest;
import com.stephen.excuse.model.dto.picture.PictureReviewRequest;
import com.stephen.excuse.model.dto.picture.PictureUploadRequest;
import com.stephen.excuse.model.vo.PictureUploadResult;
import com.stephen.excuse.model.entity.Picture;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.ReviewStatusEnum;
import com.stephen.excuse.model.enums.file.FileUploadBizEnum;
import com.stephen.excuse.model.vo.PictureVO;
import com.stephen.excuse.model.vo.UserVO;
import com.stephen.excuse.service.PictureService;
import com.stephen.excuse.service.UserService;
import com.stephen.excuse.utils.oss.CosUtils;
import com.stephen.excuse.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 图片服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
	
	@Resource
	private UserService userService;
	
	/**
	 * 校验数据
	 *
	 * @param picture picture
	 * @param add     对创建的数据进行校验
	 */
	@Override
	public void validPicture(Picture picture, boolean add) {
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String url = picture.getUrl();
		String name = picture.getName();
		String introduction = picture.getIntroduction();
		String category = picture.getCategory();
		
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(url), ErrorCode.PARAMS_ERROR, "url 不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "名称不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(name)) {
			ThrowUtils.throwIf(name.length() > 80, ErrorCode.PARAMS_ERROR, "名称过长");
		}
		if (StringUtils.isNotBlank(introduction)) {
			ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "简介过程");
		}
		if (StringUtils.isNotBlank(category)) {
			ThrowUtils.throwIf(category.length() > 80, ErrorCode.PARAMS_ERROR, "分类错误");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param pictureQueryRequest pictureQueryRequest
	 * @return {@link QueryWrapper<Picture>}
	 */
	@Override
	public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
		if (pictureQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = pictureQueryRequest.getId();
		Long notId = pictureQueryRequest.getNotId();
		String searchText = pictureQueryRequest.getSearchText();
		String url = pictureQueryRequest.getUrl();
		String name = pictureQueryRequest.getName();
		String introduction = pictureQueryRequest.getIntroduction();
		String category = pictureQueryRequest.getCategory();
		List<String> tags = pictureQueryRequest.getTags();
		Long picSize = pictureQueryRequest.getPicSize();
		Integer picWidth = pictureQueryRequest.getPicWidth();
		Integer picHeight = pictureQueryRequest.getPicHeight();
		Double picScale = pictureQueryRequest.getPicScale();
		String picFormat = pictureQueryRequest.getPicFormat();
		Integer reviewStatus = pictureQueryRequest.getReviewStatus();
		String reviewMessage = pictureQueryRequest.getReviewMessage();
		Long reviewerId = pictureQueryRequest.getReviewerId();
		Long userId = pictureQueryRequest.getUserId();
		String sortField = pictureQueryRequest.getSortField();
		String sortOrder = pictureQueryRequest.getSortOrder();
		
		// todo 补充需要的查询条件
		// 从多字段中搜索
		if (StringUtils.isNotBlank(searchText)) {
			// 需要拼接查询条件
			queryWrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
		queryWrapper.like(StringUtils.isNotBlank(introduction), "introduction", introduction);
		queryWrapper.like(StringUtils.isNotBlank(category), "category", category);
		queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
		queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
		// JSON 数组查询
		if (CollUtil.isNotEmpty(tags)) {
			for (String tag : tags) {
				queryWrapper.like("tags", "\"" + tag + "\"");
			}
		}
		// 精确查询
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "reviewStatus", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(picSize), "picSize", picSize);
		queryWrapper.eq(ObjectUtils.isNotEmpty(picWidth), "picWidth", picWidth);
		queryWrapper.eq(ObjectUtils.isNotEmpty(picHeight), "picHeight", picHeight);
		queryWrapper.eq(ObjectUtils.isNotEmpty(picScale), "picScale", picScale);
		queryWrapper.eq(ObjectUtils.isNotEmpty(picFormat), "picFormat", picFormat);
		queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取图片封装
	 *
	 * @param picture picture
	 * @param request request
	 * @return {@link PictureVO}
	 */
	@Override
	public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
		// 对象转封装类
		PictureVO pictureVO = PictureVO.objToVo(picture);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = picture.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		pictureVO.setUserVO(userVO);
		
		// endregion
		return pictureVO;
	}
	
	/**
	 * 分页获取图片封装
	 *
	 * @param picturePage picturePage
	 * @param request     request
	 * @return {@link Page<PictureVO>}
	 */
	@Override
	public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
		List<Picture> pictureList = picturePage.getRecords();
		Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
		if (CollUtil.isEmpty(pictureList)) {
			return pictureVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PictureVO> pictureVOList = pictureList.stream()
				.map(PictureVO::objToVo)
				.collect(Collectors.toList());
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
		// 填充信息
		if (CollUtil.isNotEmpty(userIdSet)) {
			CompletableFuture<Map<Long, List<User>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId)));
			try {
				Map<Long, List<User>> userIdUserListMap = mapCompletableFuture.get();
				// 填充信息
				pictureVOList.forEach(pictureVO -> {
					Long userId = pictureVO.getUserId();
					User user = null;
					if (userIdUserListMap.containsKey(userId)) {
						user = userIdUserListMap.get(userId).get(0);
					}
					pictureVO.setUserVO(userService.getUserVO(user, request));
				});
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取信息失败" + e.getMessage());
			}
		}
		// endregion
		pictureVOPage.setRecords(pictureVOList);
		return pictureVOPage;
	}
	
	/**
	 * 图片审核
	 *
	 * @param pictureReviewRequest pictureReviewRequest
	 * @param loginUser            loginUser
	 */
	@Override
	public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
		Long id = pictureReviewRequest.getId();
		Integer reviewStatus = pictureReviewRequest.getReviewStatus();
		ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
		if (id == null || reviewStatusEnum == null || ReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 判断是否存在
		Picture oldPicture = this.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 已是该状态
		if (oldPicture.getReviewStatus().equals(reviewStatus)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
		}
		// 更新审核状态
		Picture updatePicture = new Picture();
		BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
		updatePicture.setReviewerId(loginUser.getId());
		updatePicture.setReviewTime(new Date());
		boolean result = this.updateById(updatePicture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}
	
	/**
	 * 批量审核图片审核
	 *
	 * @param pictureReviewRequest pictureReviewRequest
	 * @param loginUser            loginUser
	 */
	@Override
	public void doPictureReviewByBatch(PictureReviewRequest pictureReviewRequest, User loginUser) {
		ArrayList<Picture> pictureList = new ArrayList<>();
		List<Long> idList = pictureReviewRequest.getIdList();
		Integer reviewStatus = pictureReviewRequest.getReviewStatus();
		ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
		for (Long pictureId : idList) {
			if (pictureId == null || reviewStatusEnum == null || ReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR);
			}
			// 判断是否存在
			Picture oldPicture = this.getById(pictureId);
			ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
			// 已是该状态
			if (oldPicture.getReviewStatus().equals(reviewStatus)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
			}
			// todo 在此处将实体类和 DTO 进行转换
			Picture picture = new Picture();
			BeanUtils.copyProperties(pictureReviewRequest, picture);
			picture.setId(pictureId);
			// todo 填充默认值
			picture.setReviewerId(loginUser.getId());
			picture.setReviewTime(new Date());
			pictureList.add(picture);
		}
		// 更新审核状态
		boolean result = this.updateBatchById(pictureList);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}
	
	/**
	 * 填充审核参数
	 *
	 * @param picture   picture
	 * @param loginUser loginUser
	 * @return {@link Picture}
	 */
	@Override
	public Picture fillReviewParams(Picture picture, User loginUser) {
		Picture newPicture = new Picture();
		BeanUtils.copyProperties(picture, newPicture);
		if (userService.isAdmin(loginUser)) {
			// 管理员自动过审
			newPicture.setReviewStatus(ReviewStatusEnum.PASS.getValue());
			newPicture.setReviewerId(loginUser.getId());
			newPicture.setReviewMessage("管理员自动过审");
			newPicture.setReviewTime(new Date());
		} else {
			// 非管理员，创建或编辑都要改为待审核
			newPicture.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
		}
		return newPicture;
	}
	
	/**
	 * 上传图片
	 *
	 * @param multipartFile        multipartFile
	 * @param pictureUploadRequest pictureUploadRequest
	 * @param loginUser            loginUser
	 * @return {@link PictureVO}
	 */
	@Override
	public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) throws IOException {
		// 用于判断是新增还是更新图片
		Long pictureId = pictureUploadRequest.getId();
		// 如果是更新图片，需要校验图片是否存在
		if (pictureId != null) {
			boolean exists = this.lambdaQuery()
					.eq(Picture::getId, pictureId)
					.exists();
			ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
		}
		// 上传图片，得到信息
		// 按照用户 id 划分目录
		String uploadPathPrefix = String.format("/%s/%s/%s", "excuse", FileUploadBizEnum.PICTURE.getValue(), loginUser.getId());
		PictureUploadResult pictureUploadResult = CosUtils.uploadPicture(multipartFile, uploadPathPrefix);
		// 构造要入库的图片信息
		Picture picture = new Picture();
		picture.setUrl(pictureUploadResult.getUrl());
		picture.setName(pictureUploadResult.getPicName());
		picture.setPicSize(pictureUploadResult.getPicSize());
		picture.setPicWidth(pictureUploadResult.getPicWidth());
		picture.setPicHeight(pictureUploadResult.getPicHeight());
		picture.setPicScale(pictureUploadResult.getPicScale());
		picture.setPicFormat(pictureUploadResult.getPicFormat());
		picture.setUserId(loginUser.getId());
		if (pictureId != null) {
			picture.setId(pictureId);
			boolean b = this.updateById(picture);
			ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
		}
		return PictureVO.objToVo(picture);
	}
}
