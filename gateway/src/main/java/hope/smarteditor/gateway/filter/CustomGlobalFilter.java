package hope.smarteditor.gateway.filter;

import hope.smarteditor.api.UserDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 全局过滤
 * @author lzh
 */


@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    // 每次调用AI接口扣除的金额
    private static final int AI_CALL_DEDUCTION = 10;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        log.info("请求唯一标识：" + request.getId());
        String encodedPath  = request.getPath().toString();
        try {
            // 解码 URL
            String decodedPath = URLDecoder.decode(encodedPath, "UTF-8");
            log.info("原始路径: " + decodedPath);
        } catch (UnsupportedEncodingException e) {
            System.err.println("URL 解码失败: " + e.getMessage());
        }

        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        log.info("请求来源地址：" + request.getRemoteAddress());
        ServerHttpResponse response = exchange.getResponse();

        return handleResponse(exchange, chain);
        //return chain.filter(exchange);

    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            // 1. 请求日志
            ServerHttpRequest request = exchange.getRequest();
            String method = request.getMethod().toString();
            log.info("请求唯一标识：" + request.getId());
            String encodedPath = request.getPath().toString();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("进入响应装饰器...");
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // todo 业务逻辑处理消费ai
                                        try {
                                            // 检查请求路径是否为AI接口
                                            if (isAiEndpoint(encodedPath)) {
                                                // 从请求头中获取userId
                                                String userIdHeader = exchange.getRequest().getHeaders().getFirst("userId");
                                                Long userId = Long.valueOf(userIdHeader);

                                                // 扣除用户费用
                                                boolean isDeducted = userDubboService.deductMoney(userId, AI_CALL_DEDUCTION);
                                                if (!isDeducted) {
                                                    return bufferFactory.wrap("余额不足".getBytes(StandardCharsets.UTF_8));
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;  //重大bug这里不能写  0 现在还没搞清楚
    }


    // 检查请求路径是否为AI接口
    private boolean isAiEndpoint(String requestPath) {
        // 根据你的AI接口路径进行匹配，这里假设所有AI接口都在 /api/ai 路径下
        return requestPath.startsWith("/ai");
    }
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}


