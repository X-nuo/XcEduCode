package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileSystem API
 */
//@Api(value = "FileSystem 管理接口")
public interface FileSystemControllerApi {
//    @ApiOperation("文件上传")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "file", value = "上传文件", dataType = "MultipartFile"),
//            @ApiImplicitParam(name = "filetag", value = "业务标签", dataType = "String"),
//            @ApiImplicitParam(name = "businesskey", value = "业务key", dataType = "String"),
//            @ApiImplicitParam(name = "metadata", value = "文件元信息", dataType = "String")
//    })
    public UploadFileResult upload(MultipartFile file, String filetag, String businesskey, String metadata);
}
