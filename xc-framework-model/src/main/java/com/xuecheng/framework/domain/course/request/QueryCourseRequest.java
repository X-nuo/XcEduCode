package com.xuecheng.framework.domain.course.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;
import lombok.ToString;

/**
 * Created by mrt on 2018/4/13.
 */
@Data
@ToString
public class QueryCourseRequest extends RequestData {
    //@ApiModelProperty("公司ID")
    private String companyId;
}
