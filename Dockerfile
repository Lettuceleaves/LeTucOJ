# syntax=docker/dockerfile:1
FROM ubuntu:22.04 AS base

# ================= 1. 基础环境 (工具链) =================
# 这一层对 Dev 和 Builder 都是公用的
ENV JAVA_VERSION=jdk-17.0.9
ENV GRAALVM_PKG=graalvm-community-jdk-17.0.9_linux-x64_bin.tar.gz
ENV GRAALVM_URL=https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-17.0.9/${GRAALVM_PKG}
ENV GRAALVM_HOME=/opt/graalvm
ENV MAVEN_VERSION=3.9.6
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="$GRAALVM_HOME/bin:$MAVEN_HOME/bin:$PATH"

# 安装系统依赖 (保留 git 等开发工具)
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget tar ca-certificates build-essential zlib1g-dev libz-dev git openssh-client \
    && rm -rf /var/lib/apt/lists/*

# 安装 GraalVM 和 Maven
RUN mkdir -p ${GRAALVM_HOME} \
    && wget -qO- ${GRAALVM_URL} | tar xz -C ${GRAALVM_HOME} --strip-components=1 \
    && mkdir -p ${MAVEN_HOME} \
    && wget -qO- https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xz -C ${MAVEN_HOME} --strip-components=1

# 【强烈建议】配置国内 Maven 镜像（解决你之前的 SSL 报错）
# 如果不需要阿里云镜像，可以删掉这一段
RUN echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
    <mirrors> \
        <mirror> \
            <id>aliyunmaven</id> \
            <mirrorOf>*</mirrorOf> \
            <name>阿里云公共仓库</name> \
            <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
    </mirrors> \
</settings>' > ${MAVEN_HOME}/conf/settings.xml

WORKDIR /app

# ============================================================
#  阶段 A: 开发环境 (Dev Container 专用)
# ============================================================
FROM base AS dev
# 【关键修改】：这里什么都不做，或者只安装调试工具 (vim/curl)
# 绝对不要 COPY . .
# 绝对不要 RUN mvn package
# 代码会由 devcontainer.json 通过 Volume 挂载进来
RUN echo "Dev environment ready."

# ============================================================
#  阶段 B: 生产构建 (CI/CD 专用)
# ============================================================
FROM base AS builder
# 这里才开始真正把代码放进镜像
COPY pom.xml .
COPY . .

# 这里才执行编译
# 这里的编译失败不会影响上面的 dev 阶段
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -T 1C

# 导出 jar 包示例
RUN mkdir /export && \
    find . -type f -path "*/target/*.jar" ! -name "*-sources.jar" ! -name "original-*.jar" -exec cp -v {} /export/ \;

CMD ["ls", "-l", "/export"]