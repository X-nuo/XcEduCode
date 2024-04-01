package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.CategoryNode;

/**
 * Category API
 */
//@Api(value = "Category 管理接口")
public interface CategoryControllerApi {
    //@ApiOperation("查询课程分类列表")
    public CategoryNode findCategoryList();
}
