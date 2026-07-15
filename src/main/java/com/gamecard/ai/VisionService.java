package com.gamecard.ai;

/**
 * 视觉识别服务接口（预留）
 *
 * @author GameCard Team
 */
public interface VisionService {

    /**
     * 识别图片内容
     *
     * @param imageUrl 图片地址
     * @return 识别结果描述
     */
    String analyzeImage(String imageUrl);

}
