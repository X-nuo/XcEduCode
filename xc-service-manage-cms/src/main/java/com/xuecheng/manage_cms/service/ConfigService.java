package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {
    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    /**
     * 根据ID查询配置（数据模型）信息
     * @param id
     * @return
     */
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> cmsConfig = cmsConfigRepository.findById(id);
        if (cmsConfig.isPresent()) {
            return cmsConfig.get();
        }
        return null;
    }
}
