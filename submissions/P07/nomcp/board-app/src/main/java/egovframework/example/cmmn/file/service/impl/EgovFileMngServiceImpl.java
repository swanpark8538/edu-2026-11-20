package egovframework.example.cmmn.file.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.cmmn.file.service.EgovFileMngService;
import egovframework.example.cmmn.file.service.FileVO;
import jakarta.annotation.Resource;

@Service("egovFileMngService")
public class EgovFileMngServiceImpl extends EgovAbstractServiceImpl implements EgovFileMngService {

    private static final int MAX_FILE_NAME_LENGTH = 255;

    @Resource(name = "fileManageDAO")
    private FileManageDAO fileManageDAO;

    @Resource(name = "fileIdGnrService")
    private EgovIdGnrService fileIdGnrService;

    @Resource(name = "egovFileStorage")
    private EgovFileStorage fileStorage;

    private final Set<String> allowedExtensions;

    private final int maxCount;

    public EgovFileMngServiceImpl(
            @Value("${Globals.fileUpload.extensions}") String extensions,
            @Value("${Globals.fileUpload.maxCount:10}") int maxCount) {
        this.allowedExtensions = Arrays.stream(extensions.split(","))
                .map(ext -> ext.strip().toLowerCase(Locale.ROOT))
                .filter(ext -> !ext.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.maxCount = maxCount;
    }

    @Override
    public List<FileVO> selectFileList(String atchFileId) throws Exception {
        if (atchFileId == null || atchFileId.isEmpty()) {
            return new ArrayList<>();
        }
        return fileManageDAO.selectFileList(atchFileId);
    }

    @Override
    public int selectFileCount(String atchFileId) throws Exception {
        if (atchFileId == null || atchFileId.isEmpty()) {
            return 0;
        }
        return fileManageDAO.selectFileCount(atchFileId);
    }

    @Override
    public FileVO selectDownloadFile(String atchFileId, int fileSn) throws Exception {
        FileVO key = new FileVO();
        key.setAtchFileId(atchFileId);
        key.setFileSn(fileSn);
        return fileManageDAO.selectDownloadFile(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveFiles(String atchFileId, List<MultipartFile> files) throws Exception {
        List<MultipartFile> uploads = files == null ? List.of()
                : files.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (uploads.isEmpty()) {
            return atchFileId;
        }

        List<String> originalNames = new ArrayList<>();
        for (MultipartFile upload : uploads) {
            originalNames.add(validate(upload));
        }

        boolean newGroup = atchFileId == null || atchFileId.isEmpty();
        int existingCount = newGroup ? 0 : fileManageDAO.selectFileCount(atchFileId);
        if (existingCount + uploads.size() > maxCount) {
            throw processException("fail.file.maxCount", new String[] {String.valueOf(maxCount)});
        }

        String groupId = atchFileId;
        if (newGroup) {
            groupId = fileIdGnrService.getNextStringId();
            fileManageDAO.insertAttachFile(groupId);
        }

        int fileSn = newGroup ? 0 : fileManageDAO.selectMaxFileSn(groupId);
        List<String> storedNames = new ArrayList<>();
        deleteStoredFilesOnRollback(storedNames);
        try {
            for (int i = 0; i < uploads.size(); i++) {
                MultipartFile upload = uploads.get(i);
                String originalName = originalNames.get(i);
                String storedName = fileStorage.store(upload);
                storedNames.add(storedName);

                FileVO fileVO = new FileVO();
                fileVO.setAtchFileId(groupId);
                fileVO.setFileSn(++fileSn);
                fileVO.setStreFileNm(storedName);
                fileVO.setOrignlFileNm(originalName);
                fileVO.setFileExtsn(extensionOf(originalName));
                fileVO.setFileSize(upload.getSize());
                fileManageDAO.insertFileDetail(fileVO);
            }
        } catch (IOException e) {
            throw processException("fail.file.store", e);
        }
        return groupId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String atchFileId, int fileSn) throws Exception {
        FileVO key = new FileVO();
        key.setAtchFileId(atchFileId);
        key.setFileSn(fileSn);
        FileVO fileVO = fileManageDAO.selectFile(key);
        if (fileVO == null) {
            throw processException("fail.file.notFound");
        }
        fileManageDAO.deleteFileDetail(fileVO);
        deleteStoredFilesAfterCommit(List.of(fileVO.getStreFileNm()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllFiles(String atchFileId) throws Exception {
        if (atchFileId == null || atchFileId.isEmpty()) {
            return;
        }
        List<String> storedNames = fileManageDAO.selectFileList(atchFileId).stream()
                .map(FileVO::getStreFileNm)
                .toList();
        fileManageDAO.deleteAttachFile(atchFileId);
        deleteStoredFilesAfterCommit(storedNames);
    }

    @Override
    public Path resolveStoredPath(FileVO fileVO) throws Exception {
        return fileStorage.resolve(fileVO.getStreFileNm());
    }

    /** 원본 파일명을 정리하고 확장자 화이트리스트를 검사한다. */
    private String validate(MultipartFile upload) throws Exception {
        String name = upload.getOriginalFilename();
        if (name == null) {
            throw processException("fail.file.invalidName");
        }
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        name = name.replaceAll("[\\p{Cntrl}]", "").strip();
        if (name.isEmpty() || name.length() > MAX_FILE_NAME_LENGTH) {
            throw processException("fail.file.invalidName");
        }
        String extension = extensionOf(name);
        if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
            throw processException("fail.file.notAllowedExt", new String[] {extension.isEmpty() ? "-" : extension});
        }
        return name;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteStoredFilesOnRollback(List<String> storedNames) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storedNames.forEach(fileStorage::delete);
                }
            }
        });
    }

    private void deleteStoredFilesAfterCommit(List<String> storedNames) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            storedNames.forEach(fileStorage::delete);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storedNames.forEach(fileStorage::delete);
            }
        });
    }

}
