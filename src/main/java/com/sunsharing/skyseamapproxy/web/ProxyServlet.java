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
import com.sunsharing.component.utils.base.MapHelper;
import com.sunsharing.component.utils.web.ResponseUtils;
import com.sunsharing.component.utils.web.filter.CacheHttpServletResponseWrapper;
import com.sunsharing.skyseamapproxy.config.MyProxyConfig;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

@WebServlet(urlPatterns = "/*")
@Log4j2
public class ProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {

    private final String FAVICON_URI = "/favicon.ico";
    private final static List<String> imageFileType = Arrays.asList(new String[]{"png", "jpg", "jpeg", "gif", "bmp"});
    private final static Map<String, String> fileContentTypeMap = MapHelper.ofHashMap(
        "htm", "text/html",
        "html", "text/html",
        "css", "text/css",
        "js", "application/javascript",
        "svg", "image/svg+xml",
        "ico", "image/x-icon"
    );

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
        //只允许浏览器访问。
        String accept = req.getHeader("Accept");
        String acceptLanguage = req.getHeader("Accept-Language");
        if (StringUtils.isBlank(accept)
            || StringUtils.isBlank(acceptLanguage)
            || !StringUtils.equalsIgnoreCase("GET", req.getMethod())) {
            throw new RuntimeException("抱歉，请使用原始地址进行SVN操作！");
        }
        //如果是请求/favicon.ico 直接返回。svn不存在这个地址
        if (StringUtils.equals(req.getRequestURI(), FAVICON_URI)) {
            InputStream in = ProxyServlet.class.getClassLoader().getResourceAsStream(FAVICON_URI.substring(1));
            setContentType(FAVICON_URI, resp);
            ResponseUtils.setRespHeaderCache(resp, 3600 * 24 * 7);
            IOUtils.copy(in, resp.getOutputStream());
            return;
        }

