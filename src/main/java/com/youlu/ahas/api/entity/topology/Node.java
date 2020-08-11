package com.youlu.ahas.api.entity.topology;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  节点展示配置
 */
@Getter
@Setter
public class Node implements Serializable {

//    private String id;
//    private String name;
//    private String type;
//    private String top;
//    private String left;
//    private Object data;
//    @JsonAlias("Class")
//    private String className; // 前端自定义类

    private String id;
    private String name;
    private Integer deviceType;
    private String nameMinor;
    private Double cpuUtil;
    private Double memUtil;
    private String hostConfigurationId;
}
