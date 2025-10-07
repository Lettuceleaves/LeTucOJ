# LetucOJ 使用文档

欢迎使用 LetucOJ，本平台提供在线题库管理、代码编辑与评测功能。

本章节是使用部分，会简单介绍功能。未完成的功能会标记为TODO

## 一、注册与登录

### 注册账号

首页提供注册界面，请记住自己的密码，尤其是管理员账户，管理员拥有增删改查题库的权限

### 登录系统

在首页点击登录，输入账号密码即可

### 授权与取消权限

// TODO

### 注销

// TODO

## 题目列表

题目列表默认根据题目英文名称字典序排序，每页选取前十道题

// TODO 跳转到固定页数（返回消息里还没有总题数）

// TODO 支持关键词搜索，会从 英文名、中文名、标签这三个字段中寻找匹配项（可部分匹配）

// TODO 支持多种排序方式

## 出题页面

拥有管理权限的账号可以在题目列表页面打开出题页面，需要填写以下内容：

### 英文标识
  这道题的唯一标识，建议使用统一的格式命名，只支持ascii(因为要用在url里)。
  
### 题目名称
  便于用户阅读的字段，一般为中文名。

### 测试点数量
  不可修改字段，会在添加案例时自己递增，放在这里只是方便查看信息。

### 难度
  建议在1-100内

### 标签
  标签越多，越能方便查找，基于字符串匹配所以不用在意格式，不过最好统一格式吧
### 作者
  参与了这道题的作者

### 是否公开
  比赛或其他需要时修改

### 是否展示题解
  比赛或其他需要时修改

### 题目描述
  支持MarkDown，下方有及时渲染框，但是并没有做本地保存，所以刷新时会消失！！！（可以考虑后续加上，但目前来看没必要）

### 题解
  这道题的正确答案的完整代码。只支持c语言。

## 答题页面

### 描述
  使用MarkDown模式显示

### 答题

  使用monaco-editor-vue编辑器模块，vscode同款, 没有内置c语言提示词

  注意，代码只会在刷新、切换子页面、输入空格和回车的时候保存（JS里面只在这几个动作里面使用了保存函数）

  退出会返回列表，提交之后自动跳转结果页面

  // TODO 检验

### 题解
  如果题目设置了允许展示题解，这里会出现题解内容
  // TODO 题解页面支持monaco-editor-vue

### 结果
  根据收到的信息类型显示不同的颜色
  我尽可能把错误写清楚了，先排除自己这边的问题，如果确定是服务器的问题再联系管理员根据后面的文档找到服务器端的问题，还是不行再联系我。

# LetucOJ 开发文档

## 整体架构

SpringMVC + Vue3

### 前端
使用vue3开发，具体版本信息见后文，本人不会前端，由ChatGPT生成

### 后端
使用SpringBoot框架 + 三层结构

// TODO 使用DDD（不知道有没有必要，担心过度设计）

目前可运行的有以下模块：
网关模块、练习模块、运行模块、认证模块、建议模块、考核模块

目前使用的其他技术栈：
Minio、Mysql、Redis、Docker

// TODO Docker、Nacos、RocketMQ

## 前端结构

每个.vue结构分成 html、js、css三部分，学前端的应该比我懂

pages文件夹下有多个子文件夹，public放了静态的文件，目前只有首页背景的代码墙txt文件和这个文档的md格式文件

// TODO SpringGateway 挂载vue3的history模式页面 会出现刷新404(详见history模式原理)，我试过自己在后端加配置，但是搞了半天没成功，还是使用的hash模式，但是很丑， Nginx搭配history模式会方便点(网上有教程，我没自己试过)，但是已经有SpringGateway了就不引入了，防止过度设计，期待前端大手子优化。

// TODO 前端异步加载

// 外观优化

## 后端结构

### 网关模块
负责拦截、转发和鉴权，不处理任何业务，具体鉴权方式去看源代码，也不多()

