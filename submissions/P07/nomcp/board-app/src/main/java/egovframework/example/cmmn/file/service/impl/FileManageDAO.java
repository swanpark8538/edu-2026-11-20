package egovframework.example.cmmn.file.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.example.cmmn.file.service.FileVO;

@Repository("fileManageDAO")
public class FileManageDAO extends EgovAbstractMapper {

    public void insertAttachFile(String atchFileId) {
        insert("fileManageDAO.insertAttachFile", atchFileId);
    }

    public void insertFileDetail(FileVO fileVO) {
        insert("fileManageDAO.insertFileDetail", fileVO);
    }

    public int selectMaxFileSn(String atchFileId) {
        return selectOne("fileManageDAO.selectMaxFileSn", atchFileId);
    }

    public int selectFileCount(String atchFileId) {
        return selectOne("fileManageDAO.selectFileCount", atchFileId);
    }

    public List<FileVO> selectFileList(String atchFileId) {
        return selectList("fileManageDAO.selectFileList", atchFileId);
    }

    public FileVO selectFile(FileVO fileVO) {
        return selectOne("fileManageDAO.selectFile", fileVO);
    }

    public FileVO selectDownloadFile(FileVO fileVO) {
        return selectOne("fileManageDAO.selectDownloadFile", fileVO);
    }

    public int deleteFileDetail(FileVO fileVO) {
        return delete("fileManageDAO.deleteFileDetail", fileVO);
    }

    public int deleteAttachFile(String atchFileId) {
        return delete("fileManageDAO.deleteAttachFile", atchFileId);
    }

}
