package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

//@Api(value = "ES-Course 管理接口")
public interface EsCourseControllerApi {
//    @ApiOperation("搜索课程列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
//            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
//    })
    public QueryResponseResult searchCourseList(int page, int size, CourseSearchParam courseSearchParam);
    //@ApiOperation("获取课程信息")
    public Map<String, CoursePub> getCoursePub(String courseId);
    //@ApiOperation("获取课程媒资信息")
    public TeachplanMediaPub getMediaPub(String teachplanId);
}
