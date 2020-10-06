package com.kgc.pro.proservice.service;

import com.alibaba.fastjson.JSON;
import com.kgc.pro.bean.ProjectInfo;
import com.kgc.pro.proservice.mapper.ProjectInfoMapper;
import com.kgc.pro.service.ProjectInfoService;
import com.kgc.pro.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.dubbo.config.annotation.Service;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {
    @Resource
    ProjectInfoMapper projectInfoMapper;
    @Resource
    JestClient jestClient;
    @Resource
    RedissonClient redissonClient;
    @Resource
    RedisUtil redisUtil;
    
    @Override
    public List<ProjectInfo> PROJECT_INFO_LIST(Integer status) {
        List<ProjectInfo> list=new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("status",status);
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        String dsl=searchSourceBuilder.toString();
        Search search=new Search.Builder(dsl).addIndex("project").addType("projectinfo").build();
        try {
            SearchResult searchResult=jestClient.execute(search);
            List<SearchResult.Hit<ProjectInfo,Void>> hits=searchResult.getHits(ProjectInfo.class);
            for (SearchResult.Hit<ProjectInfo,Void> hit: hits){
                list.add(hit.source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ProjectInfo> PROJECT_INFOS_PAGE(Integer status, int pageNum, int pageSize) {
        List<ProjectInfo> list=new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("status",status);
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.from((pageNum-1)*pageSize);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.query(boolQueryBuilder);
        String dsl=searchSourceBuilder.toString();
        Search search=new Search.Builder(dsl).addIndex("project").addType("projectinfo").build();
        try {
            SearchResult searchResult=jestClient.execute(search);
            List<SearchResult.Hit<ProjectInfo,Void>> hits=searchResult.getHits(ProjectInfo.class);
            for (SearchResult.Hit<ProjectInfo,Void> hit: hits){
                list.add(hit.source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ProjectInfo PROJECT_INFO_BY_ID(int ID) {
        String prokey="pro:"+ID+":info";

        Jedis jedis=redisUtil.getJedis();

        String proInfoJSOn=jedis.get(prokey);

        ProjectInfo projectInfo=null;

        if(proInfoJSOn!=null){//redis有缓存
            if(proInfoJSOn.equals("empty")){
                return null;
            }
            projectInfo= JSON.parseObject(jedis.get(prokey),ProjectInfo.class);
        }else{//无缓存

            Lock lock = redissonClient.getLock("lock");// 声明锁
            lock.lock();//上锁
            //查询
            projectInfo = projectInfoMapper.selectByPrimaryKey(ID);

            if(projectInfo!=null){
                //随机时间，防止缓存雪崩
                Random random=new Random();
                int i = random.nextInt(10);
                jedis.setex(prokey,i*60*10,JSON.toJSONString(projectInfo));
            }else{
                //空数据，存储5分钟，防止缓存穿透
                jedis.setex(prokey,5*6*1,"empty");
            }
            // 删除分布式锁
            lock.unlock();
        }
        jedis.close();

        return projectInfo;
    }

    @Override
    public int UPDATE_PROJECT(ProjectInfo projectInfo) {
        int result=projectInfoMapper.updateByPrimaryKeySelective(projectInfo);
        if(result>0){
            String prokey="pro:"+projectInfo.getId()+":info";
            Jedis jedis=redisUtil.getJedis();
            String proInfoJSOn=jedis.get(prokey);
            jedis.del(prokey);
            //随机时间，防止缓存雪崩
            Random random=new Random();
            int i = random.nextInt(10);
            jedis.setex(prokey,i*60*10,JSON.toJSONString(projectInfo));
            
        }
        return 0;
    }


    public void setEs(){
        List<ProjectInfo> allpro = projectInfoMapper.selectByExample(null);
        System.out.println("projectinfolist:"+allpro);
        List<ProjectInfo> proInfos=new ArrayList<>();
        for (ProjectInfo pro : allpro) {
            ProjectInfo projectInfo = new ProjectInfo();
            BeanUtils.copyProperties(pro,projectInfo);
            proInfos.add(projectInfo);
        }
        System.out.println(proInfos);
        for (ProjectInfo pro : proInfos) {
            Index index=new Index.Builder(pro).index("project").type("projectinfo").id(pro.getId()+"").build();
            try {
                jestClient.execute(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
