package com.stephen.excuse;

import com.stephen.excuse.config.wx.WxOpenConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * 主类测试
 *
 * @author stephen qiu
 * 
 */
@SpringBootTest
class ExcuseApplicationTests {

    @Resource
    private WxOpenConfiguration wxOpenConfiguration;

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfiguration);
    }

}
