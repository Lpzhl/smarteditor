package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.OrderBuyPointsDTO;
import hope.smarteditor.common.model.dto.OrderBuyVipDTO;
import hope.smarteditor.common.model.entity.Orders;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.service.OrdersService;
import hope.smarteditor.user.mapper.OrdersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
* @author LoveF
* @description 针对表【orders】的数据库操作Service实现
* @createDate 2024-07-10 01:39:47
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    public Result createOrderBuyPoints(OrderBuyPointsDTO orderBuyPointsDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderBuyPointsDTO, orders);
        orders.setOrderTime(new Date());
        orders.setStatus("待支付");
        // 创建 DecimalFormat 对象，指定格式为保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        // 设置 orders 对象的 amount 属性为两位小数
        orders.setAmount(Double.parseDouble(decimalFormat.format(orderBuyPointsDTO.getAmount() * 0.1)));

        // 将订单信息存入Redis，设置过期时间为5分钟
        ordersMapper.insert(orders);
        long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
        stringRedisTemplate.opsForZSet().add("orders", orders.getId().toString(), expireAt);
        return Result.success(orders);
    }

    @Override
    public Result getUserAllOrder(String userId) {
        QueryWrapper<Orders> ordersQueryWrapper = new QueryWrapper<>();
        ordersQueryWrapper.eq("user_id", userId);
        return Result.success(ordersMapper.selectList(ordersQueryWrapper));
    }

    @Override
    public Result cancelOrder(String orderId) {
        ordersMapper.update(null, new UpdateWrapper<Orders>().eq("id", orderId).set("status", "已取消"));
        return Result.success("成功");
    }

    @Override
    public Result createOrderBuyVip(OrderBuyVipDTO orderBuyVipDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderBuyVipDTO, orders);
        orders.setOrderTime(new Date());
        orders.setStatus("待支付");
        ordersMapper.insert(orders);
        // 将订单信息存入Redis，设置过期时间为5分钟
        ordersMapper.insert(orders);
        long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
        stringRedisTemplate.opsForZSet().add("orders", orders.getId().toString(), expireAt);
        return Result.success("成功");
    }

    @Override
    public void updateOrderStatus(int parseInt, String status) {
        QueryWrapper<Orders> ordersQueryWrapper = new QueryWrapper<>();
        ordersQueryWrapper.eq("id", parseInt);
        ordersMapper.update(null, new UpdateWrapper<Orders>().eq("id", parseInt).set("status", status));
    }

    /**
     * 自动更新订单是否过期
     *
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void processExpiredBookings() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredBookings = stringRedisTemplate.opsForZSet().rangeByScore("orders", 0, currentTime);
        System.out.println("========================");
        for (String orderId : expiredBookings) {
            // 开始事务
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                // 查询并更新订单状态为“已过期”

                Orders orders = ordersMapper.selectById(orderId);
                orders.setStatus("已过期");

                UpdateWrapper<Orders> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", orderId);
                update(orders, updateWrapper);
                System.out.println("==========自动更新订单状态执行==============");

                // 从Redis中移除
                stringRedisTemplate.opsForZSet().remove("orders", orderId);

                // 提交事务
                transactionManager.commit(status);
            } catch (Exception e) {
                // 回滚事务
                transactionManager.rollback(status);
                // 记录错误或采取其他补救措施
            }
        }
    }


}




