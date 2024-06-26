package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.QueryCourseRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Administrator.
 */
@Mapper
public interface CourseMapper {
   CourseBase findCourseBaseById(String id);

   /**
    * 查询我的课程列表（分页查询，条件查询）
    * @param queryCourseRequest
    * @return
    */
   Page<CourseInfo> findCourseList(QueryCourseRequest queryCourseRequest);
}
