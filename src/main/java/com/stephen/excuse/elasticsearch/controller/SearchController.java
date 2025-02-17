package com.stephen.excuse.elasticsearch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.excuse.common.BaseResponse;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ResultUtils;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.elasticsearch.manager.SearchFacade;
import com.stephen.excuse.elasticsearch.modal.dto.SearchRequest;
import com.stephen.excuse.elasticsearch.modal.vo.SearchVO;
import com.stephen.excuse.elasticsearch.service.UserEsService;
import com.stephen.excuse.model.dto.user.UserQueryRequest;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.vo.UserVO;
import com.stephen.excuse.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 从ES执行搜索接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/es")
@Slf4j
public class SearchController {
	
	@Resource
	private SearchFacade searchFacade;
	@Resource
	private UserEsService userEsService;
	
	@Resource
	private UserService userService;
	
	
	/**
	 * 使用门面模式进行重构
	 * 聚合搜索查询
	 *
	 * @param searchRequest searchRequest
	 * @return {@link BaseResponse <{@link SearchVO } <{@link Object}>>}
	 */
	@PostMapping("/all")
	public BaseResponse<SearchVO<Object>> doSearchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
		return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
	}
	
	
	/**
	 * 分页搜索用户（从 ES 查询，封装类）
	 *
	 * @param userQueryRequest userQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/search/user/page/vo")
	public BaseResponse<Page<UserVO>> searchUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                                     HttpServletRequest request) {
		long size = userQueryRequest.getPageSize();
		int current = userQueryRequest.getCurrent();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<User> userPage = userEsService.searchUserFromEs(userQueryRequest);
		Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
		List<UserVO> userVO = userService.getUserVO(userPage.getRecords(), request);
		userVOPage.setRecords(userVO);
		return ResultUtils.success(userVOPage);
	}
	
}
