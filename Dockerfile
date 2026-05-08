FROM amazoncorretto:17-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너로 복사
# GitHub Actions에서 빌드된 jar 파일명을 매칭하기 위해 와일드카드 사용
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul

# 실행 권한 부여 및 애플리케이션 실행
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", "--spring.profiles.active=prod"]
