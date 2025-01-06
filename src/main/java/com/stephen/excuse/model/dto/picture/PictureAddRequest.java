package com.stephen.excuse.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建图片请求
 *
 * @author stephen qiu
 */
@Data
public class PictureAddRequest implements Serializable {
    
    /**
     * 图片 url
     */
    private String url;
    
    /**
     * 图片名称
     */
    private String name;
    
    /**
     * 简介
     */
    private String introduction;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;
    

    private static final long serialVersionUID = 1L;
}