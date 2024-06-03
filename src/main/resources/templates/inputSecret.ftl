<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>请输入你的校验信息！</title>
</head>
<body>
<p><font color="red">${errorTips!}</font></p>
<p>${secretTips!}</p>
<form action="action/verifySecret" method="post">
    <input type="hidden" name="tinyurl" value="${tinyurl!}"/>
    <input type="text" maxlength="30" name="secretData"/>
    <input type="submit" value="提交" ${submitSate!}/>
</form>
</body>
</html>
