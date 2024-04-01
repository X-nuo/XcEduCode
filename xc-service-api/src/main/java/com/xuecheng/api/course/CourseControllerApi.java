package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.response.CoursePreviewResult;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.QueryCourseRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.response.CourseView;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

/**
 * Course API
 */
//@Api(value = "Course 管理接口")
public interface CourseControllerApi {
//    @ApiOperation("查询我的课程列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
//            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
//    })
    public QueryResponseResult findCourseList(int page, int size, QueryCourseRequest queryCourseRequest);
    //@ApiOperation("新增课程")
    public AddCourseResult addCourseBase(CourseBase courseBase);
    //@ApiOperation("根据ID查询课程基础信息")
    public CourseBase findCourseBaseById(String courseId);
    //@ApiOperation("修改课程基础信息")
    public ResponseResult editCourseBase(String courseId, CourseBase courseBase);
    //@ApiOperation("查询课程计划列表")
    public TeachplanNode findTeachplanList(String courseId);
    //@ApiOperation("增加课程计划")
    public ResponseResult addTeachplan(Teachplan teachplan);
    //@ApiOperation("获取课程营销信息")
    public CourseMarket getCourseMarketById(String courseId);
    //@ApiOperation("修改课程营销信息")
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket);
    //@ApiOperation("保存课程图片信息")
    public ResponseResult addCoursePic(String courseId, String pic);
    //@ApiOperation("根据课程ID查询图片信息")
    public CoursePic findByCourseId(String courseId);
    //@ApiOperation("删除图片信息")
    public ResponseResult deleteById(String courseId);
    //@ApiOperation("课程视图查询")
    public CourseView getCourseView(String courseId);
    //@ApiOperation("课程预览")
    public CoursePreviewResult preview(String courseId);
    //@ApiOperation("课程发布")
    public CoursePublishResult publish(String courseId);
    //@ApiOperation("保存课程视频")
    public ResponseResult savemedia(TeachplanMedia teachplanMedia);
}
