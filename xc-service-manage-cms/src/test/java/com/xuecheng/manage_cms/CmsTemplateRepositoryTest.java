package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsTemplateRepositoryTest {
    @Autowired
    private CmsTemplateRepository templateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;



    /**
     * 获取GriFS中的模版文件
     */
    @Test
    public void testGetTemplateFile() {
        Optional<CmsTemplate> cmsTemplateOptional = templateRepository.findById("643d193ec9774339d9ef9bab");
        if(cmsTemplateOptional.isPresent()) {
            //1.获取CmsTemplateFileId
            CmsTemplate cmsTemplate = cmsTemplateOptional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            //2.根据ID查询模版文件
            GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //3.打开模版文件下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(fsFile.getObjectId());
            //4.获取流对象
            GridFsResource gridFsResource = new GridFsResource(fsFile, gridFSDownloadStream);
            //5.获取流中数据
            String content = null;
            try {
                content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(content);
        }
    }
    @Test
    public void testUpdateTemplateFile() {
        //64a290ce047fc33331ee6923
        String content = "update content";
        Optional<CmsTemplate> cmsTemplateOptional = templateRepository.findById("64a290ce047fc33331ee6925");
        if(cmsTemplateOptional.isPresent()) {
            CmsTemplate cmsTemplate = cmsTemplateOptional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();

            GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            GridFSUploadStream gridFSUploadStream = gridFSBucket.openUploadStream(fsFile.getFilename());

            gridFSUploadStream.write(content.getBytes());
            ObjectId objectId = gridFSUploadStream.getObjectId();
            System.out.println(objectId.toString());    //新模版文件ID

            gridFSUploadStream.close();
            //删除旧模版文件ID
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
        }
    }

    @Test
    public void testDeleteTemplateFile(){
        String templateFileId = "64426c0da48b031006e10d9b";
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
    }
}
