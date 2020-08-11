package com.youlu.ahas.api.controller;

import com.youlu.ahas.api.entity.topology.Edge;
import com.youlu.ahas.api.entity.topology.Node;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youlu.ahas.api.AhasConstans.INFLUXDB_DB;


//@CrossOrigin(value = "http://localhost:4444",allowedHeaders = "Access-Control-Request-Method: GET")
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/data/api")
public class DataApiController {

    @Autowired
    InfluxDB influxDB;

    /**
     * 查询 架构拓扑图
     * @param seconds 秒数 -> UTC
     *
     * https://github.com/alibaba/butterfly
     *
     * const mockData = {
     *   nodes: [{
     *         id: '11',
     *         name: '用户中心',
     *         type: 'black',
     *         top: 100,
     *         left: 20,
     *         data: {},
     *         Class: BaseNode,
     *         },
     *         ],
     *     edges: [{
     *           source: '11',
     *           target: '21',
     *           arrow: true,
     *           style: 'style-red',
     *         },],
     * }
     */
    @GetMapping("/topologyGraph")
    public Map<String,Object> queryTopologyGraph(Long seconds){
        if(seconds == null) {
            seconds =Instant.now().getEpochSecond();
        }

        Map<String,Object> returnData = new HashMap<>(2);

        Instant beginDateTime = Instant.ofEpochSecond(seconds);
        Instant endDateTime = beginDateTime.minusSeconds(10);

        // 查询网络连线
        List<Edge> edges = searchNetStat(beginDateTime,endDateTime);
        returnData.put("edges",edges);
        // 查询进程节点信息
        List<Node> nodeList = searchProcess(beginDateTime,endDateTime,edges);
        returnData.put("nodes",nodeList);

        return returnData;
    }

    private  List<Node>  searchProcess(Instant beginDateTime,Instant endDateTime,List<Edge> edgeList) {
        List<Node> nodeList = new ArrayList<>();

        Node outNetwork = new Node();
        outNetwork.setName("out-network");
        outNetwork.setDeviceType(0);
        outNetwork.setId("out-network");

        nodeList.add(outNetwork);


        StringBuilder sb = new StringBuilder("select * from ahas_processes where time >= '")
                .append(endDateTime)
                .append("' AND time < '").append(beginDateTime).append("'");

        QueryResult queryResult = influxDB.query(new Query( sb.toString(),INFLUXDB_DB));

        if(queryResult.hasError()){
            return null;
        }

        queryResult.getResults().forEach(result -> {
            if (result.hasError() || result.getSeries() == null){
                return ;
            }

            result.getSeries().forEach(series -> {

                int execIndex = series.getColumns().indexOf("exec");
                int cpuIndex = series.getColumns().indexOf("cpu");
                int memIndex = series.getColumns().indexOf("mem");
                int pathIndex = series.getColumns().indexOf("path");
                int pidIndex = series.getColumns().indexOf("pid");

                series.getValues().forEach(values -> {
                    String exec =values.get(execIndex).toString();
//                    String host =values.get(hostIndex).toString();
                    Double cpu =Double.valueOf(values.get(cpuIndex).toString());
                    Double mem =Double.valueOf(values.get(memIndex).toString());
                    String path =values.get(pathIndex).toString();
                    String pid =values.get(pidIndex).toString();

                    if(!StringUtils.isEmpty(path)){

                        //
                        if (edgeList != null && edgeList.size() > 0) {
                            edgeList.forEach(edge -> {
                                if (edge.getPid().equals(pid)){
                                    Node node = new Node();
                                    node.setName(exec);
                                    node.setNameMinor(path);
                                    node.setHostConfigurationId("host");
                                    node.setDeviceType(40);
                                    node.setCpuUtil(cpu);
                                    node.setMemUtil(mem);
                                    node.setId(DigestUtils.md5DigestAsHex(pid.getBytes()));

                                    nodeList.add(node);
                                }
                            });
                        }


                    }
                });
            });
        });
        return nodeList;
    }


    private  List<Edge>  searchNetStat(Instant beginDateTime,Instant endDateTime) {
        List<Edge> edgeList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select * from ahas_netstat where time >= '")
                .append(endDateTime)
                .append("' AND time < '").append(beginDateTime).append("'");

        QueryResult queryResult = influxDB.query(new Query( sb.toString(),INFLUXDB_DB));

        if(queryResult.hasError()){
            return null;
        }

        queryResult.getResults().forEach(result -> {
            if (result.hasError() || result.getSeries() == null){
                return ;
            }

            result.getSeries().forEach(series -> {

                int localAddrIndex = series.getColumns().indexOf("localAddr");
                int remoteIndex = series.getColumns().indexOf("remoteAddr");
                int statusIndex = series.getColumns().indexOf("status");
                int pidIndex = series.getColumns().indexOf("pid");

                series.getValues().forEach(values -> {
                    String localAddr =values.get(localAddrIndex).toString();
                    String remoteAddr =values.get(remoteIndex).toString();
                    String status =values.get(statusIndex).toString();
                    String pidStr = values.get(pidIndex).toString();

                    if(!StringUtils.isEmpty(remoteAddr) && !remoteAddr.equals("0.0.0.0:0") && status.equals("ESTABLISHED")){
                        Edge edge = new Edge();
                        edge.setSource(DigestUtils.md5DigestAsHex(pidStr.getBytes()));
                        edge.setTarget("out-network");
                        edge.setArrow(true);
                        edge.setPid(pidStr);

                        edgeList.add(edge);
                    }
                });
            });
        });
        return edgeList;
    }

}
