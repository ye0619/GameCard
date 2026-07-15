package com.gamecard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * GameCard 游戏卡片生成平台 - 启动类
 *
 * @author GameCard Team
 */
@SpringBootApplication
@EnableConfigurationProperties
public class GameCardApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameCardApplication.class, args);
    }

}
