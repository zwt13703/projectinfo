package com.kgc.pro.proservice.service;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.GetMapping;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EsService {

    @Autowired
    JestClient jestClient;

    /**
     * 创建索引
     * @return
     * @throws Exception
     */
    public void createIndex(String indexName) throws Exception {
        JestResult jr = jestClient.execute(new CreateIndex.Builder(indexName).build());
        System.out.println(jr.isSucceeded());
    }

    /**
     * 新增数据
     * @return
     * @throws Exception
     */
    public void insert(Class cl,String indexName,String typeName) throws Exception {
        Index index = new Index.Builder(cl).index(indexName).type(typeName).build();
        try{
            JestResult jr = jestClient.execute(index);
            System.out.println(jr.isSucceeded());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 查询数据
     * @return
     * @throws Exception
     */
    public void getIndexMapping(String indexName,String typeName) throws Exception {
        GetMapping getMapping = new GetMapping.Builder().addIndex(indexName).addType(typeName).build();
        JestResult jr =jestClient.execute(getMapping);
        System.out.println(jr.getJsonString());
    }

    /**
     * 向ElasticSearch中批量新增
     */
    public void insertBatch(List<Class> list,String indexName,String typeName){
        boolean result = false;
        try {
            Bulk.Builder bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(typeName);
            for (Object obj : list) {
                Index index = new Index.Builder(obj).build();
                bulk.addAction(index);
            }
            BulkResult br = jestClient.execute(bulk.build());
            result = br.isSucceeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("批量新增:"+result);
    }

    /**
     * 全文搜索
     * @param indexName
     * @param typeName
     * @param query
     * @return
     * @throws Exception
     */
    public String search(Class cl,String indexName, String typeName, String query) throws Exception {
        Search search = new Search.Builder(query).addIndex(indexName).addType(typeName).build();
        JestResult jr = jestClient.execute(search);
        System.out.println("--++"+jr.getJsonString());
        System.out.println("--"+jr.getSourceAsObject(cl));
        return jr.getSourceAsString();
    }

    /**
     * 分页带条件搜索
     */
    public void serach1(Class cl,String query,String indexName,String typeName) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.queryStringQuery(query));
            //分页设置
            searchSourceBuilder.from(0).size(2);
            System.out.println("全文搜索查询语句:"+searchSourceBuilder.toString());
            System.out.println("全文搜索返回结果:"+search(cl,indexName, typeName, searchSourceBuilder.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 精确搜索(在指定的属性上精确查询)
     * 未完成
     */
//    public  void serach2(Class cl,String query,String indexName,String typeName) {
//        try {
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            searchSourceBuilder.query(QueryBuilders.termQuery("age", 24));
//            System.out.println("精确搜索查询语句:"+searchSourceBuilder.toString());
//            System.out.println("精确搜索返回结果:"+search(cl,indexName, typeName, searchSourceBuilder.toString()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 区间搜索 创建的时间
     */
    public  void serach3(String field, DateTime time1, DateTime time2,Class cl,String indexName,String typeName) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery(field).gte(time1).lte(time2));
            System.out.println("区间搜索语句:"+searchSourceBuilder.toString());
            System.out.println("区间搜索返回结果:"+search(cl,indexName, typeName, searchSourceBuilder.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除索引
     * @return
     * @throws Exception
     */
    public void deleteIndex(String indexName) throws Exception{
        JestResult jr = jestClient.execute(new DeleteIndex.Builder(indexName).build());
        System.out.println(jr.isSucceeded());
    }

    /**
     * 删除单条数据,  这个id必须是主键才能被删除
     * @return
     * @throws Exception
     */
    public void deleteData(String id,String indexName,String typeName)throws Exception{
        DocumentResult dr = jestClient.execute(new Delete.Builder(id).index(indexName).type(typeName).build());
        System.out.println(dr.isSucceeded());
    }
}