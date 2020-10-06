package com.kgc.pro.proweb.conntroller;

import com.kgc.pro.bean.ProjectInfo;
import com.kgc.pro.service.ProjectInfoService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProController {
    @Reference
    ProjectInfoService projectInfoService;

    @RequestMapping("/pro/list")
    @ResponseBody
    public Map<String,Object> ProjectList(@RequestParam(value = "status",required = false,defaultValue = "3")Integer status,
                                          @RequestParam(value = "pageNum",required = false,defaultValue = "1")Integer pageNum,
                                          @RequestParam(value = "pageSize",required = false,defaultValue = "3")Integer pageSize){
        Map<String,Object> map=new HashMap<>();

        List<ProjectInfo> projectInfos =projectInfoService.PROJECT_INFO_LIST(status);
        if(projectInfos.size()==0){
            map.put("pages",0);
            map.put("prePage",0);
            map.put("nextPage",0);
            map.put("count",0);
            map.put("navigatepageNums",null);
            map.put("hasPrePage",0);
            map.put("hasNextPage",0);
            map.put("pageinfo",null);
            return map;
        }
        Integer count=projectInfos.size();
        Integer pages=count%pageSize==0?count/pageSize:count/pageSize+1;
        if(pageNum<1){
            pageNum=1;
        }
        if(pageNum>pages){
            pageNum=pages;
        }
        List<ProjectInfo> list=projectInfoService.PROJECT_INFOS_PAGE(status,pageNum,pageSize);
        System.out.println(list);
        map.put("pages",pages);
        map.put("prePage",pageNum-1);
        map.put("nextPage",pageNum+1);
        map.put("count",count);
        List<Integer> navigatepageNums=new ArrayList<>();
        for (int i=1;i<=pages;i++){
            navigatepageNums.add(i);
        }
        map.put("navigatepageNums",navigatepageNums);
        map.put("hasPrePage",pageNum>1);
        map.put("hasNextPage",pageNum<pages);
        map.put("pageinfo",list);
        return map;
    }

    @RequestMapping("/pro/by/id")
    @ResponseBody
    public ProjectInfo proByid(int id){
        ProjectInfo projectInfo = projectInfoService.PROJECT_INFO_BY_ID(id);
        return projectInfo;
    }

    @RequestMapping("/save")
    @ResponseBody
    public int save(@RequestBody ProjectInfo projectInfo){
        int i = projectInfoService.UPDATE_PROJECT(projectInfo);
        return i;
    }
}
