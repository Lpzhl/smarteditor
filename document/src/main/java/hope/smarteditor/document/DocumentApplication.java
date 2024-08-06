package hope.smarteditor.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@MapperScan("hope.smarteditor.document.mapper")
@EnableCaching//开发缓存注解功能
@EnableDubbo
@EnableTransactionManagement
@ComponentScan(basePackages = {"hope.smarteditor.common"})  //解决common包下类无法注入的问题 比如全局异常处理失效
public class DocumentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);

    }

}
