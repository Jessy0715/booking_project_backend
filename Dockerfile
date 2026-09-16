# ── 第一階段：建置 ──────────────────────────────────────────
# 用完就丟，JDK、Maven、原始碼都不會進最終映像檔
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# 先只複製依賴描述檔，讓 Docker 快取這一層。
# 之後只改 Java 程式碼時，不用重新下載所有依賴（省好幾分鐘）
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

# 依賴都在了，才複製原始碼並打包
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B -q

# ── 第二階段：執行 ──────────────────────────────────────────
# 只裝 JRE 不裝 JDK，映像檔小一半以上
FROM eclipse-temurin:25-jre
WORKDIR /app

# 不用 root 跑：萬一應用程式被打穿，攻擊者拿到的也只是普通使用者。
# 先把 /app 的擁有權交給 spring，否則它建不了 uploads/（本機儲存模式會用到）
RUN useradd --system --uid 1001 spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app
USER spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Railway 會透過 PORT 環境變數告訴容器該聽哪個 port，application.properties 已改成讀它
EXPOSE 8088

# 容器裡的 JVM 預設會依容器的記憶體上限自動調整 heap，不用手動設 -Xmx
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
