package com.stephen.excuse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.excuse.mapper.LogFilesMapper;
import com.stephen.excuse.model.entity.LogFiles;
import com.stephen.excuse.service.LogFilesService;
import org.springframework.stereotype.Service;

/**
 * @author stephen qiu
 * @description 针对表【log_files(文件上传日志记录表)】的数据库操作Service实现
 * @createDate 2024-10-21 12:05:24
 */
@Service
public class LogFilesServiceImpl extends ServiceImpl<LogFilesMapper, LogFiles>
		implements LogFilesService {
	
	
}




