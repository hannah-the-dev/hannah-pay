# ====================================
# Stage 1: 빌드 스테이지
# ====================================
FROM gradle:jdk23 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 로케일 설정 (UTF-8)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Gradle 설정과 소스만 복사
COPY build.gradle .
COPY settings.gradle .
COPY src/ /app/src/

# 빌드 수행 (테스트 제외)
RUN gradle bootJar -x test --no-daemon

# ====================================
# Stage 2: 실행 스테이지 (경량화)
# ====================================
FROM eclipse-temurin:23-jre-noble

# 작업 디렉토리 설정
WORKDIR /app

# 비루트 사용자 생성 (보안 강화)
RUN groupadd -r spring && useradd -r -g spring spring

# 빌드된 JAR 파일 복사 (Stage 1에서)
COPY --from=builder /app/build/libs/*.jar app.jar

# uploads 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# 사용자 전환 (root → spring)
USER spring:spring

# JVM 메모리 설정 (AWS EC2 t3.micro 기준)
ENV JAVA_OPTS="-Xms512m -Xmx750m -XX:MaxMetaspaceSize=180m \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -Djava.security.egd=file:/dev/./urandom"

# 애플리케이션 포트 노출
EXPOSE 8080

# 컨테이너 시작 시 실행할 명령
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
