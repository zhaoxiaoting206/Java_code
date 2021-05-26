<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录</title>
</head>
<body>
<h1>登录</h1>
<div style="text-align: center">
    <%--这里表单表示的意思是：以post方式提交表单,提交到login请求--%>
    <form action="${pageContext.request.contextPath}/login" method="get">
        用户名： <input type="text" name="username" > <br>
        密码：   <input type="password" name="password" > <br>
        爱好：
        <input type="checkbox" name="hobbys" value="男孩"> 男孩
        <input type="checkbox" name="hobbys" value="代码"> 代码
        <input type="checkbox" name="hobbys" value="唱歌"> 唱歌
        <input type="checkbox" name="hobbys" value="电影"> 电影



        <br>
        <input type="submit">
    </form>
</div>

</body>
</html>
