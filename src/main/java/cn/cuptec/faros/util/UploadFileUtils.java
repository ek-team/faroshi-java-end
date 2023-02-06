package cn.cuptec.faros.util;

import cn.cuptec.faros.config.oss.OssProperties;
import com.aliyun.oss.OSS;

import java.io.File;

/**
 * @JL 文件上传工具类
 */
public class UploadFileUtils {

    /**
     * 上传文件
     *
     * @param file
     * @param dir  用户上传文件时指定的文件夹。
     */
    public static String uploadFile(File file, String dir, OssProperties ossProperties, String fileName) throws Exception {

        AliOssUtils aliOssUtils = new AliOssUtils(ossProperties);
        return aliOssUtils.uploadFile(file, dir, fileName);

    }
    public static OSS getOssClient( OssProperties ossProperties) throws Exception {

        AliOssUtils aliOssUtils = new AliOssUtils(ossProperties);
        return aliOssUtils.getOssClient();

    }

    /**
     * 删除文件
     *
     * @param dir 用户上传文件时指定的文件夹。
     */
    public static Boolean deleteFile(String dir, OssProperties ossProperties) throws Exception {

        AliOssUtils aliOssUtils = new AliOssUtils(ossProperties);
        return aliOssUtils.deleteFile(dir);

    }

    /**
     * 获取文件流
     */
    public static byte[] getFileByte(String filePath, OssProperties ossProperties) throws Exception {
        AliOssUtils aliOssUtils = new AliOssUtils(ossProperties);
        return aliOssUtils.getFileByte(filePath);

    }
}
