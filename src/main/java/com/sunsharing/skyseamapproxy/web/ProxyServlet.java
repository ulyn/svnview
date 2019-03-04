/*
 * @(#) SysFilter
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2018
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author ningyp
 * <br> 2018-09-13 17:28:15
 * <br> @version 1.0
 * ————————————————————————————————
 *    修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 * ————————————————————————————————
 */

package com.sunsharing.skyseamapproxy.web;

import com.alibaba.fastjson.JSON;
import com.sunsharing.component.utils.web.ResponseUtils;
import com.sunsharing.component.utils.web.filter.CacheHttpServletResponseWrapper;
import com.sunsharing.skyseamapproxy.config.MyProxyConfig;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

@WebServlet(urlPatterns = "/*")
@Log4j2
public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {
    private List<String> imageFileType = Arrays.asList(new String[]{"png", "jpg", "jpeg", "gif", "bmp"});

    @Autowired
    MyProxyConfig myProxyConfig;

    @Override
    protected String getConfigParam(String key) {
        if ("targetUri".equals(key)) {
            return myProxyConfig.getSvnUrl();
        }
        return super.getConfigParam(key);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.info("SvnUrl:" + myProxyConfig.getSvnUrl());
        log.info("DocPreviewUrl:" + myProxyConfig.getDocPreviewUrl());
        log.info("DocPreviewType:" + myProxyConfig.getDocPreviewType());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String staticSourcePath = req.getContextPath() + "/static";
        if (uri.startsWith(staticSourcePath)) {
            //静态资源输出
            String sourceUri = uri.substring(staticSourcePath.length());
            InputStream in = ProxyServlet.class.getClassLoader().getResourceAsStream("static" + sourceUri);
            if (uri.endsWith(".html") || uri.endsWith(".htm")) {
                resp.setContentType("text/html");
                resp.setHeader("Content-Type", "text/html");
            } else if (uri.endsWith(".css")) {
                resp.setContentType("text/css");
                resp.setHeader("Content-Type", "text/css");
            } else if (uri.endsWith(".js")) {
                resp.setContentType("application/javascript");
                resp.setHeader("Content-Type", "text/javascript");
            }
            IOUtils.copy(in, resp.getOutputStream());
            return;
        } else if (isPreviewResource(uri)) {
            String authorization = req.getHeader("Authorization");
            if(StringUtils.isBlank(authorization)){
                //未登录
                ResponseUtils.renderHtml(resp,"<html><head></head><body>"
                    + "<script>"
                    + "alert('尚未进行svn验证，请先验证');"
                    + "location.href='"+ uri.substring(0,uri.lastIndexOf("/")) +"';"
                    + "</script>"
                    + "</body></html>");
                return;
            }
            resp.sendRedirect(myProxyConfig.getDocPreviewUrl()
                + "/onlinePreview?_head_Authorization=" + authorization
                + "&url=" + URLEncoder.encode(myProxyConfig.getSvnUrl() + uri, Charsets.UTF_8.displayName()));
            return;
        }
        // response的header设置，要在缓冲区装入响应内容之前，http的协议是按照响应状态行、各响应头和响应正文的顺序输出的，后写的header就不生效了。
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CacheHttpServletResponseWrapper cacheHttpServletResponseWrapper
            = new CacheHttpServletResponseWrapper(resp, outputStream, "utf-8");
        super.service(req, cacheHttpServletResponseWrapper);
        if (uri.endsWith(".html") || uri.endsWith(".htm")) {
            resp.setContentType("text/html");
            resp.setHeader("Content-Type", "text/html");
        } else if (uri.endsWith(".css")) {
            resp.setContentType("text/css");
            resp.setHeader("Content-Type", "text/css");
        } else if (uri.endsWith(".js")) {
            resp.setContentType("application/javascript");
            resp.setHeader("Content-Type", "text/javascript");
        } else if (StringUtils.isNotBlank(resp.getContentType()) && resp.getContentType().startsWith("text/html")) {
            //html的 认为是目录的页面，额外输出js
            String chartset = ResponseUtils.getCharsetByContentType(resp.getContentType());
            String html = new String(outputStream.toByteArray(), chartset);
            html = html.replace("</body>",
                "<script>var DocPreviewType = " + JSON.toJSONString(myProxyConfig.getDocPreviewType()) + ";</script>"
                    + "<script src='" + req.getContextPath() + "/static/dirview.js'></script></body>");
            byte[] bytes = html.getBytes(chartset);
            resp.setIntHeader("Content-Length", bytes.length);
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Cache-Control", "no-cache");
            resp.setDateHeader("Expires", -1);
            resp.setIntHeader("Content-Length", bytes.length);
            resp.getOutputStream().write(bytes);
            return;
        } else if (StringUtils.isNotBlank(req.getParameter("scale")) && isImage(uri)) {
            //图片 要压缩的
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(outputStream.toByteArray()))
                .size(100, 90)
                .outputFormat("jpg")
                .toOutputStream(outStream);
            byte[] bytes = outStream.toByteArray();
            resp.setContentLength(bytes.length);
            resp.getOutputStream().write(bytes);
            return;
        }
        resp.getOutputStream().write(outputStream.toByteArray());
    }

    private boolean isImage(String uri) {
        String lowerCaseUri = uri.toLowerCase();
        for (String i : imageFileType) {
            if (lowerCaseUri.endsWith("." + i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreviewResource(String uri) {
        for (String type : myProxyConfig.getDocPreviewType()) {
            if (uri.endsWith("." + type)) {
                return true;
            }
        }
        return false;
    }

}
