package com.xuecheng.search.controller;

import com.xuecheng.api.course.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {
    @Autowired
    EsCourseService esCourseService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult searchCourseList(@PathVariable("page") int page, @PathVariable("size") int size, CourseSearchParam courseSearchParam) {
        return esCourseService.searchCourseList(page, size, courseSearchParam);
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getCoursePub(@PathVariable("id") String courseId) {
        return esCourseService.getCoursePub(courseId);
    }

    @Override
    @GetMapping("/getmedia/{id}")
    public TeachplanMediaPub getMediaPub(@PathVariable("id") String teachplanId) {
        String[] teachplanIds = new String[]{teachplanId};
        QueryResponseResult responseResult = esCourseService.getMediaPub(teachplanIds);
        QueryResult queryResult = responseResult.getQueryResult();
        if(queryResult != null && queryResult.getList() != null && queryResult.getList().size() > 0) {
            return (TeachplanMediaPub)queryResult.getList().get(0);
        }
        return new TeachplanMediaPub();
    }
}
