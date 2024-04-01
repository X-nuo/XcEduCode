package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {
    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.fields}")
    private String fields;
    @Value("${xuecheng.elasticsearch.media.index}")
    private String es_media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String es_media_type;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 搜索课程列表
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult searchCourseList(int page, int size, CourseSearchParam courseSearchParam) {
        //搜索请求对象，设置索引和类型
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_index);
        searchRequest.types(es_type);
        //搜索条件构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.布尔查询：用于组合多个查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            //在"name"、"teachplan"、"descriptioin"三个字段中去匹配
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description");
            //设置匹配占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            //提升"name"字段的权重
            multiMatchQueryBuilder.field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //1.2 课程分类和课程等级过滤
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            //termQuery：字段匹配过滤
            //rangeQuery：范围匹配过滤
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        //1.3 完成布尔搜索设置
        searchSourceBuilder.query(boolQueryBuilder);

        //2.高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //2.1 设置高亮标签
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //2.2 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        //2.3 完成高亮条件设置
        searchSourceBuilder.highlighter(highlightBuilder);

        //3.搜索分页
        if(page <= 0) {
            page = 1;
        }
        if(size <= 0) {
            size = 20;
        }
        //3.1 设置当前页的第一条数据
        searchSourceBuilder.from((page-1)*size);
        //3.2 设置页面容量
        searchSourceBuilder.size(size);

        //4.请求搜索
        searchRequest.source(searchSourceBuilder);
        //搜索响应
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //5.结果集处理
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        //5.1 结果总数
        long totalHits = searchHits.getTotalHits();
        //5.2 结果数据列表
        List<CoursePub> coursePubList = new ArrayList<>();
        for(SearchHit hit : hits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //获取id
            String id = (String) sourceAsMap.get("id");
            coursePub.setId(id);
            //获取高亮name
            String name = (String) sourceAsMap.get("name");
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields != null) {
                HighlightField nameField = highlightFields.get("name");
                if(nameField != null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text : fragments) {
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
            }
            coursePub.setName(name);
            //获取pic
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //获取charge
            String charge = (String) sourceAsMap.get("charge");
            coursePub.setCharge(charge);
            //获取price
            Float price = null;
            try{
                if(sourceAsMap.get("price") != null) {
                    price = Float.parseFloat(sourceAsMap.get("price").toString());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            //获取old_price
            Float price_old = null;
            if(sourceAsMap.get("price_old") != null) {
                price_old = Float.parseFloat(sourceAsMap.get("price_old").toString());
            }
            coursePub.setPrice_old(price_old);

            coursePubList.add(coursePub);
        }

        //构建响应结果
        QueryResult queryResult = new QueryResult();
        queryResult.setList(coursePubList);
        queryResult.setTotal(totalHits);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 获取课程信息（根据课程ID）
     * @param courseId
     * @return
     */
    public Map<String, CoursePub> getCoursePub(String courseId) {
        //搜索请求对象，设置索引和类型
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_index);
        searchRequest.types(es_type);
        //搜索条件构建器（根据课程ID搜索）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id", courseId));
        //请求搜索
        searchRequest.source(searchSourceBuilder);
        //搜索响应
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //结果集处理
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        Map<String, CoursePub> map = new HashMap<>();
        for(SearchHit hit : hits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("id");
            String name = (String) sourceAsMap.get("name");
            String grade = (String) sourceAsMap.get("grade");
            String charge = (String) sourceAsMap.get("charge");
            String pic = (String) sourceAsMap.get("pic");
            String description = (String) sourceAsMap.get("description");
            String teachplan = (String) sourceAsMap.get("teachplan");
            coursePub.setId(id);
            coursePub.setName(name);
            coursePub.setGrade(grade);
            coursePub.setCharge(charge);
            coursePub.setPic(pic);
            coursePub.setDescription(description);
            coursePub.setTeachplan(teachplan);
            map.put(id, coursePub);
        }
        return map;
    }

    /**
     * 获取课程媒资信息（根据多个课程计划ID）
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult getMediaPub(String[] teachplanIds) {
        //搜索请求对象，设置索引和类型
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_media_index);
        searchRequest.types(es_media_type);
        //搜索条件构建器（根据课程ID搜索）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanIds));
        //请求搜索
        searchRequest.source(searchSourceBuilder);
        //搜索响应
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //结果集处理
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        long totalHits = searchHits.getTotalHits();
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for(SearchHit hit : hits) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String teachplanId = (String) sourceAsMap.get("teachplan_id");
            String mediaId = (String) sourceAsMap.get("media_id");
            String courseId = (String) sourceAsMap.get("courseid");
            String mediaUrl = (String) sourceAsMap.get("media_url");
            String fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
            teachplanMediaPub.setCourseId(courseId);
            teachplanMediaPub.setMediaId(mediaId);
            teachplanMediaPub.setTeachplanId(teachplanId);
            teachplanMediaPub.setMediaUrl(mediaUrl);
            teachplanMediaPub.setMediaFileOriginalName(fileoriginalname);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult();
        queryResult.setTotal(totalHits);
        queryResult.setList(teachplanMediaPubList);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }
}
