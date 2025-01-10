package com.stephen.excuse.config.websocket.modal;

import lombok.Data;

/**
 * Description: ws的基本返回信息体
 *
 * @author stephen qiu
 */
@Data
public class WSBaseResp<T> {
	/**
	 * ws推送给前端的消息类型
	 */
	private Integer type;
	
	/**
	 * 数据
	 */
	private T data;
}