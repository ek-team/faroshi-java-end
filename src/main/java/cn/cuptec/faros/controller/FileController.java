package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.hutool.core.date.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file-upload.path}")
    private String path;

    /**
     * 多图上传
     * @param file
     * @param request
     * @return
     */
    @PostMapping
    public RestResponse upload(@RequestBody MultipartFile[] file,
                               HttpServletRequest request){
        File targetFile=null;
        String msg="";//返回存储路径
        int code=1;
        List imgList=new ArrayList();
        if (file!=null && file.length>0) {
            for (int i = 0; i < file.length; i++) {
                String fileName=file[i].getOriginalFilename();//获取文件名加后缀
                if(fileName!=null&&fileName!=""){
                    String returnUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() +"/file/";//存储路径

                    String fileF = fileName.substring(fileName.lastIndexOf("."), fileName.length());//文件后缀
                    fileName=new Date().getTime()+"_"+new Random().nextInt(1000)+fileF;//新的文件名

                    //先判断文件是否存在
                    String fileAdd = DateUtil.format(new Date(),"yyyyMMdd");
                    File file1 =new File(path+"/"+fileAdd);
                    //如果文件夹不存在则创建
                    if(!file1 .exists()  && !file1 .isDirectory()){
                        file1 .mkdir();
                    }
                    targetFile = new File(file1, fileName);
                    try {
                        file[i].transferTo(targetFile);
                        msg=returnUrl+fileAdd+"/"+fileName;
                        imgList.add(msg);
                    } catch (Exception e) {
                        return RestResponse.failed();
                    }
                }
            }
        }
        return RestResponse.ok(imgList);
    }

    @SneakyThrows
    @GetMapping("/{date}/{fileName}")
    public void getFile(@PathVariable String date, @PathVariable String fileName, HttpServletResponse response){
        File file =new File(path+"/"+ date + "/" + fileName);
        if (file.exists()){
            FileInputStream in = null;
            OutputStream out = null;
            try{
//                response.setHeader("content-disposition", "attachment;filename=new String (fileName.getBytes(\"UTF-8\"),\"iso-8859-1\")");
                response.setContentType("image/png;charset:utf-8");
                in = new FileInputStream(file);
                out = response.getOutputStream();

                // 创建缓冲区
                byte buffer[] = new byte[1024];
                int len = 0;
                // 循环将输入流中的内容读取到缓冲区当中
                while ((len = in.read(buffer)) > 0) {
                    // 输出缓冲区的内容到浏览器，实现文件下载
                    out.write(buffer, 0, len);
                }
            }catch (Exception e){
                log.error("文件下载异常：{}", e.getMessage(), e);
            } finally{
                if (out != null){
                    out.close();
                }
                if (in != null){
                    in.close();
                }
            }

        }
    }

}
