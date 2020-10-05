package com.kgc.pro.service;

import com.kgc.pro.bean.ProjectInfo;

import java.util.List;

public interface ProjectInfoService {
    //根据申报状态查询(用作查询全部信息)
    public List<ProjectInfo> PROJECT_INFO_LIST(Integer status);
    //根据申报状态查询(分页)
    public List<ProjectInfo> PROJECT_INFOS_PAGE(Integer status,int pageNum,int pageSize);
    //根据主键编号查询
    public ProjectInfo PROJECT_INFO_BY_ID(int ID);
    //修改
    public int UPDATE_PROJECT(ProjectInfo projectInfo);
}
