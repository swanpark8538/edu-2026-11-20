> 이 폴더에서는 MCP 없이 AI 도구의 기본 기능만 사용합니다.

# 과제: 첨부파일을 올릴 수 있는 게시판 (board-app)

이 폴더 안에 **전자정부 표준프레임워크(eGovFrame) 5.0 Boot 기반 게시판 프로젝트**를 새로 만듭니다.
프로젝트 폴더 이름은 `board-app` 이어야 합니다 (이 폴더 바로 아래에 `board-app/pom.xml` 이 생기도록 합니다).

## 1. 업무 요구

게시판 하나를 운영합니다. 사용자는 글을 목록에서 찾아 보고, 새 글을 쓰고, 고치고, 지울 수 있습니다.

1. 게시물 CRUD 기능
2. 게시판 목록 화면
3. 게시물 검색 기능
4. 게시물에 첨부파일 기능
5. 화면은 정부 디자인 시스템(KRDS) 적용

## 2. 기술 조건

| 항목 | 조건 |
|---|---|
| 프레임워크 | eGovFrame **5.0 Boot** (`org.egovframe.boot:egovframe-boot-starter-parent:5.0.0`), Java 17, Maven 을 사용합니다. `mvn -DskipTests package` 로 실행 가능한 jar 가 만들어져야 합니다. |
| 화면 | Thymeleaf 와 KRDS 를 사용합니다. |
| DB | **내장 HSQL(메모리)** 을 사용합니다. 별도 DB 설치 없이 기동되어야 하고, 필요한 테이블은 기동 시 자동으로 만들어져야 합니다. |
| 보안 | 공공 웹 보안 점검 기준을 충족해야 합니다. |
| 실행 | `java -jar <jar 경로> --server.port=<포트> --Globals.fileStorePath=<첨부파일 업로드 폴더 경로>` 로 기동되어야 합니다. 첨부파일 저장 경로는 `Globals.fileStorePath` 프로퍼티에서 읽습니다. |
| 표준 준수 | **표준프레임워크 아키텍처**를 따르고, 실행환경이 제공하는 공통 기반 서비스를 활용해야 합니다. |

## 3. 테이블

```sql
CREATE TABLE BOARD (
  BOARD_ID VARCHAR(20) NOT NULL,
  TITLE    VARCHAR(200) NOT NULL,
  CONTENT  VARCHAR(4000),
  WRITER   VARCHAR(50) NOT NULL,
  PRIMARY KEY (BOARD_ID)
);
```

첨부파일 연결, 번호 부여 등에 필요한 컬럼이나 테이블은 자유롭게 추가해도 됩니다.

## 4. 화면 구성

화면 주소와 그 밖의 요청 파라미터 이름은 자유롭게 설계합니다. 다만 검색 조건과 검색어 파라미터 이름은 `searchCondition`, `searchKeyword` 로 합니다. 화면은 다음과 같이 구성해야 합니다.

- **목록**: 게시물을 10건 단위로 나누어 보여 주는 페이징을 제공합니다. 검색 조건(`searchCondition`)은 0 제목, 1 내용, 2 작성자 중에서 고르고, 검색어(`searchKeyword`)를 입력해 검색합니다.
- **등록**: 제목, 내용, 작성자를 입력하고 파일을 여러 개 첨부할 수 있습니다.
- **수정**: 조회 화면을 겸하며 기존 내용을 고칩니다. 첨부 목록과 첨부별 내려받기 링크를 보여 주고, 첨부를 추가해도 기존 첨부 묶음이 유지되어야 합니다. 삭제 버튼도 제공합니다.
