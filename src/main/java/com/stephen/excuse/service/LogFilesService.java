package com.stephen.excuse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.excuse.model.entity.LogFiles;
import com.stephen.excuse.model.enums.file.FileUploadBizEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author stephen qiu
 * @description 针对表【log_files(文件上传日志记录表)】的数据库操作Service
 * @createDate 2024-10-21 12:05:24
 */
public interface LogFilesService extends IService<LogFiles> {
	
	/**
	 * 校验文件
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);
	
	/**
	 * 校验文件
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	void validPicture(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);
}
