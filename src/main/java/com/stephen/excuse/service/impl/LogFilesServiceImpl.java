package com.stephen.excuse.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.mapper.LogFilesMapper;
import com.stephen.excuse.model.entity.LogFiles;
import com.stephen.excuse.model.enums.file.FileUploadBizEnum;
import com.stephen.excuse.service.LogFilesService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * @author stephen qiu
 * @description 针对表【log_files(文件上传日志记录表)】的数据库操作Service实现
 */
@Service
public class LogFilesServiceImpl extends ServiceImpl<LogFilesMapper, LogFiles>
		implements LogFilesService {
	
	/**
	 * 校验文件
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	@Override
	public void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
		// 文件大小
		long fileSize = multipartFile.getSize();
		// 文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
			long ONE_M = 5 * 1024 * 1024L;
			if (fileSize > ONE_M) {
				throw new BusinessException(ErrorCode.PARAMS_SIZE_ERROR, "文件大小不能超过 5M");
			}
			if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
			}
		}
	}
	
	/**
	 * 校验图片
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	@Override
	public void validPicture(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
		// 文件大小
		long fileSize = multipartFile.getSize();
		// 文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		if (FileUploadBizEnum.PICTURE.equals(fileUploadBizEnum)) {
			long ONE_M = 10 * 1024 * 1024L;
			if (fileSize > ONE_M) {
				throw new BusinessException(ErrorCode.PARAMS_SIZE_ERROR, "图片大小不能超过10M");
			}
			if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片类型错误");
			}
		}
	}
	
	
	
}




