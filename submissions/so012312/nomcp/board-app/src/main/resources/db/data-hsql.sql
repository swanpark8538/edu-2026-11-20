--------------------------------------------------------------------
-- eGovFrame 5.0 Boot 게시판 : 초기 데이터
-- ID 발번 테이블(EgovTableIdGnrServiceImpl) 의 시작 번호를 100000 으로 두어
-- 아래 미리 심어 둔 게시물 번호와 겹치지 않도록 한다.
--------------------------------------------------------------------

INSERT INTO IDS (TABLE_NAME, NEXT_ID) VALUES ('BOARD', 100000);
INSERT INTO IDS (TABLE_NAME, NEXT_ID) VALUES ('ATC_FILE', 100000);

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, VIEW_CNT, REG_DT)
VALUES ('BBS-0000000000000001', 'eGovFrame 5.0 Boot 게시판을 시작합니다.',
        '전자정부 표준프레임워크 5.0 Boot 기반의 게시판입니다. 내장 HSQLDB 로 기동하며 게시물에 첨부파일을 올릴 수 있습니다.',
        '관리자', 12, DATEADD('DAY', -4, CURRENT_TIMESTAMP));

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, VIEW_CNT, REG_DT)
VALUES ('BBS-0000000000000002', '첨부파일 업로드 테스트 안내',
        '등록 화면에서 파일을 2개까지 올려 보세요. 허용 확장자와 최대 크기는 application.properties 의 board.file.* 에서 정합니다.',
        '홍길동', 5, DATEADD('DAY', -3, CURRENT_TIMESTAMP));

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, VIEW_CNT, REG_DT)
VALUES ('BBS-0000000000000003', '게시물 구조 안내',
        'BOARD 테이블에 VIEW_CNT(조회수), REG_DT(등록일), MOD_DT(수정일) 컬럼을 추가했고, 첨부파일은 ATC_FILE 테이블에 저장합니다.',
        '김철수', 3, DATEADD('DAY', -2, CURRENT_TIMESTAMP));

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, VIEW_CNT, REG_DT)
VALUES ('BBS-0000000000000004', '조회수는 세션당 1회만 증가합니다.',
        '같은 브라우저로 상세 화면을 여러 번 열어 보아도 조회수는 한 번만 올라갑니다.',
        '이영희', 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP));

INSERT INTO BOARD (BOARD_ID, TITLE, CONTENT, WRITER, VIEW_CNT, REG_DT)
VALUES ('BBS-0000000000000005', '검색과 페이징 확인',
        '제목·내용·글쓴이 조건으로 검색할 수 있고, 목록 아래 페이징은 표준프레임워크 PaginationInfo 를 사용합니다.',
        '박지민', 0, CURRENT_TIMESTAMP);
