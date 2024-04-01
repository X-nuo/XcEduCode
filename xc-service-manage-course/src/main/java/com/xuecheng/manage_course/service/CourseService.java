package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.response.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.QueryCourseRequest;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CmsPageClient cmsPageClient;
    @Value("${course-publish.siteId}")
    private String course_siteId;
    @Value("${course-publish.templateId}")
    private String course_templateId;
    @Value("${course-publish.pageWebPath}")
    private String course_pageWebPath;
    @Value("${course-publish.pagePhysicalPath}")
    private String course_pagePhysicalPath;
    @Value("${course-publish.dataUrlPre}")
    private String course_dataUrlPre;
    @Value("${course-publish.previewUrlPre}")
    private String course_previewUrlPre;

    /**
     * 查询课程列表（分页查询，条件查询）
     * @param page
     * @param size
     * @param companyId
     * @param queryCourseRequest
     * @return
     */
    public QueryResponseResult findCourseList(int page, int size, String companyId, QueryCourseRequest queryCourseRequest) {
        //构建分页
        PageHelper.startPage(page, size);
        //构建查询条件
        if(queryCourseRequest == null) {
            queryCourseRequest = new QueryCourseRequest();
        }
        queryCourseRequest.setCompanyId(companyId);
        //查询
        Page<CourseInfo> courseList = courseMapper.findCourseList(queryCourseRequest);
        //构建响应信息
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(courseList.getResult());
        queryResult.setTotal(courseList.getTotal());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 新增课程
     * @param courseBase
     * @return
     */
    public AddCourseResult add(CourseBase courseBase) {
        //新增课程默认状态未发布
        courseBase.setStatus("202001");
        CourseBase save = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, save.getId());
    }

    /**
     * 根据ID查询课程基础信息
     * @param courseId
     * @return
     */
    public CourseBase findCourseBaseById(String courseId) {
        Optional<CourseBase> courseBase = courseBaseRepository.findById(courseId);
        if(courseBase.isPresent()) {
            return courseBase.get();
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    /**
     * 修改课程基础信息
     * @param courseId
     * @param courseBase
     * @return
     */
    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase) {
        CourseBase old_coursebase = this.findCourseBaseById(courseId);
        if(old_coursebase != null) {
            old_coursebase.setName(courseBase.getName());
            old_coursebase.setUsers(courseBase.getUsers());
            old_coursebase.setMt(courseBase.getMt());
            old_coursebase.setSt(courseBase.getSt());
            old_coursebase.setGrade(courseBase.getGrade());
            old_coursebase.setStudymodel(courseBase.getStudymodel());
            old_coursebase.setDescription(courseBase.getDescription());
            courseBaseRepository.save(old_coursebase);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 查询课程计划列表
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.findTeachplanList(courseId);
        return teachplanNode;
    }

    /**
     * 增加课程计划
     * @param teachplan
     * @return
     */
    public ResponseResult addTeachplan(Teachplan teachplan) {
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        //1.判断课程计划父节点是否为空
        //1.1 父节点为空：说明该课程计划是第二层级（grade:2）
        if(StringUtils.isEmpty(parentid)) {
            //1.1.1 获取根节点ID
            List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseid, "0");
            if(teachplanList == null || teachplanList.size() == 0) {    //根节点为空，则创建根节点，获取根节点ID
                Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseid);
                if(courseBaseOptional.isPresent()) {
                    CourseBase courseBase = courseBaseOptional.get();
                    Teachplan teachplanRoot = new Teachplan();
                    teachplanRoot.setPname(courseBase.getName());
                    teachplanRoot.setParentid("0");
                    teachplanRoot.setCourseid(courseid);
                    teachplanRoot.setGrade("1");
                    teachplanRoot.setStatus("0");
                    Teachplan save = teachplanRepository.save(teachplanRoot);
                    parentid = save.getId();
                }
            }else { //根节点不为空，则直接获取根节点ID
                Teachplan teachplanRoot = teachplanList.get(0);
                parentid = teachplanRoot.getId();
            }
            //1.1.2 保存课程计划
            teachplan.setParentid(parentid);
            teachplan.setGrade("2");
            teachplanRepository.save(teachplan);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //1.2 父节点不为空：说明该课程计划是第三层级（grade:3）
        //1.2.1 保存课程计划
        teachplan.setGrade("3");
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程营销信息
     * @param courseId
     * @return
     */
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(courseId);
        if(courseMarket.isPresent()) {
            return courseMarket.get();
        }
        return null;
    }

    /**
     * 更新课程营销信息
     * @param courseId
     * @param courseMarket
     * @return
     */
    public CourseMarket updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket old_courseMarket = this.getCourseMarketById(courseId);
        if(old_courseMarket != null) {
            old_courseMarket.setCharge(courseMarket.getCharge());
            old_courseMarket.setPrice(courseMarket.getPrice());
            old_courseMarket.setValid(courseMarket.getValid());
            old_courseMarket.setStartTime(courseMarket.getStartTime());
            old_courseMarket.setEndTime(courseMarket.getEndTime());
            old_courseMarket.setQq(courseMarket.getQq());
            courseMarketRepository.save(old_courseMarket);
        }else {
            old_courseMarket = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, old_courseMarket);
            old_courseMarket.setId(courseId);
            courseMarketRepository.save(old_courseMarket);
        }

        return old_courseMarket;
    }

    /**
     * 保存课程图片信息
     * @param courseid
     * @param pic 图片请求路径
     * @return
     */
    public ResponseResult saveCoursePic(String courseid, String pic) {
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseid);
        CoursePic coursePic = null;
        if(coursePicOptional.isPresent()) {
            coursePic = coursePicOptional.get();
            coursePic.setPic(pic);
        } else {
            coursePic = new CoursePic();
            coursePic.setCourseid(courseid);
            coursePic.setPic(pic);
        }
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据课程ID查询图片信息
     * @param courseId
     * @return
     */
    public CoursePic findCoursePicByCourseId(String courseId) {
        Optional<CoursePic> coursePic = coursePicRepository.findById(courseId);
        if(coursePic.isPresent()) {
            return coursePic.get();
        }
        return null;
    }

    /**
     * 删除图片信息
     * @param courseId
     * @return
     */
    public ResponseResult deleteById(String courseId) {
        CoursePic coursePic = this.findCoursePicByCourseId(courseId);
        if(coursePic != null) {
            coursePicRepository.deleteById(courseId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 获取课程视图（静态化页面的课程数据）
     * @param courseId
     * @return
     */
    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();
        Optional<CourseBase> courseBase = courseBaseRepository.findById(courseId);
        if(courseBase.isPresent()) {
            courseView.setCourseBase(courseBase.get());
        }
        Optional<CoursePic> coursePic = coursePicRepository.findById(courseId);
        if(coursePic.isPresent()) {
            courseView.setCoursePic(coursePic.get());
        }
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(courseId);
        if(courseMarket.isPresent()) {
            courseView.setCourseMarket(courseMarket.get());
        }
        TeachplanNode teachplanNode = teachplanMapper.findTeachplanList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    /**
     * 预览课程（保存课程详情页面）
     * @param courseId
     * @return
     */
    public CoursePreviewResult preview(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //保存课程详情页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName(courseId + ".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPageWebPath(course_pageWebPath);
        cmsPage.setPagePhysicalPath(course_pagePhysicalPath);
        cmsPage.setSiteId(course_siteId);
        cmsPage.setTemplateId(course_templateId);
        cmsPage.setDataUrl(course_dataUrlPre + courseId);
        cmsPage.setPageStatus("100002");
        cmsPage.setPageCreateTime(new Date());
        //远程请求CMS保存页面
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(cmsPageResult.isSuccess()) {
            //页面预览URL
            String previewUrl = course_previewUrlPre + cmsPageResult.getCmsPage().getPageId();
            return new CoursePreviewResult(CommonCode.SUCCESS, previewUrl);
        }
        return new CoursePreviewResult(CourseCode.COURSE_PUBLISH_CDETAILERROR, null);
    }

    /**
     * 创建课程发布对象
     * @param courseId
     * @return
     */
    public CoursePub createCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();
        if(StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        //课程发布对象ID
        coursePub.setId(courseId);
        //课程基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if(courseBaseOptional.isPresent()) {
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //课程图片信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if(coursePicOptional.isPresent()) {
            CoursePic coursePic = coursePicOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if(courseMarketOptional.isPresent()) {
            CourseMarket courseMarket = courseMarketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        TeachplanNode teachplanList = teachplanMapper.findTeachplanList(courseId);
        coursePub.setTeachplan(JSON.toJSONString(teachplanList));

        return coursePub;
    }

    /**
     * 保存课程发布对象
     * @param courseId
     * @param coursePub
     * @return
     */
    public CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        CoursePub new_CoursePub;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        if(coursePubOptional.isPresent()) {
            new_CoursePub = coursePubOptional.get();
        } else {
            new_CoursePub = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub, new_CoursePub);
        //设置发布时间戳（logstash使用）
        new_CoursePub.setTimestamp(new Date());
        //设置发布时间（String类型）
        new_CoursePub.setPubTime(new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date()));
        coursePubRepository.save(new_CoursePub);

        return new_CoursePub;
    }

    /**
     * 课程发布（发布课程详情页面）
     * @param courseId
     * @return
     */
    public CoursePublishResult publish(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //保存课程详情页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName(courseId + ".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPageWebPath(course_pageWebPath);
        cmsPage.setPagePhysicalPath(course_pagePhysicalPath);
        cmsPage.setSiteId(course_siteId);
        cmsPage.setTemplateId(course_templateId);
        cmsPage.setDataUrl(course_dataUrlPre + courseId);
        //远程请求CMS发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //修改课程发布状态
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        //保存课程发布信息（同步ElasticSearch搜索服务）
        CoursePub coursePub = this.createCoursePub(courseId);
        CoursePub new_CoursePub = this.saveCoursePub(courseId, coursePub);
        if(new_CoursePub == null) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATEINDEXERROR);
        }
        //保存课程计划媒资发布信息（同步ElasticSearch搜索服务）
        this.saveTeachplanMediaPub(courseId);
        //获取课程页面访问地址
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    /**
     * 保存课程视频（关联课程计划）
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if(!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLANISNULL);
        }
        Teachplan teachplan = teachplanOptional.get();
        // 只允许为子叶课程计划绑定课程视频
        if(!teachplan.getGrade().equals("3") || StringUtils.isEmpty(teachplan.getGrade())) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLANGRADEISERROR);
        }

        TeachplanMedia teachplanMedia_save = null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if(teachplanMediaOptional.isPresent()) {
            teachplanMedia_save = teachplanMediaOptional.get();
        } else {
            teachplanMedia_save = new TeachplanMedia();
        }
        teachplanMedia_save.setTeachplanId(teachplanId);
        teachplanMedia_save.setCourseId(teachplanMedia.getCourseId());
        teachplanMedia_save.setMediaId(teachplanMedia.getMediaId());
        teachplanMedia_save.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        teachplanMedia_save.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(teachplanMedia_save);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 保存课程计划媒资发布对象
     * @param courseId
     */
    public void saveTeachplanMediaPub(String courseId) {
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for(TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }
}
