package com.stephen.popcorn.config.wx;

import com.stephen.popcorn.config.wx.condition.WxOpenCondition;
import com.stephen.popcorn.config.wx.properties.WxOpenProperties;
import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 微信开放平台配置
 *
 * @author stephen qiu
 */
@Configuration
@Conditional(WxOpenCondition.class)
public class WxOpenConfiguration {
	
	@Resource
	private WxOpenProperties wxOpenProperties;
	
	private WxMpService wxMpService;
	
	/**
	 * 单例模式（不用 @Bean 是为了防止和公众号的 service 冲突）
	 *
	 * @return {@link WxMpService}
	 */
	public WxMpService getWxMpService() {
		if (wxMpService != null) {
			return wxMpService;
		}
		synchronized (this) {
			if (wxMpService != null) {
				return wxMpService;
			}
			WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
			config.setAppId(wxOpenProperties.getAppId());
			config.setSecret(wxOpenProperties.getAppSecret());
			WxMpService service = new WxMpServiceImpl();
			service.setWxMpConfigStorage(config);
			wxMpService = service;
			return wxMpService;
		}
	}
}