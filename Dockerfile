# ============================================
# VueBlog 整体项目 Dockerfile
# 包含前端 Vue 和后端 Spring Boot
# ============================================

# --------------------------------------------
# 阶段 1: 构建前端 Vue 项目
# --------------------------------------------
FROM node:14-alpine AS frontend-builder

WORKDIR /app/frontend

# 复制前端 package.json 并安装依赖
COPY vueblog-vue/package*.json ./
RUN npm install

# 复制前端源代码并构建
COPY vueblog-vue/ ./
RUN npm run build

# --------------------------------------------
# 阶段 2: 构建后端 Spring Boot 项目
# --------------------------------------------
FROM maven:3.8-openjdk-8 AS backend-builder

WORKDIR /app/backend

# 复制 pom.xml 并下载依赖
COPY vueblog-java/pom.xml ./
RUN mvn dependency:go-offline -B

# 复制后端源代码并构建
COPY vueblog-java/src ./src
RUN mvn clean package -DskipTests

# --------------------------------------------
# 阶段 3: 最终运行镜像
# --------------------------------------------
FROM openjdk:8-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl

WORKDIR /app

# 从后端构建阶段复制 jar 包
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# 从前端构建阶段复制构建好的静态文件
COPY --from=frontend-builder /app/frontend/dist ./static

# 暴露后端端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=pro"]