        String uri = req.getRequestURI();
        String staticSourcePath = req.getContextPath() + "/static";
        if (uri.startsWith(staticSourcePath)) {
            //静态资源输出
            String sourceUri = uri.substring(staticSourcePath.length());
            InputStream in = ProxyServlet.class.getClassLoader().getResourceAsStream("static" + sourceUri);
            setContentType(uri, resp);
            if (sourceUri.startsWith("/lib")) {
                ResponseUtils.setRespHeaderCache(resp, 3600 * 24 * 7);
            } else {
                ResponseUtils.setRespHeaderCache(resp, 3600);
            }
            IOUtils.copy(in, resp.getOutputStream());
            return;
        } else if (StringUtils.isNotBlank(req.getParameter("getDocsify"))) {
            String targetPath = req.getParameter("getDocsify");
            //输出docsify
            DocsifyRender.render(targetPath, resp);
            return;
        } else if (uri.toLowerCase().endsWith(".md")) {
            //md文件预览
            String formDocsify = req.getHeader("svn-view");
            if (StringUtils.isNotBlank(formDocsify)) {
                //docsify来读取的，ignore，后续直接输出即可
            } else {
                //重定向到docsify
                String url = uri.substring(0, uri.lastIndexOf("/"));
                resp.sendRedirect(url + "/?getDocsify=" + URLEncoder.encode(uri, "utf-8"));
                return;
            }
        } else if (isPreviewResource(uri)) {
            String authorization = req.getHeader("Authorization");
            if (StringUtils.isBlank(authorization)) {
                //未登录
                ResponseUtils.renderHtml(resp, "<html><head></head><body>"
                    + "<script>"
                    + "alert('尚未进行svn验证，请先验证');"
                    + "location.href='" + uri.substring(0, uri.lastIndexOf("/")) + "';"
                    + "</script>"
                    + "</body></html>");
                return;
            } else {
                String previewUrl = myProxyConfig.getDocPreviewUrl()
                    + "/onlinePreview?_head_Authorization=" + authorization
                    + "&url=" + URLEncoder.encode(myProxyConfig.getSvnUrl() + uri, Charsets.UTF_8.displayName());
                ResponseUtils.renderHtml(resp, "<!DOCTYPE html>\n"
                    + "<html lang=\"en\">\n"
                    + "<head>\n"
                    + "    <meta charset=\"UTF-8\">\n"
                    + "    <title>正在加载中...</title>\n"
                    + "    <style type=\"text/css\">\n"
                    + "        .box{\n"
                    + "            width:300px;\n"
                    + "            height:125px;\n"
                    + "            border:0px solid #000;\n"
                    + "            margin:200px auto 0;\n"
                    + "        }\n"
                    + "\n"
                    + "        .box p{\n"
                    + "            text-align: center;\n"
                    + "            width:100%;\n"
                    + "            float:left;\n"
                    + "            /*p标签默认有样式*/\n"
                    + "            margin:0;\n"
                    + "            padding:0;\n"
                    + "        }\n"
                    + "\n"
                    + "        .box div{\n"
                    + "            width:30px;\n"
                    + "            height:70px;\n"
                    + "            margin:15px;\n"
                    + "            float: left;\n"
                    + "            background-color: hotpink;\n"
                    + "            border-radius:10px;\n"
                    + "        }\n"
                    + "\n"
                    + "        .box div:nth-child(1){\n"
                    + "            background-color: lightcoral;\n"
                    + "            /*缩、放 这是两次，所以是2*/\n"
                    + "            /*animation:loading 0.5s ease 0s 2 alternate;*/\n"
                    + "            animation: loading 0.5s ease 0s infinite alternate;\n"
                    + "        }\n"
                    + "        .box div:nth-child(2){\n"
                    + "            background-color: darkorange;\n"
                    + "            animation: loading 0.5s ease 0.1s infinite alternate;\n"
                    + "        }\n"
                    + "        .box div:nth-child(3){\n"
                    + "            background-color: lightcoral;\n"
                    + "            animation: loading 0.5s ease 0.2s infinite alternate;\n"
                    + "        }\n"
                    + "        .box div:nth-child(4){\n"
                    + "            background-color: gold;\n"
                    + "            animation: loading 0.5s ease 0.3s infinite alternate;\n"
                    + "        }\n"
                    + "        .box div:nth-child(5){\n"
                    + "            background-color: burlywood;\n"
                    + "            animation: loading 0.5s ease 0.4s infinite alternate;\n"
                    + "        }\n"
                    + "\n"
                    + "        @keyframes loading{\n"
                    + "            from{\n"
                    + "                /*缩放y轴*/\n"
                    + "                transform:scaleY(1)\n"
                    + "            }\n"
                    + "\n"
                    + "            to{\n"
                    + "                transform: scaleY(0.5);\n"
                    + "            }\n"
                    + "\n"
                    + "        }\n"
                    + "    </style>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "    <div class=\"box\">\n"
                    + "        <div></div>\n"
                    + "        <div></div>\n"
                    + "        <div></div>\n"
                    + "        <div></div>\n"
                    + "        <div></div>\n"
                    + "        <p>正在生成预览，请耐心等待...</p>\n"
                    + "    </div>\n"
                    + "<script>"
                    + "location.href='" + previewUrl + "';"
                    + "</script>"
                    + "</body>\n"
                    + "</html>");
                return;
            }
        }
        // response的header设置，要在缓冲区装入响应内容之前，http的协议是按照响应状态行、各响应头和响应正文的顺序输出的，后写的header就不生效了。
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CacheHttpServletResponseWrapper cacheHttpServletResponseWrapper
            = new CacheHttpServletResponseWrapper(resp, outputStream, "utf-8");
        super.service(req, cacheHttpServletResponseWrapper);
        if (setContentType(uri, resp)) {

        } else if (uri.toLowerCase().endsWith("md")) {
            setNoCache(resp);
        } else if (StringUtils.isNotBlank(resp.getContentType()) && resp.getContentType().startsWith("text/html")) {
            //html的 认为是目录的页面，额外输出js
            String chartset = ResponseUtils.getCharsetByContentType(resp.getContentType());
            String html = new String(outputStream.toByteArray(), chartset);
            html = html.replace("</body>",
                "<script>var DocPreviewType = " + JSON.toJSONString(myProxyConfig.getDocPreviewType()) + ";</script>"
                    + "<script src='" + req.getContextPath() + "/static/dirview.js'></script></body>");
            byte[] bytes = html.getBytes(chartset);
            resp.setIntHeader("Content-Length", bytes.length);
            setNoCache(resp);
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

    private void setNoCache(HttpServletResponse resp) {
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", -1);
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

    private boolean setContentType(String uri, HttpServletResponse resp) {
        for (String key : fileContentTypeMap.keySet()) {
            if (StringUtils.endsWithIgnoreCase(uri, "." + key)) {
                System.out.println("find setContentType:" + uri);
                resp.setContentType(fileContentTypeMap.get(key));
                resp.setHeader("Content-Type", fileContentTypeMap.get(key));
                return true;
            }
        }
        return false;
    }
}
