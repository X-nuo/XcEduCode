package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface TaskRepository extends JpaRepository<XcTask, String> {
    /**
     * 根据更新时间之前获取任务
     * @param pageable
     * @param updateTime
     * @return
     */
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    /**
     * 更新任务时间
     * @param taskId
     * @param updateTime
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :taskId")
    public int updateTaskTime(@Param("taskId") String taskId, @Param("updateTime") Date updateTime);

    /**
     * 更新任务version +1
     * @param taskId
     * @param version
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.version = :version+1 where t.id = :taskId and t.version = :version")
    public int updateTaskVersion(@Param("taskId") String taskId, @Param("version") int version);
}