### 练习模块
负责题库的管理和练习代码的提交

### 运行模块
只用来运行，根据用户代码和输入，并发执行代码，输出结果，目前只支持c语言，使用了asan

// TODO 增加运行池模块，优化docker创建这一块的时间，然后使得run运行后立刻销毁，彻底沙盒化

### 认证模块
负责用户的注册、登录、权限管理等功能，使用JWT进行鉴权，用户登录后会返回一个token，后续请求需要携带这个token，里面由role信息，前端可以base64解析出来呈现不同页面，密码已经加密存储

# 版本信息

// TODO 我是懒狗

# 报错信息对照

// TODO 有时间再整理吧()

# 部署&运维手册
mysqldump -u root -p --databases practice --result-file=init8.sql

# 后端接口

## 备注
接口的返回值统一，具体格式如下：

```json
{
  "status": "int", // 状态码，0表示成功，1表示答案错误示超时，2表示编译错误，3表示运行时错误，4表示超时，5表示服务器错误
  "data": "object", // 返回的数据对象
  "error": "string" // 错误信息，如果没有错误则为空字符串
}
```
我标注了部分正确结构的data结构，具体需参考后端源码

错误信息在在error中，正确结果在data中

除了提交代码的接口，其他接口一般只需要处理status == 1 和 status == 5

部分参数需要jwt中的信息，这部分由网关模块处理，但是需要添加字段

部分参数同理，不需要的字段可以设置为0或者null之类的，但是必须要有，不然后端无法解析，例如

9001端口为minio端口，可以直接访问minio的UI界面

## 模块： gateway

### 路径：letucoj.cn

系统统一入口，所有请求都会经过此模块。

## 模块： practice

### 路径：/practice/list
权限：USER
方法名：getList
参数：

```json
{
  "start": "int",
  "limit": "int",
  "order": "string",
  "like": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "name": "string",
      "cnname": "string"
    }
  ],
  "error": "string"
}
```

备注：获取题目列表，用户权限不会返回隐藏题目。

### 路径：/practice/searchList
权限：USER
方法名：searchList
参数：

```json
{
  "start": "int",
  "limit": "int",
  "order": "string",
  "like": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "name": "string",
      "cnname": "string"
    }
  ],
  "error": "string"
}
```

备注：搜索题目列表，用户权限不会返回隐藏题目。

### 路径：/practice/full/get
权限：USER
方法名：getProblem
参数：

```json
{
  "name": "string",
  "cnname": "string",
  "limit": "long",
  "data": "object"
}
```

返回值：

```json
{
  "status": "int",
  "data": {
    "name": "string",
    "cnname": "string",
    "caseAmount": "int",
    "difficulty": "int",
    "tags": "string",
    "authors": "string",
    "createtime": "Date",
    "updateat": "Date",
    "content": "string",
    "freq": "float",
    "ispublic": "boolean",
    "solution": "string",
    "showsolution": "boolean"
  },
  "error": "string"
}
```

备注：获取题目详细信息。

### 路径：/practice/fullRoot/insert
权限：MANAGER
方法名：insertProblem
参数：

