package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {
    public static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    /**
     *  定时任务
     *  每隔1分钟扫描任务表，并向MQ发送任务数据
     */
    @Scheduled(fixedDelay = 60000)
    public void sendCourseTask() {
        //获取当前时间前1分钟
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();
        //获取当前时间前一分钟的任务数据
        List<XcTask> tasks = taskService.getTasks(time, 1000);
        for(XcTask xcTask : tasks) {
            String taskId = xcTask.getId();
            Integer version = xcTask.getVersion();
            //乐观锁，防止并发问题
            if(taskService.getTask(taskId, version) > 0) {
                //发送任务消息
                taskService.publish(xcTask, xcTask.getMqExchange(), xcTask.getMqRoutingkey());
                LOGGER.info("send choose course task id:{}",taskId);
            }
        }
    }

    /**
     * 监听完成课程更新消息
     * @param xcTask
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void finishCourseTask(XcTask xcTask) {
        if(xcTask != null && StringUtils.isNotEmpty(xcTask.getId())) {
            taskService.finishCourseTask(xcTask.getId());
        }
    }

}
