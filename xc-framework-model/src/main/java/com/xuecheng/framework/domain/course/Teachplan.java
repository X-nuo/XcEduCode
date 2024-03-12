package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by admin on 2018/2/7.
 */
@Data
@ToString
@Entity
@Table(name="teachplan")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class Teachplan implements Serializable {
    private static final long serialVersionUID = -916357110051689485L;
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String id;
    private String pname;
    private String parentid;
    //层级 分为1，2，3级
    private String grade;
    //课程类型 1：视频，2：文档
    private String ptype;
    //章节及课程介绍
    private String description;
    //课程ID
    private String courseid;
    //状态 已发布，未发布
    private String status;
    //排序字段
    private Integer orderby;
    //时长 单位分钟
    private Double timelength;
    //是否试学
    private String trylearn;

}
