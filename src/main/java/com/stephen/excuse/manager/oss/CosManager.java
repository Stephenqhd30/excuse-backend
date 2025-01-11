package com.stephen.excuse.manager.oss;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.stephen.excuse.common.ErrorCode;
import com.stephen.excuse.common.ThrowUtils;
import com.stephen.excuse.common.exception.BusinessException;
import com.stephen.excuse.config.oss.cos.condition.CosCondition;
import com.stephen.excuse.config.oss.cos.properties.CosProperties;
import com.stephen.excuse.constants.FileConstant;
import com.stephen.excuse.model.vo.PictureUploadResult;
import com.stephen.excuse.model.entity.LogFiles;
import com.stephen.excuse.model.enums.oss.OssTypeEnum;
import com.stephen.excuse.service.LogFilesService;
import com.stephen.excuse.utils.encrypt.SHA3Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Cos 对象存储操作
 *
 * @author stephen qiu
 */
@Component
@Slf4j
@Conditional(CosCondition.class)
public class CosManager {
	
	@Resource
	private CosProperties cosProperties;
	
	@Resource
	private COSClient cosClient;
	
	@Resource
	private LogFilesService logFilesService;
	
	/**
	 * 上传对象
	 *
	 * @param key           唯一键
	 * @param localFilePath 本地文件路径
	 */
	public void putObject(String key, String localFilePath) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				new File(localFilePath));
		cosClient.putObject(putObjectRequest);
	}
	
	/**
	 * 上传对象
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public void putObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				file);
		cosClient.putObject(putObjectRequest);
	}
	
	/**
	 * 上传对象（附带图片信息）
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public PutObjectResult putPictureObject(File file, String key) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				file);
		// 对图片进行处理（获取基本信息也被视作为一种处理）
		PicOperations picOperations = new PicOperations();
		// 1 表示返回原图信息
		picOperations.setIsPicInfo(1);
		// 构造处理参数
		putObjectRequest.setPicOperations(picOperations);
		return cosClient.putObject(putObjectRequest);
	}
	
	/**
	 * 上传文件到 COS
	 *
	 * @param file 待上传的文件
	 * @param path 上传的路径
	 * @return 文件在 COS 的 URL
	 */
	@Transactional(rollbackFor = Exception.class)
	public String uploadToCos(MultipartFile file, String path) throws IOException {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
		// 获取文件的原始名称和后缀
		String originalName = file.getOriginalFilename();
		String suffix = FilenameUtils.getExtension(originalName);
		long fileSize = file.getSize();
		// 生成唯一键
		String uniqueKey = SHA3Utils.encrypt(Arrays.toString(file.getBytes()) + originalName + suffix);
		String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
		String filePath = (StringUtils.isBlank(path) ? "" : path + "/") + fileName;
		
		try (InputStream inputStream = file.getInputStream()) {
			// 上传到 COS
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(fileSize);
			PutObjectRequest putRequest = new PutObjectRequest(cosProperties.getBucket(), filePath, inputStream, metadata);
			cosClient.putObject(putRequest);
		} catch (IOException | CosClientException e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败" + e.getMessage());
		}
		
		// 保存文件信息
		LogFiles logFile = new LogFiles();
		logFile.setFileKey(uniqueKey);
		logFile.setFileName(fileName);
		logFile.setFileOriginalName(originalName);
		logFile.setFileSuffix(suffix);
		logFile.setFileSize(fileSize);
		logFile.setFileUrl(FileConstant.COS_HOST + filePath);
		logFile.setFileOssType(OssTypeEnum.COS.getValue());
		logFilesService.save(logFile);
		
		return FileConstant.COS_HOST + filePath;
	}
	
	/**
	 * 上传图片
	 *
	 * @param multipartFile    文件
	 * @param uploadPathPrefix 上传路径前缀
	 * @return {@link PictureUploadResult}
	 */
	public PictureUploadResult uploadPictureToCos(MultipartFile multipartFile, String uploadPathPrefix) throws IOException {
		// 图片上传地址
		String uuid = RandomUtil.randomString(16);
		String originFileName = multipartFile.getOriginalFilename();
		String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
				FileUtil.getSuffix(originFileName));
		String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
		File file = null;
		try {
			// 创建临时文件
			file = File.createTempFile(uploadPath, null);
			multipartFile.transferTo(file);
			// 上传图片
			PutObjectResult putObjectResult = this.putPictureObject(file, uploadPath);
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
			// 封装返回结果
			PictureUploadResult uploadPictureResult = new PictureUploadResult();
			int picWidth = imageInfo.getWidth();
			int picHeight = imageInfo.getHeight();
			double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
			uploadPictureResult.setPicName(FileUtil.mainName(originFileName));
			uploadPictureResult.setPicWidth(picWidth);
			uploadPictureResult.setPicHeight(picHeight);
			uploadPictureResult.setPicScale(picScale);
			uploadPictureResult.setPicFormat(imageInfo.getFormat());
			uploadPictureResult.setPicSize(FileUtil.size(file));
			uploadPictureResult.setUrl(FileConstant.COS_HOST + "/" + uploadPath);
			return uploadPictureResult;
		} catch (Exception e) {
			log.error("图片上传到对象存储失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		} finally {
			this.deleteTempFile(file);
		}
	}
	
	
	/**
	 * 从COS中删除文件
	 *
	 * @param id 文件ID
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteInCosById(Long id) {
		LogFiles fileInDatabase = logFilesService.getOne(
				new LambdaQueryWrapper<LogFiles>()
						.eq(LogFiles::getId, id)
						.eq(LogFiles::getFileOssType, OssTypeEnum.COS.getValue())
		);
		ThrowUtils.throwIf(ObjectUtils.isEmpty(fileInDatabase), ErrorCode.NOT_FOUND_ERROR, "文件不存在");
		
		LambdaQueryWrapper<LogFiles> fileLambdaQueryWrapper = Wrappers.lambdaQuery(LogFiles.class)
				.eq(LogFiles::getFileKey, fileInDatabase.getFileKey())
				.eq(LogFiles::getFileOssType, OssTypeEnum.COS.getValue())
				// 搭配事务给数据库被读行数据加行锁
				.last("FOR UPDATE");
		List<LogFiles> filesInDatabase = logFilesService.list(fileLambdaQueryWrapper);
		if (!logFilesService.removeById(fileInDatabase.getId())) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据有误");
		}
		if (Objects.equals(filesInDatabase.size(), 1)) {
			// 调用通过问价你的 Url 删除文件
			deleteInCosByUrl(fileInDatabase.getFileUrl());
		}
	}
	
	/**
	 * 通过文件的url从COS中删除文件
	 *
	 * @param url 文件URL
	 */
	private void deleteInCosByUrl(String url) {
		ThrowUtils.throwIf(StringUtils.isEmpty(url), ErrorCode.PARAMS_ERROR, "被删除地址为空");
		String bucket = cosProperties.getBucket();
		String[] split = url.split(bucket);
		if (split.length != 2) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "URL格式错误");
		}
		// 移除前导斜杠
		String key = split[1].startsWith("/") ? split[1].substring(1) : split[1];
		try {
			cosClient.deleteObject(bucket, key);
		} catch (CosClientException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败: " + e.getMessage());
		}
	}
	
	/**
	 * 删除临时文件
	 */
	public void deleteTempFile(File file) {
		if (file == null) {
			return;
		}
		// 删除临时文件
		boolean deleteResult = file.delete();
		if (!deleteResult) {
			log.error("file delete error, filepath = {}", file.getAbsolutePath());
		}
	}
	
}
