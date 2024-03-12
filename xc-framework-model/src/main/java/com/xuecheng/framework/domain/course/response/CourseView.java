package com.xuecheng.framework.domain.course.response;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 课程视图（课程详情页模型数据）
 */
@Data
@ToString
@NoArgsConstructor
public class CourseView {
    CourseBase courseBase;
    CoursePic coursePic;
    CourseMarket courseMarket;
    TeachplanNode teachplanNode;
}
