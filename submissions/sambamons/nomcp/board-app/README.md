# board-app

전자정부 표준프레임워크(eGovFrame) 5.0 Boot 기반 게시판입니다.

- Java 17, Maven, `egovframe-boot-starter-parent` 5.0.0 (Spring Boot 3.5.6)
- 내장 HSQL DB(메모리 모드) — 기동할 때 `db/schema.sql`, `db/data.sql` 로 초기화
- MyBatis(`@EgovMapper`), eGovFrame ID 생성 서비스(`EgovTableIdGnrServiceImpl`), `PaginationInfo`
- Thymeleaf 화면, 게시물 첨부파일(여러 개 업로드·다운로드·삭제)

## 실행

```bash
mvn spring-boot:run
# 또는
mvn package && java -jar target/board-app-1.0.0.jar
```

http://localhost:8080 접속 → 게시물 목록

## 기능

| URL | 설명 |
|---|---|
| `GET /board/list.do` | 목록 (제목/내용/작성자 검색, 페이징) |
| `GET /board/{id}/detail.do` | 상세 + 첨부파일 목록 |
| `GET·POST /board/add.do` | 등록 (첨부파일 여러 개) |
| `GET·POST /board/{id}/edit.do` | 수정 (기존 첨부 삭제 / 새 첨부 추가) |
| `POST /board/{id}/delete.do` | 삭제 (첨부파일 포함) |
| `GET /board/file/{fileId}/download.do` | 첨부파일 다운로드 |

## 테이블

과제 DDL 의 `BOARD` 에 `REG_DT`, `UPD_DT` 를 추가했고, 다음 테이블을 더했습니다.

- `BOARD_FILE` — 첨부파일 메타정보 (원본 파일명, 저장 파일명, 크기, 형식)
- `IDS` — ID 생성 서비스용. `BOARD_00000000000001`, `FILE_000000000000001` 형식(20자)

## 첨부파일

- 저장 위치: `board.upload-dir` (기본 `./upload`)
- 저장 파일명은 UUID 로 만들고 원본 파일명은 DB 에만 둡니다.
- 크기 제한: 파일당 10MB, 요청당 50MB (`spring.servlet.multipart.*`)
- 게시물/첨부 삭제 시 실제 파일은 트랜잭션 커밋 후 지우고, 등록이 롤백되면 저장한 파일도 지웁니다.

## 테스트

```bash
mvn test
```

등록(첨부) → 다운로드 → 수정(첨부 교체) → 삭제 흐름과 입력값 검증을 확인합니다.
