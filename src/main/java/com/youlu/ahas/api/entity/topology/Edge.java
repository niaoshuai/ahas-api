package com.youlu.ahas.api.entity.topology;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  连线配置
 */
@Getter
@Setter
public class Edge implements Serializable {

    private String source; // 源点
    private String target; // 目标点
    private Boolean arrow;  // 是否带有箭头
    private String style;   // 前端样式
    private String pid;
}
