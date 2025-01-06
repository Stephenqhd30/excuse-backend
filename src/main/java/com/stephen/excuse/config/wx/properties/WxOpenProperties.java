package com.stephen.excuse.config.wx.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信开放平台配置信息
 *
 * @author: stephen qiu
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.open")
public class WxOpenProperties {
	/**
	 * appid
	 */
	private String appId;
	
	/**
	 * appSecret
	 */
	private String appSecret;
}
