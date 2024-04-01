package com.xuecheng.filesystem;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FileSystemTest {
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_server;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;

    /**
     * FastDFS 文件查询测试
     */
    @Test
    public void testQueryFile() throws MyException, IOException {
        ClientGlobal.initByTrackers(tracker_server);
        ClientGlobal.setG_charset(charset);
        ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
        ClientGlobal.setG_network_timeout(network_timeout_in_seconds);

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storeStorage = null;
        StorageClient storageClient = new StorageClient(trackerServer, storeStorage);

        FileInfo info = storageClient.query_file_info("group1", "M00/00/00/rBr2OGRcxBuADin0AAG6lc9K-Hc974.JPG");
        System.out.println(info);
    }
}
