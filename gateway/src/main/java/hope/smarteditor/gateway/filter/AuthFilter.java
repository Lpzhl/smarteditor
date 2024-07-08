
package hope.smarteditor.gateway.filter;

import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.gateway.config.RedisService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RedisService redisService;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    // 每次调用AI接口扣除的金额
    private static final int AI_CALL_DEDUCTION = 10;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取请求路径
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

        // 从请求头中获取令牌和userId
        String token = request.getHeaders().getFirst("Authorization");
        String userIdHeader = request.getHeaders().getFirst("userId");

        if (token == null || userIdHeader == null) {
            // 令牌或userId缺失或格式不正确，返回未授权状态
            return onError(response, HttpStatus.UNAUTHORIZED, "未授权");
        }

        try {
            // 验证令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long tokenUserId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            Long headerUserId = Long.valueOf(userIdHeader);

            System.out.println("tokenUserId = " + tokenUserId);
            System.out.println("headerUserId = " + headerUserId);

            if (!headerUserId.equals(tokenUserId)) {
                // 当前用户ID与token中的用户ID不匹配，返回未授权状态
                return onError(response, HttpStatus.UNAUTHORIZED, "未授权");
            }

            // 从Redis中获取存储的token
            String redisToken = (String) redisService.getLike(tokenUserId.toString());

            if (redisToken == null || !redisToken.equals(token)) {
                // 如果Redis中没有token或token不匹配，返回未授权状态
                return onError(response, HttpStatus.UNAUTHORIZED, "token已过期或者未授权");
            }

            // 检查请求路径是否为AI接口
            if (isAiEndpoint(encodedPath)) {
                // 扣除用户费用
                boolean isDeducted = userDubboService.checkAndDeduct(tokenUserId, AI_CALL_DEDUCTION);
                if (!isDeducted) {
                    return onError(response, HttpStatus.FORBIDDEN, "余额不足");
                }
            }

        } catch (Exception ex) {
            // 令牌无效，返回未授权状态
            System.out.println("ex = " + ex.getMessage());
            return onError(response, HttpStatus.UNAUTHORIZED, "token已过期或者未授权");
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

    // 检查请求路径是否为AI接口
    private boolean isAiEndpoint(String requestPath) {
        // 根据你的AI接口路径进行匹配，这里假设所有AI接口都在 /api/ai 路径下
        return requestPath.startsWith("/ai");
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

