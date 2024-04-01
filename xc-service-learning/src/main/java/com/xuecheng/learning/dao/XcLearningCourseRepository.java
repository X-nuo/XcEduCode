package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse, String> {
    /**
     *
     * @param userId
     * @param courseId
     * @return
     */
    XcLearningCourse findXcLearningCourseByUserIdAndCourseId(String userId, String courseId);
}
