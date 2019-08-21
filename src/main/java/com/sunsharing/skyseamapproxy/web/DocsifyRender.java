package com.sunsharing.skyseamapproxy.web;

import com.sunsharing.component.utils.web.ResponseUtils;

import javax.servlet.http.HttpServletResponse;

public class DocsifyRender {


    private final static String html = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "  <meta charset=\"UTF-8\">\n" +
        "  <title>docsify</title>\n" +
        "  <link rel=\"icon\" href=\"https://docsify.js.org/_media/favicon.ico\">\n" +
        "  <meta name=\"keywords\" content=\"doc,docs,documentation,gitbook,creator,generator,github,jekyll,github-pages\">\n" +
        "  <meta name=\"description\" content=\"A magical documentation generator.\">\n" +
        "  <meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">\n" +
        "  <link rel=\"stylesheet\" href=\"//unpkg.com/docsify/lib/themes/vue.css\" title=\"vue\">\n" +
        "  <link rel=\"stylesheet\" href=\"//unpkg.com/docsify/lib/themes/dark.css\" title=\"dark\" disabled>\n" +
        "  <link rel=\"stylesheet\" href=\"//unpkg.com/docsify/lib/themes/buble.css\" title=\"buble\" disabled>\n" +
        "  <link rel=\"stylesheet\" href=\"//unpkg.com/docsify/lib/themes/pure.css\" title=\"pure\" disabled>\n" +
        "  <style>\n" +
        "    nav.app-nav li ul {\n" +
        "      min-width: 100px;\n" +
        "    }\n" +
        "  </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "  <div id=\"app\">Loading ...</div>\n" +
        "  <script>\n" +
        "    window.$docsify = {\n" +
        "      auto2top: true,\n" +
        "      coverpage: true,\n" +
        "      executeScript: true,\n" +
        "      loadSidebar: true,\n" +
        "      loadNavbar: true,\n" +
        "      mergeNavbar: true,\n" +
        "      maxLevel: 4,\n" +
        "      subMaxLevel: 2,\n" +
        "      name: '在线MD预览',\n" +
        "      requestHeaders: {\n" +
        "    'svn-view': 'true'\n" +
        "  },search: {\n" +
        "        noData: {\n" +
        "          '/de-de/': 'Keine Ergebnisse!',\n" +
        "          '/zh-cn/': '没有结果!',\n" +
        "          '/': 'No results!'\n" +
        "        },\n" +
        "        paths: 'auto',\n" +
        "        placeholder: {\n" +
        "          '/de-de/': 'Suche',\n" +
        "          '/zh-cn/': '搜索',\n" +
        "          '/': 'Search'\n" +
        "        }\n" +
        "      },\n" +
        "      formatUpdated: '{MM}/{DD} {HH}:{mm}'" +
        "    }\n" +
        "  </script>" +
        "<script src=\"//unpkg.com/docsify/lib/docsify.min.js\"></script>\n" +
        "<script src=\"//unpkg.com/docsify/lib/plugins/search.min.js\"></script>\n" +
        "<script src=\"//unpkg.com/prismjs/components/prism-bash.min.js\"></script>\n" +
        "<script src=\"//unpkg.com/prismjs/components/prism-markdown.min.js\"></script>\n" +
        "<script src=\"//unpkg.com/prismjs/components/prism-nginx.min.js\"></script>\n" +
        "<script>location.href = '#%s'</script>\n" +
        "</body>\n" +
        "</html>";

    public static void render(String uri, HttpServletResponse response) {
        String name = uri.substring(uri.lastIndexOf("/"), uri.lastIndexOf("."));
        ResponseUtils.renderHtml(response, String.format(html, name));
    }

}
