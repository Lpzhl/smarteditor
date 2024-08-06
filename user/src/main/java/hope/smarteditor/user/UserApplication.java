package hope.smarteditor.user;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author lzh
 */

@SpringBootApplication(scanBasePackages={"hope.smarteditor.user*"})
@MapperScan("hope.smarteditor.user.mapper")
@EnableTransactionManagement //开启注解方式的事务管理
@EnableCaching//开发缓存注解功能
@EnableScheduling //开启任务调度
@EnableDiscoveryClient
@EnableDubbo
@ComponentScan(basePackages = {"hope.smarteditor.common", "hope.smarteditor.user"})  //解决common包下类无法注入的问题 比如全局异常处理失效
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}


