package cn.cuptec.faros.util;

import cn.cuptec.faros.config.oss.OssProperties;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import com.aliyun.oss.OSS;

import java.io.*;
import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
public class AliOssUtils {
    private final OssProperties storageConfig;

    /**
     * 上传文件
     *
     * @param file
     * @param dir  用户上传文件时指定的文件夹。
     */
    public String uploadFile(File file, String dir, String fileName1) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(storageConfig.getEndpoint(), storageConfig.getAccessKeyId(), storageConfig.getAccessKeySecret());

        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String key = dir + fileName1 + "." + suffix;
        // 创建PutObjectRequest对象。
        PutObjectRequest putObjectRequest = new PutObjectRequest(storageConfig.getBucket(), key, file);
        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);
        ossClient.deleteObject(storageConfig.getBucket(), key);
        // 上传文件。
        ossClient.putObject(putObjectRequest);
        // 关闭OSSClient。
        ossClient.shutdown();
        // 解析结果
        String resultStr = "https://" + storageConfig.getBucket() + "." + storageConfig.getEndpoint() + "/" + key;
        return resultStr;
    }
    /**

     */
    public OSS getOssClient() {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(storageConfig.getEndpoint(), storageConfig.getAccessKeyId(), storageConfig.getAccessKeySecret());



        return ossClient;
    }

    /**
     * 删除文件
     *
     * @param dir
     * @return
     */
    public Boolean deleteFile(String dir) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(storageConfig.getEndpoint(), storageConfig.getAccessKeyId(), storageConfig.getAccessKeySecret());
        ossClient.deleteObject(storageConfig.getBucket(), dir);
        return true;
    }

    public static void main(String[] args) {
        String url="https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/log/1476029671717556224_WALKER_20211225.xlog";
        System.out.println(url.split("/")[3] + "/" + url.split("/")[4]);
    }
    /**
     * 获取文件流
     * @return
     */
    public byte[] getFileByte(String filePath) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(storageConfig.getEndpoint(), storageConfig.getAccessKeyId(), storageConfig.getAccessKeySecret());

        byte[] oss_byte = getOssFileByteArray("log/44.xlog",ossClient);
//
//        String s = byteArrayToStr(oss_byte);
//        System.out.println(s);


        return oss_byte;
    }
    public static String byteArrayToStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray);
        return str;
    }
    /**
     * 获取oss文件byte[]
     */
    public byte[] getOssFileByteArray(String filepath,OSS ossClient) {
        byte[] result = null;

        try {
            if (filepath != null && !"".equals(filepath.trim())) {

                // 上传
                OSSObject ossObj = ossClient.getObject(storageConfig.getBucket(), filepath);
                if (ossObj != null) {
                    InputStream is = ossObj.getObjectContent();
                    result = InputStreamToByteArray(is);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件下载异常");
        } finally {
            // 关闭client
            ossClient.shutdown();
        }
        return result;
    }

    public static byte[] InputStreamToByteArray(InputStream is) {
        // 1.创建源与目的的
        byte[] dest = null;// 在字节数组输出的时候是不需要源的。
        // 2.选择流，选择文件输入流
        ByteArrayOutputStream os = null;// 新增方法
        try {
            os = new ByteArrayOutputStream();
            // 3.操作,读文件
            byte[] flush = new byte[1024 * 10];// 10k，创建读取数据时的缓冲，每次读取的字节个数。
            int len = -1;// 接受长度；
            while ((len = is.read(flush)) != -1) {
                // 表示当还没有到文件的末尾时
                // 字符数组-->字符串，即是解码。
                os.write(flush, 0, len);// 将文件内容写出字节数组
            }
            os.flush();
            return os.toByteArray();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // 4.释放资源
            try {
                if (is != null) {// 表示当文打开时，才需要通知操作系统关闭
                    is.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;

    }


}
