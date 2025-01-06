package com.stephen.excuse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.excuse.model.dto.picture.PictureQueryRequest;
import com.stephen.excuse.model.entity.Picture;
import com.stephen.excuse.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

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
     * @param add 对创建的数据进行校验
     */
    void validPicture(Picture picture, boolean add);

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
     * @param request request
     * @return {@link Page<PictureVO>}
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);
}