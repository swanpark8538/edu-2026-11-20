-- ============================================================
--  eGovFrame 5.0 Boot 게시판  :  초기 데이터
-- ============================================================

-- 채번 시작 값 (샘플 게시물 3건 이후부터 ID 발급)
INSERT INTO IDS (TABLE_NAME, NEXT_ID) VALUES ('BOARD', 4);
INSERT INTO IDS (TABLE_NAME, NEXT_ID) VALUES ('BOARD_FILE', 1);

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, READCNT, CREATE_DT)
VALUES ('0000000001', 'eGovFrame 5.0 Boot 게시판에 오신 것을 환영합니다.',
        '전자정부 표준프레임워크 5.0 Boot + 내장 HSQLDB + MyBatis 으로 동작하는 게시판입니다. 상단의 [새 글 쓰기] 버튼으로 첨부파일을 함께 등록해 보세요.', 'admin', 12, CURRENT_TIMESTAMP);

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, READCNT, CREATE_DT)
VALUES ('0000000002', '첨부파일 테스트 안내',
        '등록/수정 화면에서 여러 개의 파일을 업로드할 수 있습니다. 업로드된 파일은 egov.board.attach-path 에 지정된 폴더에 저장되고, BOARD_FILE 테이블에 원본 파일명과 저장 파일명이 함께 관리됩니다.', 'hong', 3, CURRENT_TIMESTAMP);

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, READCNT, CREATE_DT)
VALUES ('0000000003', '게시물 검색 / 페이징 확인',
        '검색 조건(제목, 내용, 작성자, 제목+내용)과 페이징은 eGovFrame PaginationInfo 를 사용합니다.', 'kim', 0, CURRENT_TIMESTAMP);
