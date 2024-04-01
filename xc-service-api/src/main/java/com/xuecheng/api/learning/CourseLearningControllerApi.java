package com.xuecheng.api.learning;

import com.xuecheng.framework.domain.learning.response.MediaPublishResult;

/**
 * Learning API
 */
//@Api(value = "Course Learning 管理接口")
public interface CourseLearningControllerApi {
    //@ApiOperation("获取媒资URL")
    MediaPublishResult getMedia(String courseId, String teachplanId);
}
