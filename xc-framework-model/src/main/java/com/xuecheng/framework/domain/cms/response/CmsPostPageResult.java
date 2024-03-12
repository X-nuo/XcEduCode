package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by admin on 2018/3/5.
 */
@Data
@ToString
@NoArgsConstructor
public class CmsPostPageResult extends ResponseResult {
    public CmsPostPageResult(ResultCode resultCode, String url) {
        super(resultCode);
        this.pageUrl = url;
    }
    String pageUrl;
}
