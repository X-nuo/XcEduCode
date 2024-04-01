package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.response.CoursePreviewResult;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.QueryCourseRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.response.CourseView;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {
    @Autowired
    private CourseService courseService;

    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    @PreAuthorize("hasAuthority('course_find_list')")
    public QueryResponseResult findCourseList(@PathVariable("page") int page, @PathVariable("size") int size, QueryCourseRequest queryCourseRequest) {
        //进行细粒度授权（获取公司ID）
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        return courseService.findCourseList(page, size, userJwt.getCompanyId(), queryCourseRequest);
    }

    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.add(courseBase);
    }

    @Override
    @GetMapping("/coursebase/{id}")
    @PreAuthorize("hasAuthority('course_get_baseinfo')")
    public CourseBase findCourseBaseById(@PathVariable("id") String courseId) {
        return courseService.findCourseBaseById(courseId);
    }

    @Override
    @PutMapping("/coursebase/edit/{id}")
    public ResponseResult editCourseBase(@PathVariable("id") String courseId, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(courseId, courseBase);
    }

    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    @Override
    @GetMapping("/coursemarket/get/{id}")
    public CourseMarket getCourseMarketById(@PathVariable("id") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override
    @PostMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String courseId, @RequestBody                                                                                                                                                                                        CourseMarket courseMarket) {
        CourseMarket new_CourseMarket = courseService.updateCourseMarket(courseId, courseMarket);
        if(new_CourseMarket != null) {
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId, pic);
    }

    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findByCourseId(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePicByCourseId(courseId);
    }

    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteById(@RequestParam("courseId") String courseId) {
        return courseService.deleteById(courseId);
    }

    @Override
    @GetMapping("/courseview/{id}")
    public CourseView getCourseView(@PathVariable("id") String courseId) {
        return courseService.getCourseView(courseId);
    }

    @Override
    @PostMapping("/preview/{id}")
    public CoursePreviewResult preview(@PathVariable("id") String courseId) {
        return courseService.preview(courseId);
    }

    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String courseId) {
        return courseService.publish(courseId);
    }

    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
}
