var userAgent = window.navigator.userAgent.toLowerCase();
var DirView = {
    ie:/msie/.test(userAgent),
    moz:/gecko/.test(userAgent),
    opera:/opera/.test(userAgent),
    safari:/safari/.test(userAgent)
};
DirView.getBaseUrl = function (baseUrl) {
    //遍历获取
    var scripts = document.getElementsByTagName("script");
    if (scripts && scripts.length > 0) {
        for (var i = 0; i < scripts.length; i++) {
            var nodeScript = scripts[i];
            var jsPath = nodeScript.hasAttribute ? // non-IE6/7
                         nodeScript.src : // see http://msdn.microsoft.com/en-us/library/ms536429(VS.85).aspx
                         nodeScript.getAttribute("src", 4);
            if (jsPath.indexOf(baseUrl) !== -1) {
                return jsPath.substring(0, jsPath.indexOf(baseUrl));
            }
        }
        return "ERROR:BASEURL_UNKNOWN";
    } else {
        return "";
    }
};
DirView.extend = function (obj) {
    var length = arguments.length;
    if (length < 2 || obj == null) {
        return obj;
    }
    for (var index = 1; index < length; index++) {
        var source = arguments[index];
        if (source) {
            for (var key in source) {
                if (source[key] != null && source[key] !== '') {
                    obj[key] = source[key];
                }
            }
        }
    }
    return obj;
};
DirView.loadCss = function (files) {
    var urls = files && typeof (files) == "string" ? [files] : files;
    for (var i = 0, len = urls.length; i < len; i++) {
        var cssFile = document.createElement("link");
        cssFile.setAttribute('type', 'text/css');
        cssFile.setAttribute('rel', 'stylesheet');
        cssFile.setAttribute('href', urls[i]);
        var h = document.getElementsByTagName("head")[0];
        h.appendChild(cssFile);
    }
};
/**
 * 加载外部的js文件
 * @param sUrl 要加载的js的url地址
 * @fCallback js加载完成之后的处理函数
 */
DirView.loadJs = function(sUrl,callback){
    var _script = document.createElement('script');
    _script.setAttribute('type','text/javascript');
    _script.setAttribute('src',sUrl);
    document.getElementsByTagName('head')[0].appendChild(_script);
    if(this.ie){
        _script.onreadystatechange=function(){
            if(this.readyState=='loaded' || this.readyState=='complete'){
                callback && callback();
            }
        };
    }else if(this.moz){
        _script.onload=function(){
            callback && callback();
        };
    }else{
        callback && callback();
    }
}
DirView.contextPath = DirView.getBaseUrl("/static/dirview.js");


document.body.style.display = "none";
DirView.loadCss(["/static/lib/bootstrap/dist/css/bootstrap.css",
                 "/static/lib/font-awesome/css/font-awesome.css",
                 "/static/lib/imageviewer/dist/viewer.css",
                 "/static/css/style.css"]);
DirView.loadJs("/static/lib/jquery/dist/jquery.min.js",function () {
    DirView.loadJs("/static/lib/bootstrap/dist/js/bootstrap.min.js",function () {
        DirView.loadJs("/static/lib/imageviewer/dist/viewer.js",function () {
            init();
        });
    });
});
var ImageFileType = ["png","jpg","jpeg","gif","bmp"];
var FileTypeClassName = {
    "txt":"fa-file-text darkgray",
    "pdf":"fa-file-pdf-o red",
    "doc":"fa-file-word-o blue",
    "docx":"fa-file-word-o blue",
    "xls":"fa-file-excel-o green",
    "xlsx":"fa-file-excel-o green",
    "ppt":"fa-file-powerpoint-o orange",
    "pptx":"fa-file-powerpoint-o orange",
    "html":"fa-internet-explorer blue",
    "js":"fa-code-o darkgray",
    "rar":"fa-file-archive-o darkred",
    "zip":"fa-file-archive-o darkred"
};
//获取文件后缀
function getType(file){
    var filename=file;
    var index1=filename.lastIndexOf(".");
    var index2=filename.length;
    var type=filename.substring(index1 + 1,index2);
    return type;
}

function parseFiles() {
    var data = [];
    $("ul li").each(function (idx,li) {
        data.push($("a",li).attr("href"));
    })
    console.info("files",data);
    return data;
}

function imgView() {
    var $image = $('.project-folder');

    $image.viewer({
                      // title: function(image){
                      //     return decodeURIComponent(image.alt);
                      // },
                      viewed: function(){
                          // 添加到网址中

                      },url: 'data-imgurl'
                  });
}

function init() {
    var data = parseFiles();

    var html = '<div>\n'
               + '    <ul class="project-folder clearfix">\n';
    $.each(data,function (idx,item) {
        var url = item;
        var name = decodeURIComponent(url);
        if(item == '../'){
            //返回上一层的按钮
            html +=   '        <li title="返回上一级目录" class="item-folder">\n'
                      + '           <a href="' + url + '"><div class="top"><i class="fa fa-angle-double-left silver"></i></div>\n'
                      + '            <span>返回上一级目录</span></a></li>        \n'
        }else if(item.substring(item.length - 1) == '/'){
            // 是个目录
            html +=   '        <li title="'+ name.substring(0,name.length - 1) +'" class="item-folder">\n'
                      + '           <a href="' + url + '"><div class="top"><i class="fa fa-folder"></i></div>\n'
                      + '            <span>'+ name.substring(0,name.length - 1) +'</span></a></li>        \n'
        }else{
            //取得后缀
            var fileType = getType(url).toLowerCase();
            if($.inArray(fileType,ImageFileType) != -1){
                //图片
                html += '<li title="' + name + '" class="item-image">'
                        + '<div class="top"><img src="' + url + '?scale=1" data-imgurl="' + url + '" alt="' + name + '"></div>'
                        + '<span>' + name +  '</span></li>'
            // }else if($.inArray(fileType,DocPreviewType)  != -1){
            }else{
                var className = FileTypeClassName[fileType];
                if(!className){
                    className = "fa-file darkgrey";
                }
                html +=   '        <li title="' + name + '" class="item-folder">\n'
                          + '           <a href="' + url + '" target="_blank"><div class="top"><i class="fa ' + className + '"></i></div>\n'
                          + '            <span> ' + name + ' </span></a></li>        \n'
            }
        }
    })

    html +=  '    </ul>\</div>';
    $("body ul").after(html).remove();

    imgView();

    $("body").show();
}