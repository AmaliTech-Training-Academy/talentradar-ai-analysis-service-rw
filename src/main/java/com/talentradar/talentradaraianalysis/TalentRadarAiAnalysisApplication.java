package com.talentradar.talentradaraianalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TalentRadarAiAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentRadarAiAnalysisApplication.class, args);
    }

}
