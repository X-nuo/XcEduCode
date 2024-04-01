package com.xuecheng.manage_cms.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.ext.CmsTemplateExt;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsTemplateFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;

    /**
     * 查询CMS模版列表
     * @return
     */
    public QueryResponseResult getTemplates() {
        List<CmsTemplate> cmsTemplates = cmsTemplateRepository.findAll();

        QueryResult<CmsTemplate> queryResult = new QueryResult<>();
        queryResult.setList(cmsTemplates);
        queryResult.setTotal(cmsTemplates.size());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 查询CMS模版列表（分页查询、条件查询）
     * @param page
     * @param size
     * @param queryTemplateRequest
     * @return
     */
    public QueryResponseResult getTemplates(int page, int size, QueryTemplateRequest queryTemplateRequest) {
        //构建查询条件
        CmsTemplate cmsTemplate = new CmsTemplate();
        if(StringUtils.isNotEmpty(queryTemplateRequest.getSiteId())) {
            cmsTemplate.setSiteId(queryTemplateRequest.getSiteId());
        }
        Example<CmsTemplate> example = Example.of(cmsTemplate);
        //构建分页条件
        if(page <= 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page-1, size);
        //查询
        Page<CmsTemplate> cmsTemplates = cmsTemplateRepository.findAll(example, pageable);
        //构建响应结果
        QueryResult<CmsTemplate> queryResult = new QueryResult<>();
        queryResult.setList(cmsTemplates.getContent());
        queryResult.setTotal(cmsTemplates.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 新增模版
     * @param cmsTemplate
     * @param file
     * @return
     */
    public ResponseResult addTemplate(CmsTemplate cmsTemplate, MultipartFile file) {
        if (file != null) {
            //1.向GridFS存储模版文件
            InputStream inputStream = null;
            try {
                inputStream = file.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ObjectId objectId = gridFsTemplate.store(inputStream, file.getOriginalFilename());
            //2.保存GridFS生成的FileID
            cmsTemplate.setTemplateFileId(objectId.toString());
        }
        cmsTemplateRepository.save(cmsTemplate);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 添加模版文件
     * @param templateId
     * @param file
     * @return
     */
    public ResponseResult addTemplateFileById(String templateId, MultipartFile file) {
        CmsTemplate old_template = this.findTemplateById(templateId);
        if(old_template == null) {
            return new ResponseResult(CommonCode.FAIL);
        }
        String templateFileId = old_template.getTemplateFileId();
        //如果存在模版文件，则先进行删除
        if (StringUtils.isNotEmpty(templateFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
        }
        ObjectId objectId = null;
        try {
            objectId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        old_template.setTemplateFileId(objectId.toString());
        cmsTemplateRepository.save(old_template);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取模版文件
     * @param templateId
     * @return
     */
    public CmsTemplateFileResult getTemplateFile(String templateId) {
        Optional<CmsTemplate> cmsTemplate = cmsTemplateRepository.findById(templateId);
        if(cmsTemplate.isPresent()) {
            //1.获取模版文件ID
            String templateFileId = cmsTemplate.get().getTemplateFileId();
            if(StringUtils.isEmpty(templateFileId)) {
                ExceptionCast.cast(CmsCode.CMS_TEMPLATEFILE_ISEMPTY);
            }
            //2.根据模版文件ID获取模版文件
            GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //3.打开模版文件下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(fsFile.getObjectId());
            //4.创建模版文件流对象
            GridFsResource gridFsResource = new GridFsResource(fsFile, gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return new CmsTemplateFileResult(CommonCode.SUCCESS, content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new CmsTemplateFileResult(CommonCode.FAIL, null);
    }

    /**
     * 根据Id获取模版信息
     * @param templateId
     * @return
     */
    public CmsTemplate findTemplateById(String templateId) {
        Optional<CmsTemplate> cmsTemplate = cmsTemplateRepository.findById(templateId);
        if(cmsTemplate.isPresent()) {
            return cmsTemplate.get();
        }
        return null;
    }

    /**
     * 修改模版信息
     * @param templateId
     * @param cmsTemplateExt
     * @return
     */
    public ResponseResult updateTemplate(String templateId, CmsTemplateExt cmsTemplateExt) {
        CmsTemplate old_template = this.findTemplateById(templateId);
        if(old_template == null) {
            return new ResponseResult(CommonCode.FAIL);
        }
        old_template.setTemplateName(cmsTemplateExt.getTemplateName());
        old_template.setTemplateParameter(cmsTemplateExt.getTemplateParameter());
        old_template.setSiteId(cmsTemplateExt.getSiteId());
        String templateFileId = old_template.getTemplateFileId();
        //如果存在模版文件，则保存新的模版文件，再删除旧文件
        if(StringUtils.isNotEmpty(templateFileId)) {
            GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开文件上传流
            GridFSUploadStream gridFSUploadStream = gridFSBucket.openUploadStream(fsFile.getFilename());
            gridFSUploadStream.write(cmsTemplateExt.getTemplateValue().getBytes());
            gridFSUploadStream.close();
            ObjectId fileId = gridFSUploadStream.getObjectId();
            //更新模版文件ID
            old_template.setTemplateFileId(fileId.toString());
            //删除旧的模版文件
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
        }
        cmsTemplateRepository.save(old_template);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 删除模版
     * @param templateId
     * @return
     */
    public ResponseResult deleteTemplate(String templateId) {
        CmsTemplate old_CmsTemplate = this.findTemplateById(templateId);
        if(old_CmsTemplate != null) {
            String templateFileId = old_CmsTemplate.getTemplateFileId();
            if(StringUtils.isNotEmpty(templateFileId)) {
                gridFsTemplate.delete(Query.query(Criteria.where("_id").is(templateFileId)));
            }
            cmsTemplateRepository.deleteById(templateId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

}
