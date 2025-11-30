# Freerouting CLI 실행 매뉴얼

## 준비물
- JDK 17 설치 (PATH에 `java` / `javac` 확인)
- PowerShell 기준 명령 예시

## 소스에서 바로 실행 (Gradle run)
```powershell
cd D:\git\freerouting
$env:FREEROUTING__GUI__ENABLED="false"        # GUI 끄기
$env:FREEROUTING__API_SERVER__ENABLED="false" # API 서버 끄기
.\gradlew.bat run --args="-de tests/Issue555-BBD_Mars-64.dsn -do build/out/Issue555.ses -dl"
```
- `-de <dsn>` : 입력 Specctra DSN 파일
- `-do <ses>` : 출력 Specctra SES 파일
- `-dl`       : 파일 로깅 끄기 (필요 없으면 제거)
- 로그 레벨 설정 시 `-ll INFO` 등 추가

## 실행용 fat JAR 빌드 후 실행
```powershell
cd D:\git\freerouting
.\gradlew.bat executableJar

$env:FREEROUTING__GUI__ENABLED="false"
$env:FREEROUTING__API_SERVER__ENABLED="false"
java -jar build\libs\freerouting-executable.jar -de tests\Issue555-BBD_Mars-64.dsn -do build\out\Issue555.ses -dl
```

## 옵션 요약
- `--user_data_path=<dir>` : 설정·로그 저장 위치 지정
- `-ea` / `--enable-analytics` : 텔레메트리 opt-in (기본 off)
- `-da` : 텔레메트리 강제 off
- `-inc <netclass1,netclass2>` : 지정 넷클래스 무시
- `-help` : CLI 도움말 출력 후 종료

## 출력물
- 라우팅 결과 SES: 지정한 `-do` 경로
- 로그/설정: 기본 `%TEMP%\freerouting` 또는 `--user_data_path` 지정 경로
