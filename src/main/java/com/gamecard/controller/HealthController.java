package com.gamecard.controller;

import com.gamecard.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口
 *
 * @author GameCard Team
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查 - 验证项目启动成功
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("GameCard Backend Running");
    }

}