```json
{
  "dto": {
    "name": "string",
    "cnname": "string",
    "caseAmount": "int",
    "difficulty": "int",
    "tags": "string",
    "authors": "string",
    "createtime": "Date",
    "updateat": "Date",
    "content": "string",
    "freq": "float",
    "ispublic": "boolean",
    "solution": "string",
    "showsolution": "boolean"
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：新增题目。

### 路径：/practice/fullRoot/update
权限：MANAGER
方法名：updateProblem
参数：

```json
{
  "dto": {
    "name": "string",
    "cnname": "string",
    "caseAmount": "int",
    "difficulty": "int",
    "tags": "string",
    "authors": "string",
    "createtime": "Date",
    "updateat": "Date",
    "content": "string",
    "freq": "float",
    "ispublic": "boolean",
    "solution": "string",
    "showsolution": "boolean"
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：更新题目信息。

### 路径：/practice/fullRoot/delete
权限：MANAGER
方法名：deleteProblem
参数：

```json
{
    "name": "string",
    "cnname": "string",
    "caseAmount": "int",
    "difficulty": "int",
    "tags": "string",
    "authors": "string",
    "createtime": "Date",
    "updateat": "Date",
    "content": "string",
    "freq": "float",
    "ispublic": "boolean",
    "solution": "string",
    "showsolution": "boolean"
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：删除题目。

### 路径：/practice/getCase
权限：MANAGER
方法名：getCase
参数：

```json
{
  "name": "string",
  "code": "string",
  "input": "string" 
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：获取测试用例, 返回值格式同run模块submit。

### 路径：/practice/submitCase
权限：MANAGER
方法名：submitCase
参数：

```json
{
  "name": "string",
  "input": "string",
  "output": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：提交测试用例。

### 路径：/practice/submit
权限：USER
方法名：submit
参数：

```json
{
  "message": {
    "service": "byte",
    "code": "string",
    "problemName": "string"
  },
  "name": "string", // param
  "cnname": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：提交代码进行评测。

## 模块： run

### 路径：/run/runFeign
权限：INTERNAL
方法名：runFeign
参数：

```json
[
  "string"
]
```

返回值：

```json
{
  "status": "byte",
  "data": "object",
  "error": "string"
}
```

备注：内部服务调用，执行代码运行。状态码说明：0-通过，1-答案错误，2-编译错误，3-运行时错误，4-超时，5-服务器错误。

## 模块： advice

### 路径：/advice
权限：USER
方法名：advice
参数：

```json
{
  "userFile": "string" // param
}
```

返回值：

```json
Flux<ServerSentEvent<String>>
```

备注：基于用户代码提供AI建议，返回Server-Sent Events流式响应。

## 模块： user

### 路径：/user/register
权限：PUBLIC
方法名：register
参数：

```json
{
  "username": "string",
  "cnname": "string", 
  "password": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": null,
  "error": "string"
}
```

备注：用户注册接口, 返回JWT。

### 路径：/user/login
权限：PUBLIC
方法名：login
参数：

```json
{
  "username": "string",
  "cnname": "string",
  "password": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": {
    "token": "string"
  },
  "error": "string"
}
```

备注：用户登录接口,返回JWT。

### 路径：/user/activate
权限：MANAGER
方法名：activate
参数：

```json
{
  "username": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：激活用户账户。

### 路径：/user/deactivate
权限：USER
方法名：deactivate
参数：

```json
{
  "username": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：注销账户。

### 路径：/user/logout
权限：USER
方法名：logout
参数：

```json
{
  "Authorization": "string", // header
  "ttl": "long" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：用户注销登录。

### 路径：/user/changePassword
权限：USER
方法名：changePassword
参数：

```json
{
  "username": "string", // param
  "oldPassword": "string", // param
  "newPassword": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：修改用户密码。

### 路径：/user/users
权限：MANAGER
方法名：listUsers
参数：无

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "userName": "string",
      "cnname": "string",
      "password": "string",
      "role": "string",
      "enabled": "boolean"
    }
  ],
  "error": "string"
}
```

备注：获取所有普通用户列表。

### 路径：/user/managers
权限：MANAGER
方法名：listManagers
参数：无

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "userName": "string",
      "cnname": "string",
      "password": "string",
      "role": "string",
      "enabled": "boolean"
    }
  ],
  "error": "string"
}
```

备注：获取所有管理员列表。

### 路径：/user/promote
权限：MANAGER
方法名：promote
参数：

```json
{
  "username": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：将用户提升为管理员。

### 路径：/user/demote
权限：MANAGER
方法名：demote
参数：

```json
{
  "username": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：将管理员降级为普通用户。

## 模块： contest

### 路径：/contest/full/getProblem
权限：USER
方法名：getProblem
参数：

```json
{
  "dto": {
    "name": "string",
    "cnname": "string",
    "limit": "long",
    "data": "object"
  },
  "contestName": "string" // header
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：获取指定竞赛的单个问题详细信息。

### 路径：/contest/full/getContest
权限：USER
方法名：getContest
参数：

```json
{
  "dto": {
    "name": "string",
    "data": "object"
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：获取指定竞赛的详细信息。

### 路径：/contest/insertContest
权限：MANAGER
方法名：insertContest
参数：

```json
{
  "dto": {
    "name": "string",
    "data": {
      "name": "string",
      "cnname": "string",
      "mode": "string",
      "start": "Timestamp",
      "end": "Timestamp",
      "note": "string",
      "ispublic": "boolean"
    }
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：用于新增竞赛记录。

### 路径：/contest/updateContest
权限：MANAGER
方法名：updateContest
参数：

```json
{
  "dto": {
    "name": "string",
    "data": {
      "name": "string",
      "cnname": "string",
      "mode": "string",
      "start": "Timestamp",
      "end": "Timestamp",
      "note": "string",
      "ispublic": "boolean"
    }
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：更新指定竞赛记录。

### 路径：/contest/insertProblem
权限：MANAGER
方法名：insertProblem
参数：

```json
{
  "dto": {
    "contestName": "string",
    "problemName": "string",
    "score": "int"
  }
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```
备注：向指定竞赛新增问题。

### 路径：/contest/deleteProblem
权限：MANAGER
方法名：deleteProblem
参数：

```json
{
  "dto": {
    "contestName": "string",
    "problemName": "string",
    "score": "int"
  }
}
```
返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```
备注：从指定竞赛删除问题。

### 路径：/contest/attend
权限：USER
方法名：attendContest
参数：

```json
{
  "cnname": "string",
  "contestName": "string" // header
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```
备注：用户参加指定竞赛。

### 路径：/contest/list/problem
权限：USER
方法名：getProblemList
参数：

```json
{
  "contestName": "string" // header
}
```

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "contestName": "string",
      "problemName": "string",
      "score": "int"
    }
  ],
  "error": "string"
}
```

备注：获取指定竞赛的问题列表。

### 路径：/contest/list/contest
权限：USER
方法名：getContestList
参数：无
返回值：

```json
{
  "status": "int",
  "data": [
    {
      "name": "string",
      "cnname": "string",
      "mode": "string",
      "start": "Timestamp",
      "end": "Timestamp",
      "note": "string",
      "ispublic": "boolean"
    }
  ],
  "error": "string"
}
```

备注：获取所有竞赛列表。

### 路径：/contest/list/board
权限：USER
方法名：getBoardList
参数：

```json
{
  "name": "string"
}
```

返回值：

```json
{
  "status": "int",
  "data": [
    {
      "contestName": "string",
      "userName": "string",
      "score": "int",
      "times": "int"
    }
  ],
  "error": "string"
}
```

备注：获取指定名称的排行榜数据。


### 路径：/contest/submit
权限：USER
方法名：submit
参数：

```json
{
  "message": {
    "service": "byte",
    "code": "string",
    "problemName": "string",
    "contestName": "string"
  },
  "name": "string", // param
  "cnname": "string" // param
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```
备注：提交代码进行评测。

## 模块： sys

### 路径：/sys/doc/get
权限：PUBLIC
方法名：getDoc
参数：无

返回值：

```json
{
  "status": "int",
  "data": "string", 
  "error": "string"
}
```

备注：data是文档内容。

### 路径：/sys/doc/update
权限：ROOT
方法名：updateDoc
参数：

```json
{
  "doc": "byte[]"
}
```

返回值：

```json
{
  "status": "int",
  "data": "object",
  "error": "string"
}
```

备注：更新文档内容。