package com.stephen.excuse.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.excuse.aop.listener.UserExcelListener;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.config.security.utils.DeviceUtils;
import com.stephen.excuse.constants.CommonConstant;
import com.stephen.excuse.constants.SaltConstant;
import com.stephen.excuse.constants.UserConstant;
import com.stephen.excuse.mapper.UserMapper;
import com.stephen.excuse.model.dto.user.UserQueryRequest;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.user.UserGenderEnum;
import com.stephen.excuse.model.enums.user.UserRoleEnum;
import com.stephen.excuse.model.vo.LoginUserVO;
import com.stephen.excuse.model.vo.UserVO;
import com.stephen.excuse.service.UserService;
import com.stephen.excuse.utils.regex.RegexUtils;
import com.stephen.excuse.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 用户服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
	
	/**
	 * 校验数据
	 *
	 * @param user user
	 * @param add  对创建的数据进行校验
	 */
	@Override
	public void validUser(User user, boolean add) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String userAccount = user.getUserAccount();
		String userName = user.getUserName();
		Integer userGender = user.getUserGender();
		String userProfile = user.getUserProfile();
		String userEmail = user.getUserEmail();
		String userPhone = user.getUserPhone();
		
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "账号不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(userName), ErrorCode.PARAMS_ERROR, "用户名不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(userAccount)) {
			ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 20, ErrorCode.PARAMS_ERROR, "账号不能小于4位，不能多于20位");
		}
		if (StringUtils.isNotBlank(userName)) {
			ThrowUtils.throwIf(userName.length() < 4 || userName.length() > 20, ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (StringUtils.isNotBlank(userProfile)) {
			ThrowUtils.throwIf(userProfile.length() > 50, ErrorCode.PARAMS_ERROR, "用户简介不能多余50字");
		}
		if (StringUtils.isNotBlank(userEmail)) {
			ThrowUtils.throwIf(!RegexUtils.checkEmail(userEmail), ErrorCode.PARAMS_ERROR, "用户邮箱有误");
		}
		if (StringUtils.isNotBlank(userPhone)) {
			ThrowUtils.throwIf(!RegexUtils.checkMobile(userPhone), ErrorCode.PARAMS_ERROR, "用户手机号码有误");
		}
		if (ObjectUtils.isNotEmpty(userGender)) {
			ThrowUtils.throwIf(UserGenderEnum.getEnumByValue(userGender) == null, ErrorCode.PARAMS_ERROR, "性别填写有误");
		}
	}
	
	/**
	 * 获得加密密码
	 *
	 * @param userPassword userPassword
	 * @return String
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		return DigestUtils.md5DigestAsHex((SaltConstant.SALT + userPassword).getBytes());
		
	}
	
	/**
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return long 注册成功之后的id
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		synchronized (userAccount.intern()) {
			// 账户不能重复
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("userAccount", userAccount);
			long count = this.baseMapper.selectCount(queryWrapper);
			if (count > 0) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
			}
			// 2. 加密
			String encryptPassword = getEncryptPassword(userPassword);
			// 3. 插入数据
			User user = new User();
			user.setUserAccount(userAccount);
			user.setUserPassword(encryptPassword);
			// 3. 给用户分配一个默认的头像
			user.setUserAvatar(Optional.ofNullable(user.getUserAvatar()).orElse(UserConstant.USER_AVATAR));
			boolean saveResult = this.save(user);
			if (!saveResult) {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
			}
			return user.getId();
		}
	}
	
	/**
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request      request
	 * @return {@link LoginUserVO}
	 */
	@Override
	public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 2. 加密
		String encryptPassword = getEncryptPassword(userPassword);
		// 查询用户是否存在
		LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class)
				.eq(User::getUserAccount, userAccount)
				.eq(User::getUserPassword, encryptPassword);
		User user = this.getOne(eq);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 3. 记录用户的登录态
		// 使用Sa-Token登录，并指定设备同端登录互斥
		StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
		StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, user);
		return this.getLoginUserVO(user);
	}
	
	/**
	 * 微信公共号登录
	 *
	 * @param wxOAuth2UserInfo 从微信获取的用户信息
	 * @param request          request
	 * @return {@link LoginUserVO}
	 */
	@Override
	public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
		String unionId = wxOAuth2UserInfo.getUnionId();
		String mpOpenId = wxOAuth2UserInfo.getOpenid();
		// 单机锁
		synchronized (unionId.intern()) {
			// 查询用户是否已存在
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("unionId", unionId);
			User user = this.getOne(queryWrapper);
			// 被封号，禁止登录
			if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
				throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
			}
			// 用户不存在则创建
			if (user == null) {
				user = new User();
				user.setUnionId(unionId);
				user.setMpOpenId(mpOpenId);
				user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
				user.setUserName(wxOAuth2UserInfo.getNickname());
				boolean result = this.save(user);
				if (!result) {
					throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
				}
			}
			// 记录用户的登录态
			request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
			return getLoginUserVO(user);
		}
	}
	
	/**
	 * 获取当前登录用户
	 *
	 * @param request request
	 * @return {@link User}
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 先判断是否已经登录
		Object loginUserId = StpUtil.getLoginIdDefaultNull();
		if (loginUserId == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		}
		// 从数据库查询（追求性能的话可以注释，直接走缓存）
		// StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		User currentUser = this.getById((String) loginUserId);
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return currentUser;
	}
	
	/**
	 * 获取当前登录用户（允许未登录）
	 *
	 * @param request request
	 * @return {@link User}
	 */
	@Override
	public User getLoginUserPermitNull(HttpServletRequest request) {
		// 先判断是否已登录
		User currentUser = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		if (currentUser == null || currentUser.getId() == null) {
			return null;
		}
		// 从数据库查询（追求性能的话可以注释，直接走缓存）
		long userId = currentUser.getId();
		return this.getById(userId);
	}
	
	/**
	 * 是否为管理员
	 *
	 * @param request request
	 * @return boolean 是否为管理员
	 */
	@Override
	public boolean isAdmin(HttpServletRequest request) {
		// 仅管理员可查询
		User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		return isAdmin(user);
	}
	
	/**
	 * 是否为管理员
	 *
	 * @param user user
	 * @return boolean 是否为管理员
	 */
	@Override
	public boolean isAdmin(User user) {
		return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
	}
	
	/**
	 * 用户注销
	 *
	 * @param request request
	 * @return boolean 是否退出成功
	 */
	@Override
	public boolean userLogout(HttpServletRequest request) {
		// 判断是否登录
		StpUtil.checkLogin();
		// 移除登录态
		StpUtil.logout();
		return true;
	}
	
	/**
	 * 获取登录用户视图类
	 *
	 * @param user user
	 * @return {@link LoginUserVO
	 * }
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		if (user == null) {
			return null;
		}
		// todo 在此处将实体类和 DTO 进行转换
		LoginUserVO loginUserVO = new LoginUserVO();
		BeanUtils.copyProperties(user, loginUserVO);
		// 设置将token保存到登录用户信息中
		loginUserVO.setToken(StpUtil.getTokenInfo().getTokenValue());
		return loginUserVO;
	}
	
	/**
	 * 获取用户VO封装类
	 *
	 * @param user    user
	 * @param request request
	 * @return {@link UserVO}
	 */
	@Override
	public UserVO getUserVO(User user, HttpServletRequest request) {
		// 对象转封装类
		return UserVO.objToVo(user);
	}
	
	
	/**
	 * 获得用户视图类列表
	 *
	 * @param userList userList
	 * @param request  request
	 * @return {@link List<UserVO>}
	 */
	@Override
	public List<UserVO> getUserVO(List<User> userList, HttpServletRequest request) {
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userList.stream().map(user -> getUserVO(user, request)).collect(Collectors.toList());
	}
	
	/**
	 * 分页获取用户视图类
	 *
	 * @param userPage userPage
	 * @param request  request
	 * @return {@link Page {@link UserVO} }
	 */
	@Override
	public Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request) {
		List<User> userList = userPage.getRecords();
		Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
		if (CollUtil.isEmpty(userList)) {
			return userVOPage;
		}
		// 填充信息
		List<UserVO> userVOList = userList.stream().map(UserVO::objToVo).collect(Collectors.toList());
		userVOPage.setRecords(userVOList);
		
		return userVOPage;
	}
	
	/**
	 * 获取查询封装类
	 *
	 * @param userQueryRequest userQueryRequest
	 * @return {@link QueryWrapper<User>}
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		Long id = userQueryRequest.getId();
		Long notId = userQueryRequest.getNotId();
		String unionId = userQueryRequest.getUnionId();
		String mpOpenId = userQueryRequest.getMpOpenId();
		String userName = userQueryRequest.getUserName();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		Integer userGender = userQueryRequest.getUserGender();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();
		String userEmail = userQueryRequest.getUserEmail();
		String userPhone = userQueryRequest.getUserPhone();
		String searchText = userQueryRequest.getSearchText();
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		
		// 精准查询
		queryWrapper.eq(id != null, "id", id);
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
		queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
		queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userGender), "userGender", userGender);
		// 拼接查询条件
		if (StringUtils.isNotBlank(searchText)) {
			queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
		queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
		queryWrapper.like(StringUtils.isNotBlank(userEmail), "userEmail", userEmail);
		queryWrapper.like(StringUtils.isNotBlank(userPhone), "userPhone", userPhone);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 导入用户数据
	 *
	 * @param file 上传的 Excel 文件
	 * @return 返回成功和错误信息
	 */
	@Override
	public Map<String, Object> importUsers(MultipartFile file) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.OPERATION_ERROR, "上传的文件为空");
		
		// 传递 userService 实例给 UserExcelListener
		UserExcelListener listener = new UserExcelListener(this);
		
		try {
			EasyExcel.read(file.getInputStream(), User.class, listener).sheet().doRead();
		} catch (IOException e) {
			log.error("文件读取失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件读取失败");
		} catch (ExcelAnalysisException e) {
			log.error("Excel解析失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "Excel解析失败");
		}
		
		// 返回处理结果，包括成功和异常的数据
		Map<String, Object> result = new HashMap<>();
		// 获取异常记录
		result.put("errorRecords", listener.getErrorRecords());
		log.info("成功导入 {} 条用户数据，{} 条错误数据", listener.getSuccessRecords().size(), listener.getErrorRecords().size());
		
		return result;
	}
	
}
