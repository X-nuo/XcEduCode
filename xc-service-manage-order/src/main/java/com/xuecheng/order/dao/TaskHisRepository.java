package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTaskHis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskHisRepository extends JpaRepository<XcTaskHis, String> {
}
