package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.SysDictionaryValue;
import com.xuecheng.framework.domain.system.request.QueryDictionaryRequest;
import com.xuecheng.framework.domain.system.response.SysDictionaryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/dictionary")
public class SysDictionaryController implements SysDictionaryControllerApi {
    @Autowired
    private SysDictionaryService sysDictionaryService;

    @Override
    @GetMapping("/list")
    public List<SysDictionary> getSysDictionaryList(QueryDictionaryRequest queryDictionaryRequest) {
        return sysDictionaryService.getSysDictionary(queryDictionaryRequest);
    }

    @Override
    @GetMapping("/get/{dtype}")
    public SysDictionary getSysDictionaryByDicType(@PathVariable("dtype") String dtype) {
        return sysDictionaryService.getSysDictionaryVal(dtype);
    }

    @Override
    @PostMapping("/add")
    public ResponseResult add(@RequestBody SysDictionary sysDictionary) {
        return sysDictionaryService.addSysDictionary(sysDictionary);
    }

    @Override
    @PutMapping("/addVal/{id}")
    public SysDictionaryResult addVal(@PathVariable("id") String sysDictionaryId, @RequestBody SysDictionaryValue sysDictionaryValue) {
        return sysDictionaryService.addSysDictionaryVal(sysDictionaryId, sysDictionaryValue);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") String sysDictionaryId) {
        return sysDictionaryService.delSysDictionaryById(sysDictionaryId);
    }
}
