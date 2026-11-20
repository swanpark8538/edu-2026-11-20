# eGovFrame 5.0 게시판

전자정부 표준프레임워크 5.0.0 서비스 계층과 Spring Boot 3.5.6으로 구현한 게시판입니다. Java 17과 Maven을 사용합니다.

## 실행

```powershell
cd board-app
mvn package
java -jar target/board-app-1.0.0.jar
```

이 실습 환경에서 Maven이 PATH에 없다면 아래 명령으로 빌드할 수 있습니다.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot'
& 'C:\Users\Danny\AppData\Local\Programs\apache-maven-3.9.9\bin\mvn.cmd' package
```

브라우저에서 [http://127.0.0.1:8080/boards](http://127.0.0.1:8080/boards)를 엽니다.

## 사용 방법

- 게시판에서 글을 등록하고, 목록에서 글을 열어 수정하거나 삭제할 수 있습니다.
- 글에 파일을 1개 첨부할 수 있습니다. 최대 크기는 10MB이며, 상세 화면에서 원래 파일명으로 다운로드할 수 있습니다.
- 글을 수정할 때 기존 파일을 유지하거나, 새 파일로 교체하거나, 삭제할 수 있습니다.
- 내장 HSQLDB는 시작 시 테이블을 준비합니다. 게시글과 첨부파일은 앱을 실행한 작업 디렉터리의 `data/`에 함께 저장됩니다. 재시작할 때 같은 작업 디렉터리에서 실행하면 기존 데이터가 유지됩니다.
