package com.stephen.excuse.config.wx.condition;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 微信开放平台
 *
 * @author stephen qiu
 */
public class WxOpenCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String enabled = context.getEnvironment().getProperty("wx.enabled");
		return Boolean.parseBoolean(enabled);
	}
}
