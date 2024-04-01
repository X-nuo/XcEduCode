package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {
    @Autowired
    private FileSystemRepository fileSystemRepository;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_server;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;

    /**
     * 初始化FastDFS配置
     */
    private void initFastDFS() {
        try {
            ClientGlobal.initByTrackers(tracker_server);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * FastDFS上传操作
     * @param file
     * @return file_ID
     */
    public String fdfs_upload(MultipartFile file) {
        //1.FastDFS准备
        //1.1 初始化FastDFS配置
        initFastDFS();
        //1.2 创建Tracker客户端
        TrackerClient trackerClient = new TrackerClient();
        try {
            //1.3 连接Tracker服务，获取Storage服务
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //1.4 创建Storage客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);

            //2.上传文件准备
            //2.1 文件字节
            byte[] bytes = file.getBytes();
            //2.2 文件扩展名
            String filename = file.getOriginalFilename();
            String extname = filename.substring(filename.lastIndexOf(".") + 1);

            //3.文件上传操作
            String file1_id = storageClient1.upload_file1(bytes, extname, null);
            return file1_id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * FileSystem上传文件
     * @param file
     * @param filetag
     * @param businesskey
     * @param metadata
     * @return
     */
    public UploadFileResult upload(MultipartFile file, String filetag, String businesskey, String metadata) {
        if(file == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        String fileId = this.fdfs_upload(file);
        if(StringUtils.isEmpty(fileId)) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        if(StringUtils.isNotEmpty(metadata)) {
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        fileSystem.setFileName(file.getOriginalFilename());
        fileSystem.setFileSize(file.getSize());
        fileSystem.setFileType(file.getContentType());
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }
}
