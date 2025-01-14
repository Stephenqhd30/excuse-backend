package com.stephen.excuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.excuse.model.dto.picture.PictureQueryRequest;
import com.stephen.excuse.model.dto.picture.PictureReviewRequest;
import com.stephen.excuse.model.dto.picture.PictureUploadByBatchRequest;
import com.stephen.excuse.model.dto.picture.PictureUploadRequest;
import com.stephen.excuse.model.entity.Picture;
import com.stephen.excuse.model.entity.User;
import com.stephen.excuse.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 图片服务
 *
 * @author stephen qiu
 */
public interface PictureService extends IService<Picture> {
	
	/**
	 * 校验数据
	 *
	 * @param picture picture
	 * @param add     对创建的数据进行校验
	 */
	void validPicture(Picture picture, boolean add);
	
	/**
	 * 校验数据
	 * @param fileUrl fileUrl
	 */
	void validPicture(String fileUrl);
	
	/**
	 * 获取查询条件
	 *
	 * @param pictureQueryRequest pictureQueryRequest
	 * @return {@link QueryWrapper<Picture>}
	 */
	QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);
	
	/**
	 * 获取图片封装
	 *
	 * @param picture picture
	 * @param request request
	 * @return {@link PictureVO}
	 */
	PictureVO getPictureVO(Picture picture, HttpServletRequest request);
	
	/**
	 * 分页获取图片封装
	 *
	 * @param picturePage picturePage
	 * @param request     request
	 * @return {@link Page<PictureVO>}
	 */
	Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);
	
	/**
	 * 图片审核
	 *
	 * @param pictureReviewRequest pictureReviewRequest
	 * @param loginUser            loginUser
	 */
	void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);
	
	/**
	 * 批量审核图片审核
	 *
	 * @param pictureReviewRequest pictureReviewRequest
	 * @param loginUser            loginUser
	 */
	void doPictureReviewByBatch(PictureReviewRequest pictureReviewRequest, User loginUser);
	
	/**
	 * 填充审核参数
	 *
	 * @param picture   picture
	 * @param loginUser loginUser
	 * @return {@link Picture}
	 */
	Picture fillReviewParams(Picture picture, User loginUser);
	
	/**
	 * 上传图片
	 *
	 * @param multipartFile        multipartFile
	 * @param pictureUploadRequest pictureUploadRequest
	 * @param loginUser            loginUser
	 * @return {@link PictureVO}
	 */
	PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
	                        User loginUser) throws IOException;
	/**
	 * 上传图片(根据地址)
	 *
	 * @param fileUrl              fileUrl
	 * @param pictureUploadRequest pictureUploadRequest
	 * @param loginUser            loginUser
	 * @return {@link PictureVO}
	 */
	PictureVO uploadPicture(String fileUrl, PictureUploadRequest pictureUploadRequest, User loginUser) throws IOException;
	
	/**
	 * 批量抓取和创建图片
	 *
	 * @param pictureUploadByBatchRequest pictureUploadByBatchRequest
	 * @param loginUser                   loginUser
	 * @return 成功创建的图片数
	 */
	Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
	
}