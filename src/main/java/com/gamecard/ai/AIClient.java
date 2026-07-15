package com.gamecard.ai;

/**
 * AI 客户端接口 - 统一 AI 调用入口（预留）
 *
 * @author GameCard Team
 */
public interface AIClient {

    /**
     * 调用 AI 文本生成
     *
     * @param prompt 提示词
     * @return AI 响应文本
     */
    String chat(String prompt);

}
