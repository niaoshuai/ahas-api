package com.youlu.ahas.api.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfig {

    @Value("${INFLUX_URL}")
    private String influxUrl;

    /**
     * 容器
     * @return
     */
    @Bean(destroyMethod = "close")
    InfluxDB influxDB(){
        return InfluxDBFactory.connect(influxUrl);
    }
}
