# Cloudflare R2 Storage Project

<p align="center">
    <img src="assets/Cloudflare_R2.svg" width="600" alt="Cloudflare R2 Logo"/>
</p>

<p align="center">
  <a href="https://github.com/sanwenyukaochi/Cloudflare-R2/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/sanwenyukaochi/Cloudflare-R2/ci.yml?branch=main" alt="Build Status">
  </a>
  <a href="https://github.com/sanwenyukaochi/Cloudflare-R2/releases">
    <img src="https://img.shields.io/github/v/release/sanwenyukaochi/Cloudflare-R2" alt="Release">
  </a>
  <a href="https://github.com/sanwenyukaochi/Cloudflare-R2/issues">
    <img src="https://img.shields.io/github/issues/sanwenyukaochi/Cloudflare-R2" alt="Issues">
  </a>
  <a href="https://github.com/sanwenyukaochi/Cloudflare-R2/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/sanwenyukaochi/Cloudflare-R2" alt="License">
  </a>
</p>

---

## 🚀 项目简介

Cloudflare R2 Storage Project 是一个基于 **Spring Boot** 的云存储服务，集成 **Cloudflare R2**（兼容 S3 协议），支持对象存储的常用操作。项目同时集成了 **OpenAPI (Swagger)**，便于接口调试与开发。

* **技术栈**：Java 25 | Spring Boot 3.5.x | Cloudflare R2 (S3 SDK) | Springdoc OpenAPI | Lombok
* **用途**：提供高性能、低成本的对象存储服务，支持文件上传、下载与管理。

---

## ⚡ 快速开始

### 环境要求

* **JDK 25**
* **Gradle**
* **Cloudflare R2 账号与存储桶**

### 安装与运行

```bash
# 克隆项目
git clone https://github.com/sanwenyukaochi/Cloudflare-R2.git
cd Cloudflare-R2

# 构建项目
./gradlew build

# 启动项目
./gradlew bootRun
```

> 💡 Tip：你也可以使用 Gradle Wrapper `./gradlew` 来保证与项目一致的 Gradle 版本。

### 配置 Cloudflare R2

在 `src/main/resources/application.properties` 中配置：

```properties
cloudflare.account-id=<你的AccountId>
cloudflare.r2.accessKey=<你的AccessKey>
cloudflare.r2.secretKey=<你的SecretKey>
```

---

## ✨ 主要功能

* 存储桶管理：创建、删除、列举
* 文件操作：上传、下载、删除
* 文件元数据查询
* **OpenAPI (Swagger) 在线接口文档**

### API 文档

访问 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 查看所有接口。

---

## 📁 项目结构

```
Cloudflare-R2/
├── src/main/java/com/cloudflare/storage/
│   ├── S3Application.java       # 启动类
│   ├── config/                  # 配置类
│   ├── controller/              # 控制器层，API接口
│   ├── requests/                # 请求参数封装
│   ├── service/                 # 业务逻辑层
├── src/main/resources/
│   └── application.properties   # 配置文件
├── build.gradle                 # 构建脚本
```

---

## 💡 使用建议

* Fork 后请根据自身环境修改 R2 配置
* 可结合 CI/CD 自动化部署
* 对大文件上传可使用分片上传策略

---

## 📞 联系与支持

如需定制化开发或技术支持，请联系作者：

* GitHub: [sanwenyukaochi](https://github.com/sanwenyukaochi)
* 邮箱: `sanwenyukaochi@outlook.com`
