# 게시판

Java 17과 Maven으로 실행합니다.

```powershell
mvn package
java -jar target/board-app-1.0.0.jar
```

브라우저에서 http://127.0.0.1:8080/ 을 열면 글을 작성, 조회, 수정, 삭제할 수 있습니다.
게시글마다 최대 5 MB의 첨부파일 한 개를 올릴 수 있습니다. 수정 화면에서 새 파일을 선택하면 기존 파일이 교체됩니다.
게시글과 첨부파일은 실행 디렉터리의 `data/`에 내장 HSQLDB로 저장됩니다.
