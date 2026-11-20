# board-app

eGovFrame(전자정부 표준프레임워크) Boot 기반 게시판입니다. 첨부파일 기능이 있고, 내장 HSQL DB로 동작합니다.

## 실행

```bash
# JAVA_HOME 이 JDK 17 을 가리켜야 합니다
mvn spring-boot:run
# 또는
mvn package && java -jar target/board-app-1.0.0.jar
```

브라우저에서 http://localhost:8080 에 접속하면 게시판 목록으로 이동합니다.

## 기능

- 게시물 목록: 제목·내용·작성자 검색, 페이징(eGov `PaginationInfo`)
- 게시물 상세, 등록, 수정, 삭제 (입력값 검증 포함)
- 첨부파일
  - 등록·수정 시 여러 파일 업로드 (파일당 10MB)
  - 다운로드 (한글 파일명 지원)
  - 수정 화면에서 기존 파일 선택 삭제
  - 게시물을 삭제하면 첨부파일도 삭제
- 게시물 ID 생성: eGov `EgovTableIdGnrServiceImpl` (`IDS` 테이블, `BOARD_00000000000001` 형식)

## 구성

| 항목 | 내용 |
|---|---|
| Java | 17 |
| 프레임워크 | eGovFrame 실행환경 4.3.0 (`ptl.mvc`, `fdl.cmmn`, `fdl.idgnr`, `psl.dataaccess`), Spring Boot 2.7.18 |
| 데이터 접근 | MyBatis (`src/main/resources/mapper/BoardMapper.xml`) |
| DB | HSQLDB 메모리 모드. 기동할 때마다 `schema.sql` 로 테이블을 새로 만듭니다 |
| 화면 | Thymeleaf |
| 첨부파일 저장 위치 | `./upload-files` (`board.upload-dir` 속성으로 변경) |

### 테이블

- `BOARD`: 과제 DDL에 `REG_DT`(등록일시) 컬럼을 추가했습니다.
- `BOARD_FILE`: 첨부파일 메타데이터. 실제 파일은 UUID 이름으로 디스크에 저장합니다.
- `IDS`: eGov ID 생성기가 사용하는 테이블입니다.

## eGovFrame 버전 관련

과제는 eGovFrame **5.0** 을 요구합니다. 하지만 작업 환경에서 eGov Maven 저장소(`https://maven.egovframe.go.kr/maven/`)에 `org.egovframe.rte:*:5.0.0` 이 없어서(404) 내려받을 수 없었습니다. 그래서 받을 수 있는 최신 버전인 **4.3.0 + Spring Boot 2.7** 로 구성했습니다.

5.0 으로 전환하려면 다음을 바꿉니다.

1. `pom.xml`
   - `egovframe.version` 을 5.0 버전으로 변경
   - Spring Boot parent 를 3.x 로 변경
   - `mybatis-spring-boot-starter` 를 3.x 로 변경
2. Java 소스의 `javax.annotation.*`, `javax.validation.*` import 를 `jakarta.*` 로 변경

## 테스트

```bash
mvn test
```

`BoardApplicationTests` 는 다음 흐름을 MockMvc 로 검증합니다.

등록(첨부 포함) → 목록 검색 → 상세 → 다운로드 → 수정(파일 삭제·추가) → 검증 오류 → 삭제
