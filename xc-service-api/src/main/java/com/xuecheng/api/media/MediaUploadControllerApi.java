package com.xuecheng.api.media;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.GenerateHtmlResult;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Media Upload API
 */
//@Api(value = "Media 媒资文件上传接口")
public interface MediaUploadControllerApi {
    //@ApiOperation("上传注册")
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
    //@ApiOperation("分块检查")
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);
    //@ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5);
    //@ApiOperation("合并分块")
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

}
