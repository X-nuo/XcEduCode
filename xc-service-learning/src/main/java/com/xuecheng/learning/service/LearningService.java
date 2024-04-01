package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.learning.response.MediaPublishResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class LearningService {
    @Autowired
    CourseSearchClient courseSearchClient;

    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    /**
     * 获取媒资URL（Feign服务调用）
     * @param courseId
     * @param teachplanId
     * @return
     */
    public MediaPublishResult getMedia(String courseId, String teachplanId) {
        //校验权限（学习权限、是否付费...）

        //获取媒资URL
        TeachplanMediaPub mediaPub = courseSearchClient.getMediaPub(teachplanId);
        if(mediaPub == null || StringUtils.isEmpty(mediaPub.getMediaUrl())) {
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }

        return new MediaPublishResult(CommonCode.SUCCESS, mediaPub.getMediaUrl());
    }

    /**
     * 为用户添加课程（订单支付成功后）
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    @Transactional
    public ResponseResult addCourseForUser(String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask) {
        //检查历史任务（课程是否已经添加）
        Optional<XcTaskHis> xcTaskHisOptional = xcTaskHisRepository.findById(xcTask.getId());
        if(xcTaskHisOptional.isPresent()) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //历史任务不存在（课程未添加）
        //添加课程
        //检查课程是否已经存在
        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findXcLearningCourseByUserIdAndCourseId(userId, courseId);
        if(xcLearningCourse == null) { //课程不存在，则添加课程
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        } else { //课程存在，则更新日期
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        }
        //课程更新完毕，添加历史任务
        XcTaskHis xcTaskHis = new XcTaskHis();
        BeanUtils.copyProperties(xcTask, xcTaskHis);
        xcTaskHisRepository.save(xcTaskHis);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
