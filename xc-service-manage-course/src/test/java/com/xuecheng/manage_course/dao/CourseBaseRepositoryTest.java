package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.QueryCourseRequest;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourseBaseRepositoryTest {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CourseService courseService;

    /**
     * 查询我的课程列表（分页查询）
     */
    @Test
    public void testFindCourseList() {
        PageHelper.startPage(2, 10);
        Page<CourseInfo> courseList = courseMapper.findCourseList(new QueryCourseRequest());
        List<CourseInfo> result = courseList.getResult();
        System.out.println(result);
    }

    @Test
    public void testFindCategoryList() {
        CategoryNode categoryList = categoryMapper.findCategoryList();
        System.out.println(categoryList.getId());
    }

    @Test
    public void testSaveMediaPub() {
        courseService.saveTeachplanMediaPub("297e7c7c62b888f00162b8a7dec20000");
    }
}
