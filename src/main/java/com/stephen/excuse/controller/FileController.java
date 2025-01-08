package com.stephen.excuse.controller;

import com.stephen.excuse.common.BaseResponse;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ResultUtils;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.model.dto.file.UploadFileRequest;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.enums.file.FileUploadBizEnum;
import com.stephen.excuse.service.LogFilesService;
import com.stephen.excuse.service.UserService;
import com.stephen.excuse.utils.oss.CosUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 文件接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
	
	@Resource
	private UserService userService;
	
	@Resource
	private LogFilesService logFilesService;
	
	
	/**
	 * 文件上传(使用COS对象存储)
	 *
	 * @param multipartFile     multipartFile
	 * @param uploadFileRequest uploadFileRequest
	 * @param request           request
	 * @return BaseResponse<String>
	 */
	@PostMapping("/upload")
	public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
	                                       UploadFileRequest uploadFileRequest, HttpServletRequest request) {
		String biz = uploadFileRequest.getBiz();
		FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
		ThrowUtils.throwIf(fileUploadBizEnum == null, ErrorCode.PARAMS_ERROR, "文件上传有误");
		
		// 校验文件类型
		logFilesService.validFile(multipartFile, fileUploadBizEnum);
		User loginUser = userService.getLoginUser(request);
		
		// 文件目录：根据业务、用户来划分
		String path = String.format("/%s/%s/%s", "stephen", fileUploadBizEnum.getValue(), loginUser.getId());
		// 直接上传文件
		String s = CosUtils.uploadFile(multipartFile, path);
		// 返回可访问地址
		return ResultUtils.success(s);
		
	}
}
