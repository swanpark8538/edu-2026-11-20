# 과제: eGovFrame 5.0 Boot 게시판

**전자정부 표준프레임워크(eGovFrame) 5.0 Boot 기반 게시판 프로젝트**를 만듭니다.

- Java 17 과 Maven 을 사용합니다.
- 내장 HSQL DB 로 기동되어야 합니다.
- 게시물에 첨부파일 기능이 있어야 합니다.

## 테이블

아래 DDL 을 기준으로 합니다. 필요한 컬럼이나 테이블은 자유롭게 추가해도 됩니다.

```sql
CREATE TABLE BOARD (
  BOARD_ID VARCHAR(20) NOT NULL,
  TITLE    VARCHAR(200) NOT NULL,
  CONTENT  VARCHAR(4000),
  WRITER   VARCHAR(50) NOT NULL,
  PRIMARY KEY (BOARD_ID)
);
```
