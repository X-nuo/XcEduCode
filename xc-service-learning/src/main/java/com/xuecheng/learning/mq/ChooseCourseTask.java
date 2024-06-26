package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Component
public class ChooseCourseTask {
    public static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);
    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 监听课程任务消息
     * @param xcTask
     * @throws Exception
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void saveCourse(XcTask xcTask) throws Exception {
        //获取XcTask信息（用户ID，课程ID，...）
        String requestBody = xcTask.getRequestBody();
        Map map = JSON.parseObject(requestBody, Map.class);
        String userId = (String) map.get("userId");
        String courseId = (String) map.get("courseId");
        String valid = (String) map.get("valid");
        //处理日期格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Date startTime = null;
        Date endTime = null;
        if(map.get("startTime") != null) {
            startTime = simpleDateFormat.parse((String) map.get("startTime"));
        }
        if(map.get("endTime") != null) {
            endTime = simpleDateFormat.parse((String) map.get("endTime"));
        }
        //添加课程
        ResponseResult addCourseResult = learningService.addCourseForUser(userId, courseId, valid, startTime, endTime, xcTask);
        //课程添加完成，需要向订单系统进行反馈
        if(addCourseResult.isSuccess()) {
            //发送完成课程更新响应
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE, RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask);
            LOGGER.info("send choose course task id:{}", xcTask.getId());
        }
    }


}
