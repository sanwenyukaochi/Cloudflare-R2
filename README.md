# Cloudflare R2 Storage Project

![Project Banner](<替换为你的项目banner图片URL>)

## 项目简介

本项目是一个基于 Spring Boot 的云存储服务，集成 Cloudflare R2（兼容 S3 协议），支持对象存储的常用操作。项目同时集成了 OpenAPI (Swagger) 文档，便于接口调试与开发。

- **技术栈**：Java 25、Spring Boot 3.5.x、Cloudflare R2 (S3 SDK)、Springdoc OpenAPI、Lombok
- **用途**：为应用提供高性能、低成本的对象存储服务，支持文件上传、下载、管理等功能。

---

## 快速开始

### 环境要求
- JDK 25
- Gradle
- Cloudflare R2 账号与存储桶

### 安装与运行
```bash
# 克隆项目
$ git clone <你的仓库地址>
$ cd Cloudflare-R2

# 构建项目
$ ./gradlew build

# 运行项目
$ ./gradlew bootRun
```

### 配置 Cloudflare R2
请在 `src/main/resources/application.properties` 中配置以下内容：
```properties
cloudflare.r2.accessKey=<你的AccessKey>
cloudflare.r2.secretKey=<你的SecretKey>
cloudflare.r2.endpoint=<R2 Endpoint>
cloudflare.r2.bucket=<Bucket名称>
```

---

## 主要功能

- 存储桶管理（创建、删除、列举）
- 文件上传、下载、删除
- 文件元数据查询
- OpenAPI (Swagger) 在线接口文档

### API 文档
访问 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 查看所有接口。

![Swagger UI 示例](<替换为Swagger UI截图URL>)

---

## 项目结构
```
Cloudflare-R2/
├── src/main/java/com/cloudflare/storage/
│   ├── S3Application.java           # 启动类
│   ├── config/                      # 配置类
│   ├── controller/                  # 控制器层，API接口
│   ├── requests/                    # 请求参数封装
│   ├── service/                     # 业务逻辑层
├── src/main/resources/
│   └── application.properties       # 配置文件
├── build.gradle                     # 构建脚本
```

---

## 测试方法

```bash
# 运行所有测试
$ ./gradlew test
```

测试用例位于 `src/test/java/com/cloudflare/storage/`。

---

## 常见问题

- **连接失败**：请检查 R2 endpoint、AccessKey、SecretKey 是否正确。
- **依赖问题**：请确保使用 JDK 25，且已正确安装 Gradle。
- **接口404**：确认服务已启动，且访问端口为 8080。

---

## 贡献指南

欢迎提交 Issue 或 Pull Request！请遵循以下流程：
1. Fork 仓库
2. 新建分支
3. 提交修改
4. 创建 Pull Request

---

## License

本项目采用 MIT License，详见 [LICENSE](LICENSE)。

---

## 联系方式

- 作者：<你的名字或团队>
- 邮箱：<你的邮箱>
- Issues：<你的GitHub Issue链接>

---

## 架构图

![架构图](<替换为架构图图片URL>)

---

## 示例接口调用

```bash
# 上传文件示例
curl -X POST "http://localhost:8080/api/bucket/upload" \
     -F "file=@/path/to/your/file.jpg" \
     -F "bucketName=<Bucket名称>"
```

更多接口请参考 Swagger UI。

---

> 如需定制化开发或技术支持，请联系作者。

