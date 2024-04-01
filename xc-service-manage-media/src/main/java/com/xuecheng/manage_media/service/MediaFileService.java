package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class MediaFileService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * 查询媒资文件列表
     * @param page
     * @param size
     * @param queryMediaFileRequest
     * @return
     */
    public QueryResponseResult getMediaFiles(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        //条件匹配器（模糊查询、精确匹配）
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());
        //构建查询条件
        MediaFile mediaFile = new MediaFile();
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        Example<MediaFile> example = Example.of(mediaFile, matcher);
        //构建分页
        if(page <= 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page-1, size);
        //查询
        Page<MediaFile> mediaFilePage = mediaFileRepository.findAll(example, pageable);
        //构建响应信息
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(mediaFilePage.getContent());
        queryResult.setTotal(mediaFilePage.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }
}
