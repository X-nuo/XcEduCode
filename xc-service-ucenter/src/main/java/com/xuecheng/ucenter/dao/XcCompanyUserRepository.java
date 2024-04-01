package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser, String> {
    /**
     * 根据用户ID查询公司信息
     * @param userId
     * @return
     */
    XcCompanyUser findByUserId(String userId);
}
