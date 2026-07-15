package com.gamecard.export;

/**
 * 导出服务接口 - 负责卡片导出（PNG / PDF）
 *
 * @author GameCard Team
 */
public interface ExportService {

    /**
     * 导出为 PNG
     *
     * @param cardId 卡片 ID
     * @return PNG 文件路径
     */
    String exportPng(String cardId);

    /**
     * 导出为 PDF
     *
     * @param cardId 卡片 ID
     * @return PDF 文件路径
     */
    String exportPdf(String cardId);

}
