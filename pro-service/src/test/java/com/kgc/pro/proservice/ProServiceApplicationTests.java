package com.kgc.pro.proservice;

import com.kgc.pro.bean.ProjectInfo;
import com.kgc.pro.proservice.mapper.ProjectInfoMapper;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ProServiceApplicationTests {
    @Resource
    ProjectInfoMapper projectInfoMapper;
    @Resource
    JestClient jestClient;
    @Test
    void contextLoads() {
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
