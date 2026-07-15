package com.gamecard.upload;

/**
 * 文件上传服务接口 - 负责图片上传、存储、删除
 *
 * @author GameCard Team
 */
public interface UploadService {

    /**
     * 上传图片
     *
     * @param fileBytes 文件字节数组
     * @param fileName  原始文件名
     * @return 文件访问 URL
     */
    String uploadImage(byte[] fileBytes, String fileName);

    /**
     * 删除图片
     *
     * @param fileUrl 文件 URL
     * @return 是否删除成功
     */
    boolean deleteImage(String fileUrl);

}
