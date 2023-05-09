package cn.cuptec.faros;

import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.im.ChatServer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.MultipartConfigElement;

@Slf4j
@SpringBootApplication
@ComponentScan("cn.cuptec.faros.*")
@EnableTransactionManagement
@EnableScheduling
@AllArgsConstructor
public class FarosApplication implements CommandLineRunner {

    private final Url url;

    public static void main(String[] args) {
        SpringApplication.run(FarosApplication.class, args);
    }

    /**
     * 文件上传配置
     *
     * @return
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        // 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }

    @Override
    public void run(String... args) throws Exception {
        final ChatServer server = new ChatServer(Integer.parseInt(url.getChatServer()));
        //final ChatServer server = new ChatServer(8098);
        server.init();
        server.start();
        // 注册进程钩子，在JVM进程关闭前释放资源
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
                log.warn(">>>>>>>>>> jvm shutdown");
                System.exit(0);
            }
        });
    }
}
