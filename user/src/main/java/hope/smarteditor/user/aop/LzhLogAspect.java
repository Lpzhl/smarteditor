package hope.smarteditor.user.aop;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一日志切面
 * @author lzh
 */

@Component
@Aspect
@Slf4j
public class LzhLogAspect {

    @Pointcut("@annotation(hope.smarteditor.user.annotation.LzhLog)")
    public void LzhLogAspect(){}

    @Before("LzhLogAspect()")
    public void beforePkhLog(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();

            String methodName = joinPoint.getSignature().getName();
            log.info("===================== Method " + methodName + "() begin==============");
            // 执行时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = new Date();
            String time = sdf.format(d);
            log.info("时间           : " + time);
            // 打印请求 URL
            log.info("请求 URL            : " + request.getRequestURL());
            // 打印请求方法
            log.info("请求方法   : " + request.getMethod());
            // 打印controller的全路径以及执行方法
            log.info("controller的全路径以及执行方法  : " + joinPoint.getSignature().getDeclaringTypeName() + "." + methodName);
            // 打印请求的 IP
            log.info("请求的 IP             : " + request.getRemoteHost());
            // 打印请求入参
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                log.info("请求入参    : " + arg);
            }
            log.info("执行控制器...");
        } else {
            log.warn("无法检索请求属性");
        }
    }

    @After("LzhLogAspect()")
    public void afterPkhLog(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("======================== Method " + methodName + "() End ==========================");
    }
}

