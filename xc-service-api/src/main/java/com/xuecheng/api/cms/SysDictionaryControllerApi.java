package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.SysDictionaryValue;
import com.xuecheng.framework.domain.system.request.QueryDictionaryRequest;
import com.xuecheng.framework.domain.system.response.SysDictionaryResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.List;

/**
 * SysDictionary API
 */
//@Api(value = "SysDictionary 管理接口")
public interface SysDictionaryControllerApi {
    //@ApiOperation("查询字典列表")
    public List<SysDictionary> getSysDictionaryList(QueryDictionaryRequest queryDictionaryRequest);
    //@ApiOperation("根据字典类型查询")
    public SysDictionary getSysDictionaryByDicType(String dtype);
    //@ApiOperation("新增数据字典")
    public ResponseResult add(SysDictionary sysDictionary);
    //@ApiOperation("新增字典明细")
    public SysDictionaryResult addVal(String sysDictionaryId, SysDictionaryValue sysDictionaryValue);
    //@ApiOperation("删除数据字典")
    public ResponseResult del(String sysDictionaryId);
}
