package com.xuecheng.manage_media.upload;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * 断点续传
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FTPTest {
    /**
     * 文件分块
     */
    @Test
    public void testChunk() throws IOException {
        //1.分块前准备
        //1.1 定义目标文件和分块目录
        File sourceFile = new File("/Users/xnuo/Desktop/XcEduProject/ffmpeg/lucene.avi");
        String chunkPath = "/Users/xnuo/Desktop/XcEduProject/ffmpeg/chunk/";
        File chunkFolder = new File(chunkPath);
        if(!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //1.2 确定分块数量
        //分块大小：1MB
        long chunkSize = 1024 * 1024 * 1;
        //分块数量：文件大小/分块大小
        long chunkCount = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        if(chunkCount < 0) {
            chunkCount = 1;
        }
        //1.3 定义缓冲区（用于文件读取写入操作）
        byte[] buffer = new byte[1024];

        //2.进行分块
        //2.1 使用RandomAccessFile操作目标文件(sourceFile)，进行读取操作
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //2.2 循环读取目标文件，写入分块文件
        for(int i=0; i<chunkCount; i++) {
            //2.2.1 创建分块文件
            File chunkFile = new File(chunkPath + i);
            boolean newFile = chunkFile.createNewFile();
            if(newFile) {
                //2.2.2 使用RandomAccessFile操作分块文件(chunkFile)，进行写入操作
                RandomAccessFile raf_write = new RandomAccessFile(sourceFile, "rw");
                //2.2.3 文件的拷贝
                int len = -1;
                while((len = raf_read.read(buffer)) != -1) {
                    raf_write.write(buffer, 0, len);
                    if(chunkFile.length() > chunkSize) {
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }

    /**
     * 分块合并
     */
    @Test
    public void testMerge() throws IOException {
        //1.合并前准备
        //1.1 确定分块目录
        File chunkFolder = new File("/Users/xnuo/Desktop/XcEduProject/ffmpeg/chunk/");
        //1.2 创建合并文件
        File mergeFile = new File("/Users/xnuo/Desktop/XcEduProject/ffmpeg/lucene_merge.avi");
        if(mergeFile.exists()) {
            mergeFile.delete();
        }
        mergeFile.createNewFile();
        //1.3 定义缓存区（用于文件读取写入操作）
        byte[] buffer = new byte[1024];
        //2.对分块文件进行排序
        //2.1 将分块文件数组转换成List集合（方便排序）
        File[] chunkFiles = chunkFolder.listFiles();
        List<File> fileList  = new ArrayList<>(Arrays.asList(chunkFiles));
        //2.2 按照分块文件名（1，2，3，...）进行排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        //3.进行合并
        //3.1 使用RandomAccessFile操作合并文件(mergeFile)，进行写入操作
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        raf_write.seek(0);
        //3.2 遍历分块文件，写入合并文件
        for(File chunkFile : fileList) {
            //3.2.1 使用RandomAccessFile操作分块文件(chunkFile)，进行读取操作
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
            //3.2.2 文件的拷贝
            int len = -1;
            while((len = raf_read.read(buffer)) != -1) {
                raf_write.write(buffer);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
