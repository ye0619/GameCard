package com.gamecard.ai;

/**
 * 文本生成服务接口（预留）
 *
 * @author GameCard Team
 */
public interface TextService {

    /**
     * 生成卡片文案
     *
     * @param input 用户输入信息
     * @return 生成的文案
     */
    String generateCardText(String input);

}
