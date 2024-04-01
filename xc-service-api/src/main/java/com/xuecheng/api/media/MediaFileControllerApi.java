package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;

//@Api(value = "Media 媒资管理接口")
public interface MediaFileControllerApi {
    //@ApiOperation("查询媒资列表")
    public QueryResponseResult getMediaList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);
}
