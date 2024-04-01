package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.request.QuerySiteRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    /**
     * 查询CMS站点列表
     * @return
     */
    public QueryResponseResult getSites() {
        List<CmsSite> cmsSites = cmsSiteRepository.findAll();

        QueryResult<CmsSite> queryResult = new QueryResult<>();
        queryResult.setList(cmsSites);
        queryResult.setTotal(cmsSites.size());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 查询CMS站点列表（分页查询，条件查询）
     * @param page
     * @param size
     * @param querySiteRequest
     * @return
     */
    public QueryResponseResult getSites(int page, int size, QuerySiteRequest querySiteRequest) {
        //条件匹配器（模糊查询）
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("siteName", ExampleMatcher.GenericPropertyMatchers.contains());
        //构建查询条件
        CmsSite cmsSite = new CmsSite();
        if(StringUtils.isNotEmpty(querySiteRequest.getSiteName())) {
            cmsSite.setSiteName(querySiteRequest.getSiteName());
        }
        Example<CmsSite> example = Example.of(cmsSite, exampleMatcher);
        //构建分页
        if(page <= 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page-1, size);
        //查询
        Page<CmsSite> cmsSites = cmsSiteRepository.findAll(example, pageable);
        //构建响应信息
        QueryResult<CmsSite> queryResult = new QueryResult<>();
        queryResult.setTotal(cmsSites.getTotalElements());
        queryResult.setList(cmsSites.getContent());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 新增站点
     * @param cmsSite
     * @return
     */
    public ResponseResult addSite(CmsSite cmsSite) {
        cmsSiteRepository.save(cmsSite);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据ID查询站点
     * @param siteId
     * @return
     */
    public CmsSite findSiteById(String siteId) {
        Optional<CmsSite> cmsSite = cmsSiteRepository.findById(siteId);
        if(cmsSite.isPresent()) {
            return cmsSite.get();
        }
        return null;
    }

    /**
     * 修改站点
     * @param siteId
     * @param cmsSite
     * @return
     */
    public ResponseResult updateSite(String siteId, CmsSite cmsSite) {
        CmsSite old_cmsSite = this.findSiteById(siteId);
        if(old_cmsSite != null) {
            old_cmsSite.setSiteName(cmsSite.getSiteName());
            old_cmsSite.setSiteDomain(cmsSite.getSiteDomain());
            old_cmsSite.setSitePort(cmsSite.getSitePort());
            old_cmsSite.setSiteWebPath(cmsSite.getSiteWebPath());
            old_cmsSite.setSitePhysicalPath(cmsSite.getSitePhysicalPath());
            old_cmsSite.setSiteCreateTime(cmsSite.getSiteCreateTime());
            cmsSiteRepository.save(old_cmsSite);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 删除站点
     * @param siteId
     * @return
     */
    public ResponseResult deleteSite(String siteId) {
        CmsSite cmsSite = this.findSiteById(siteId);
        if(cmsSite != null) {
            cmsSiteRepository.deleteById(siteId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
}
