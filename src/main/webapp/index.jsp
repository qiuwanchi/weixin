<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>首页</title>
<script type="text/javascript" src="<%=basePath%>/resources/js/jquery.min.js"></script>
</head>
<script type="text/javascript">
function show(){
	$("#t").toggle();
}
</script>
<body>
大家好，我是<a onclick="show();" href="javascript:void(0);">邱哥</a>。
<div id="t" style="display: none;">
<img alt="邱哥" src="<%=basePath%>/resources/img/one.jpg">
<img alt="邱哥" src="<%=basePath%>/resources/img/two.jpg">
</div>
</body>
</html>