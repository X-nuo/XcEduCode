package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.SysDictionaryValue;
import com.xuecheng.framework.domain.system.request.QueryDictionaryRequest;
import com.xuecheng.framework.domain.system.response.SysCode;
import com.xuecheng.framework.domain.system.response.SysDictionaryResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SysDictionaryService {
    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    /**
     * 根据字典类型查询字典明细
     * @param dtype
     * @return
     */
    public SysDictionary getSysDictionaryVal(String dtype) {
        return sysDictionaryRepository.findByDType(dtype);
    }

    /**
     * 查询所有字典类型
     * @return
     */
    public List<SysDictionary> getSysDictionary(QueryDictionaryRequest queryDictionaryRequest) {
        //条件匹配器（模糊查询）
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("dName", ExampleMatcher.GenericPropertyMatchers.contains());
        //构建查询条件
        SysDictionary sysDictionary = new SysDictionary();
        if(StringUtils.isNotEmpty(queryDictionaryRequest.getDname())) {
            sysDictionary.setDName(queryDictionaryRequest.getDname());
        }
        Example<SysDictionary> example = Example.of(sysDictionary,exampleMatcher);
        //排序
        Sort sort = Sort.by(Sort.Direction.ASC, "dType");
        return sysDictionaryRepository.findAll(example, sort);
    }

    /**
     * 根据Id查询字典
     * @param sysDictionaryId
     * @return
     */
    public SysDictionary getSysDictionaryById(String sysDictionaryId) {
        Optional<SysDictionary> sysDictionaryOptional = sysDictionaryRepository.findById(sysDictionaryId);
        if(sysDictionaryOptional.isPresent()) {
            return sysDictionaryOptional.get();
        }
        return null;
    }

    /**
     * 新增数据字典
     * @return
     */
    public ResponseResult addSysDictionary(SysDictionary sysDictionary) {
        SysDictionary old_sysDictionary = sysDictionaryRepository.findByDNameOrDType(sysDictionary.getDName(), sysDictionary.getDType());
        if(old_sysDictionary != null) {
            ExceptionCast.cast(SysCode.SYS_ADDDICTIONARY_EXISTSNAME);
        }
        List<SysDictionaryValue> dValue = new ArrayList<>();
        sysDictionary.setDValue(dValue);
        sysDictionaryRepository.save(sysDictionary);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 新增数据字典明细
     * @param sysDictionaryId
     * @param sysDictionaryValue
     * @return
     */
    public SysDictionaryResult addSysDictionaryVal(String sysDictionaryId, SysDictionaryValue sysDictionaryValue) {
        SysDictionary sysDictionary = this.getSysDictionaryById(sysDictionaryId);
        if(sysDictionary == null) {
            ExceptionCast.cast(SysCode.SYS_ADDDICTIONARYVAL_NOTEXISTSNAME);
        }
        List<SysDictionaryValue> dValue = sysDictionary.getDValue();
        dValue.add(sysDictionaryValue);
        SysDictionary save = sysDictionaryRepository.save(sysDictionary);
        return new SysDictionaryResult(CommonCode.SUCCESS, save);
    }

    /**
     * 删除数据字典
     * @param sysDictionaryId
     * @return
     */
    public ResponseResult delSysDictionaryById(String sysDictionaryId) {
        SysDictionary old_SysDictionary = this.getSysDictionaryById(sysDictionaryId);
        if(old_SysDictionary != null) {
            sysDictionaryRepository.deleteById(sysDictionaryId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
}
