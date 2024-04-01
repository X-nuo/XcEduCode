package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.TaskHisRepository;
import com.xuecheng.order.dao.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    TaskHisRepository taskHisRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 获取任务（在更新时间之前的N条任务数据）
     * @param updateTime    指定更新时间
     * @param n             获取任务数
     * @return
     */
    public List<XcTask> getTasks(Date updateTime, int n) {
        //Pageable pageable = new PageRequest(0, n);
        Pageable pageable = PageRequest.of(0, n);
        Page<XcTask> xcTasks = taskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }

    /**
     * 乐观锁（解决并发问题）
     * 防止并发重复发送任务消息：当成功发送任务消息，version+1，返回成功执行条数1；当重复发送消息时，根据版本号查询不到该消息，version更新失败。
     * @param taskId
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String taskId, int version) {
        int i = taskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    /**
     * 发送任务消息
     * @param xcTask
     * @param ex        交换机
     * @param routing   路由
     */
    @Transactional
    public void publish(XcTask xcTask, String ex, String routing) {
        /*
        Optional<XcTask> xcTaskOptional = taskRepository.findById(xcTask.getId());
        if(xcTaskOptional.isPresent()) {
            //RabbitMQ发送任务消息（message: XcTask, routingkey: ）
            rabbitTemplate.convertAndSend(ex, routing, xcTask);
            //更新任务时间
            XcTask one = xcTaskOptional.get();
            one.setUpdateTime(new Date());
            taskRepository.save(one);
        }
         */
        if(xcTask != null) {
            rabbitTemplate.convertAndSend(ex, routing, xcTask);
            taskRepository.updateTaskTime(xcTask.getId(), new Date());
        }
    }

    /**
     * 完成课程更新任务
     * @param taskId
     */
    @Transactional
    public void finishCourseTask(String taskId) {
        Optional<XcTask> xcTaskOptional = taskRepository.findById(taskId);
        if(xcTaskOptional.isPresent()) {
            XcTask xcTask = xcTaskOptional.get();
            //设置任务删除时间
            xcTask.setDeleteTime(new Date());
            //保存历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            taskHisRepository.save(xcTaskHis);
            //删除任务（已完成）
            taskRepository.delete(xcTask);
        }
    }
}
