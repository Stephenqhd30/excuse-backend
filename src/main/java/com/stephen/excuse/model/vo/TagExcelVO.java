package com.stephen.excuse.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 导出标签视图
 *
 * @author stephen
 */
@Data
public class TagExcelVO implements Serializable {
	
	private static final long serialVersionUID = -8843841908950627489L;
	
	/**
	 * 标签名称
	 */
	@ExcelProperty("标签名称")
	@ColumnWidth(25)
	private String tagName;
}
