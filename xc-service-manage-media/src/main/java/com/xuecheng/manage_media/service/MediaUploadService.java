package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {
    @Autowired
    MediaFileRepository mediaFileRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    /**
     * 上传注册
     * 在前端WebUploder组件，before-send-file回调中使用，主要完成一些上传文件的准备工作：
     * 1.检查文件是否已上传，已上传则直接返回；
     * 2.检查文件上传目录是否存在，不存在则创建上传目录。
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1. 检查文件是否上传
        //1.1 文件路径检查
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //1.2 数据库检查
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(fileMd5);
        //1.3 文件已上传，则抛出已上传信息
        if(file.exists() && mediaFileOptional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //2.文件未上传，则创建文件上传目录
        boolean createFolderBool = createFileFolder(fileMd5);
        if(!createFolderBool) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 分块检查
     * 在前端WebUploder组件，before-send回调中使用，主要对上传前分块文件进行检查：
     * 1.检查该分块是否已经上传；
     * 2.检查分块目录是否存在，不存在则创建分块目录
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //1.检查分块目录是否存在，不存在则创建分块目录
        this.createChunkFolder(fileMd5);
        //2.检查分块是否已经存在
        //2.1 分块路径检查
        String chunkPath = this.getChunkPath(fileMd5, chunk);
        File file = new File(chunkPath);
        if(file.exists()) { //2.2 若分块已经存在，则返回存在信息
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }else { //2.3 若分块不存在，则返回不存在信息
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    /**
     * 上传分块
     * 通过文件流的方式将前端传入的文件按顺序写入分块文件中
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        if(file == null) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }
        //1.分块上传前准备
        //1.1 准备文件流
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        //1.2 准备分块文件（以流的方式写入文件不需要手动创建目标文件）
        String chunkPath = this.getChunkPath(fileMd5, chunk);
        File chunkFile = new File(chunkPath);
        //2.写入流程
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkFile);
            IOUtils.copy(inputStream, outputStream);
        }catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块
     * 在前端WebUploder组件，after-send-file回调中使用，当所有分块上传完毕：
     * 1.合并分块
     * 2.校验文件Md5
     * 3.在数据库记录文件信息
     * 4.视频处理（.avi ---> .mp4 ---> hls）
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.合并前准备
        //1.1 获取所有分块文件
        List<File> chunkFileList = this.getChunksFile(new File(this.getChunkFolder(fileMd5)));
        //1.2 创建合并文件
        File mergeFile = new File(this.getFilePath(fileMd5, fileExt));
        //1.2.1 若合并文件存在，则先删除再创建
        if(mergeFile.exists()) {
            mergeFile.delete();
        }
        //1.2.2 创建合并文件
        boolean newFile = false;
        try {
            newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!newFile) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CREATEFAIL);
        }
        //2.进行文件的合并
        mergeFile = this.mergeFile(chunkFileList, mergeFile);
        if(mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //3.文件校验
        boolean checkRes = this.checkFileMd5(mergeFile, fileMd5);
        if(!checkRes) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //4.将文件信息保存数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFilePath(this.getFileFolderRelatviePath(fileMd5));    //保存文件相对路径
        mediaFile.setFileSize(fileSize);
        mediaFile.setFileType(fileExt);
        mediaFile.setMimeType(mimetype);
        mediaFile.setUploadTime(new Date());
        mediaFile.setFileStatus("301002");  //文件上传成功
        mediaFileRepository.save(mediaFile);
        //5.视频处理
        this.sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取文件路径
     * 文件路径 = 根目录 / 一级目录（Md5的第一个字符）/ 二级目录（Md5的第二个字符）/ 三级目录（Md5）/ 文件名（Md5）.文件后缀
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        String filePath = uploadPath + "/" + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
        return filePath;
    }

    /**
     * 获取文件目录相对路径
     * @param fileMd5
     * @return
     */
    private String getFileFolderRelatviePath(String fileMd5) {
        String fileFolderRelativePath = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        return fileFolderRelativePath;
    }

    /**
     * 获取文件上传目录
     * 文件上传目录 = 根目录 / 一级目录（Md5的第一个字符）/ 二级目录（Md5的第二个字符）/ 三级目录（Md5）/
     * @param fileMd5
     * @return
     */
    private String getFileFolder(String fileMd5) {
        String fileFolder = uploadPath + "/" + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        return fileFolder;
    }

    /**
     * 创建文件上传目录
     * @param fileMd5
     * @return
     */
    private boolean createFileFolder(String fileMd5) {
        String fileFolder = this.getFileFolder(fileMd5);
        File file = new File(fileFolder);
        if(!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs;
        }
        return true;
    }

    /**
     * 获取分块路径
     * 分块路径 = 文件上传目录 / chunks /分块（分块以1，2，3..序列命名，没有后缀）
     * @param fileMd5
     * @param chunk
     * @return
     */
    private String getChunkPath(String fileMd5, Integer chunk) {
        String chunkPath = this.getFileFolder(fileMd5) + "chunks/" + chunk;
        return chunkPath;
    }

    /**
     * 获取分块目录
     * 分块目录 = 文件上传目录 / chunks /
     * @param fileMd5
     * @return
     */
    private String getChunkFolder(String fileMd5) {
        String chunkFolder = this.getFileFolder(fileMd5) + "chunks/";
        return chunkFolder;
    }

    /**
     * 创建分块目录
     * @param fileMd5
     */
    private void createChunkFolder(String fileMd5) {
        String fileFolder = this.getChunkFolder(fileMd5);
        File file = new File(fileFolder);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 获取所有分块文件
     * 注意：需要对分块文件进行顺序排序
     * @param chunksFolder 分块目录
     * @return
     */
    private List<File> getChunksFile(File chunksFolder) {
        File[] chunkArray = chunksFolder.listFiles();
        List<File> chunkList = new ArrayList<>(Arrays.asList(chunkArray));
        Collections.sort(chunkList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        return chunkList;
    }

    /**
     * 块文件合并流程
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            //1.使用RandomAccessFile操作合并文件(mergeFile)，进行写入操作
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            raf_write.seek(0);
            //2.准备缓冲区
            byte[] buffer = new byte[1024];
            //3.遍历分块文件，写入合并文件
            for(File chunkFile : chunkFileList) {
                //3.1 使用RandomAccessFile操作分块文件(chunkFile)，进行读取操作
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
                //3.2 文件的拷贝
                int len = -1;
                while((len = raf_read.read(buffer)) != -1) {
                    raf_write.write(buffer, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mergeFile;
    }

    /**
     * 校验文件Md5
     * @param mergeFile
     * @param fileMd5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if(mergeFile == null || StringUtils.isEmpty(fileMd5)) {
            return false;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(mergeFile);
            String mergeFileMd5 = DigestUtils.md5Hex(inputStream);
            if(mergeFileMd5.equalsIgnoreCase(fileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    /**
     * RabbitMQ发送视频处理消息
     * @param mediaId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        String msg = JSON.toJSONString(msgMap);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
