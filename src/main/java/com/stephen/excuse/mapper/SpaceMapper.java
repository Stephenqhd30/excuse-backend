package com.stephen.excuse.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stephen.excuse.model.entity.Space;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author stephen qiu
 * @description 针对表【space(空间)】的数据库操作Mapper
 * @createDate 2025-01-17 19:06:28
 * @Entity com.stephen.excuse.model.entity.Space
 */
public interface SpaceMapper extends BaseMapper<Space> {
	
	/**
	 * 获取存储使用量排名前 N 的空间
	 * @param topN 排名前 N
	 * @return List<Space>
	 */
	List<Space> getTopNSpaceUsage(int topN);
	
	/**
	 * 删除某用户的所有空间
	 *
	 * @param userId 用户 ID
	 * @return 删除的记录数
	 */
	int deleteByUserId(Long userId);
}




