/*
 * @(#) ReadProperties
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2018
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author ulyn
 * <br> 2018-09-13 17:58:01
 */

package com.sunsharing.skyseamapproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.Data;

@ConfigurationProperties("proxyConfig")
@Data
@Component
public class MyProxyConfig {

    private List<String> docPreviewType;
    private String svnUrl;
    private String docPreviewUrl;
}