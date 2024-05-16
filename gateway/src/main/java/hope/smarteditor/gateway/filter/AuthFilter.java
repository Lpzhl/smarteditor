
package hope.smarteditor.gateway.filter;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import hope.smarteditor.common.constant.JwtClaimsConstant;
import hope.smarteditor.common.properties.JwtProperties;
import hope.smarteditor.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


/**
 *
 * 网关jwt过滤器
*/



@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtProperties jwtProperties;

    // 定义白名单列表
    private static final List<String> WHITE_LIST = Arrays.asList("/user/login", "/user/register");


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        /// 获取请求路径
        String encodedPath = request.getPath().value();


        try {
            // 解码 URL
            String decodedPath = URLDecoder.decode(encodedPath, "UTF-8");
            // 输出转发后的请求地址
            log.info("转发请求 URI: {}", decodedPath);
        } catch (UnsupportedEncodingException e) {
            System.err.println("URL 解码失败: " + e.getMessage());
        }

        // 检查请求路径是否在白名单中
        if (isWhiteListed(encodedPath)) {
            // 如果是白名单中的路径，直接放行
            return chain.filter(exchange);
        }

        // 从请求头中获取令牌
        String token = request.getHeaders().getFirst("Authorization");

        if (token == null) {
            // 令牌缺失或格式不正确，返回未授权状态
            return onError(response, HttpStatus.UNAUTHORIZED, "未授权");
        }


        try {
            // 验证令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        } catch (Exception ex) {
            // 令牌无效，返回未授权状态
            System.out.println("ex = " + ex.getMessage());
            return onError(response, HttpStatus.UNAUTHORIZED, "token已过期");
        }

        // 令牌有效，继续过滤器链
        return chain.filter(exchange);
    }

    // 检查请求路径是否在白名单中
    private boolean isWhiteListed(String requestPath) {
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String whitePath : WHITE_LIST) {
            if (pathMatcher.match(whitePath, requestPath)) {
                return true;
            }
        }
        return false;
    }

    // 创建错误响应体的方法
    private Mono<Void> onError(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // 设置执行顺序为最先执行
    }
}

