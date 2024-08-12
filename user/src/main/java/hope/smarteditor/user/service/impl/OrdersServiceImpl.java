package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.OrderBuyPointsDTO;
import hope.smarteditor.common.model.dto.OrderBuyVipDTO;
import hope.smarteditor.common.model.entity.Orders;
import hope.smarteditor.common.model.vo.OrdersVO;
import hope.smarteditor.common.model.vo.TotalSalesVO;
import hope.smarteditor.common.model.vo.UnpaidOrdersVO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        stringRedisTemplate.opsForZSet().add("orders", orders.getId().toString(), expireAt);
        return Result.success(orders);
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
        long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        stringRedisTemplate.opsForZSet().add("orders", orders.getId().toString(), expireAt);
        return Result.success("成功");
    }
    @Override
    public Result getUserAllOrder(String userId) {
        QueryWrapper<Orders> ordersQueryWrapper = new QueryWrapper<>();
        ordersQueryWrapper.eq("user_id", userId);
        List<Orders> orders = ordersMapper.selectList(ordersQueryWrapper);
        List<OrdersVO> ordersVOList = new ArrayList<>();

        for (Orders order : orders) {
            OrdersVO ordersVO = new OrdersVO();
            ordersVO.setId(order.getId());
            ordersVO.setUserId(order.getUserId());
            ordersVO.setOrderType(order.getOrderType());
            ordersVO.setAmount(order.getAmount());
            ordersVO.setOrderTime(order.getOrderTime());
            ordersVO.setStatus(order.getStatus());
            ordersVO.setNum(order.getNum());

            // 设置描述
            if ("购买会员".equals(order.getOrderType())) {
                ordersVO.setDescription("购买了 " + order.getNum() + " 个月会员");
            } else if ("购买积分".equals(order.getOrderType())) {
                ordersVO.setDescription("增加了 " + order.getNum() * 10 + " 积分");
            }

            // 设置过期时间加5分钟
            LocalDateTime endOrderTime = order.getOrderTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusMinutes(5);
            ordersVO.setEndOrderTime(endOrderTime);

            // 添加到列表
            ordersVOList.add(ordersVO);
        }

        // 对订单列表按照订单时间降序排序
        ordersVOList.sort(Comparator.comparing(OrdersVO::getOrderTime).reversed());

        // 返回处理后的订单列表
        return Result.success(ordersVOList);
    }


    @Override
    public void updateOrderStatus(int parseInt, String status) {
        QueryWrapper<Orders> ordersQueryWrapper = new QueryWrapper<>();
        ordersQueryWrapper.eq("id", parseInt);
        ordersMapper.update(null, new UpdateWrapper<Orders>().eq("id", parseInt).set("status", status));
    }

    @Override
    public boolean deleteOrder(String orderId) {
        try {
            Orders order = ordersMapper.selectById(orderId);
            if (order == null) {
                return false;
            }

            int rows = ordersMapper.deleteById(orderId);
            if (rows > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<TotalSalesVO> getScoreAndSales() {
        List<Orders> paidOrders = ordersMapper.getPaidOrders();

        double pointsSalesLast7Days = 0;
        double membershipSalesLast7Days = 0;
        double pointsSalesToday = 0;
        double membershipSalesToday = 0;
        double pointsSalesLastMonth = 0;
        double membershipSalesLastMonth = 0;
        double pointsSalesTotal = 0;
        double membershipSalesTotal = 0;

        int pointsOrdersLast7Days = 0;
        int membershipOrdersLast7Days = 0;
        int pointsOrdersToday = 0;
        int membershipOrdersToday = 0;
        int pointsOrdersLastMonth = 0;
        int membershipOrdersLastMonth = 0;
        int pointsOrdersTotal = 0;
        int membershipOrdersTotal = 0;

        LocalDateTime now = LocalDateTime.now();

        for (Orders order : paidOrders) {
            LocalDateTime orderTime = order.getOrderTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            if ("购买积分".equals(order.getOrderType())) {
                pointsSalesTotal += order.getAmount();
                pointsOrdersTotal++; // 统计总的积分订单数量

                if (orderTime.isAfter(now.minusDays(7))) {
                    pointsSalesLast7Days += order.getAmount();
                    pointsOrdersLast7Days++; // 统计近七天的积分订单数量
                }
                if (orderTime.toLocalDate().equals(now.toLocalDate())) {
                    pointsSalesToday += order.getAmount();
                    pointsOrdersToday++; // 统计今天的积分订单数量
                }
                if (orderTime.isAfter(now.minusDays(30))) {
                    pointsSalesLastMonth += order.getAmount();
                    pointsOrdersLastMonth++; // 统计近一个月的积分订单数量
                }
            } else if ("购买会员".equals(order.getOrderType())) {
                membershipSalesTotal += order.getAmount();
                membershipOrdersTotal++; // 统计总的会员订单数量

                if (orderTime.isAfter(now.minusDays(7))) {
                    membershipSalesLast7Days += order.getAmount();
                    membershipOrdersLast7Days++; // 统计近七天的会员订单数量
                }
                if (orderTime.toLocalDate().equals(now.toLocalDate())) {
                    membershipSalesToday += order.getAmount();
                    membershipOrdersToday++; // 统计今天的会员订单数量
                }
                if (orderTime.isAfter(now.minusDays(30))) {
                    membershipSalesLastMonth += order.getAmount();
                    membershipOrdersLastMonth++; // 统计近一个月的会员订单数量
                }
            }
        }

        List<TotalSalesVO> totalSalesVOList = new ArrayList<>();

        // 设置近七天的销售额和订单数量
        TotalSalesVO last7DaysVO = new TotalSalesVO();
        last7DaysVO.setTimeCategory("近七天");
        last7DaysVO.setPointsSales(roundToTwoDecimal(pointsSalesLast7Days));
        last7DaysVO.setMembershipSales(roundToTwoDecimal(membershipSalesLast7Days));
        last7DaysVO.setTotalSales(roundToTwoDecimal(pointsSalesLast7Days + membershipSalesLast7Days));
        last7DaysVO.setPointsOrderCount(pointsOrdersLast7Days); // 近七天积分订单数量
        last7DaysVO.setMembershipOrderCount(membershipOrdersLast7Days); // 近七天会员订单数量
        totalSalesVOList.add(last7DaysVO);

        // 设置今天的销售额和订单数量
        TotalSalesVO todayVO = new TotalSalesVO();
        todayVO.setTimeCategory("今天");
        todayVO.setPointsSales(roundToTwoDecimal(pointsSalesToday));
        todayVO.setMembershipSales(roundToTwoDecimal(membershipSalesToday));
        todayVO.setTotalSales(roundToTwoDecimal(pointsSalesToday + membershipSalesToday));
        todayVO.setPointsOrderCount(pointsOrdersToday); // 今天积分订单数量
        todayVO.setMembershipOrderCount(membershipOrdersToday); // 今天会员订单数量
        totalSalesVOList.add(todayVO);

        // 设置一个月的销售额和订单数量
        TotalSalesVO lastMonthVO = new TotalSalesVO();
        lastMonthVO.setTimeCategory("一个月");
        lastMonthVO.setPointsSales(roundToTwoDecimal(pointsSalesLastMonth));
        lastMonthVO.setMembershipSales(roundToTwoDecimal(membershipSalesLastMonth));
        lastMonthVO.setTotalSales(roundToTwoDecimal(pointsSalesLastMonth + membershipSalesLastMonth));
        lastMonthVO.setPointsOrderCount(pointsOrdersLastMonth); // 近一个月积分订单数量
        lastMonthVO.setMembershipOrderCount(membershipOrdersLastMonth); // 近一个月会员订单数量
        totalSalesVOList.add(lastMonthVO);

        // 设置总销售额和订单数量
        TotalSalesVO totalVO = new TotalSalesVO();
        totalVO.setTimeCategory("总共");
        totalVO.setPointsSales(roundToTwoDecimal(pointsSalesTotal));
        totalVO.setMembershipSales(roundToTwoDecimal(membershipSalesTotal));
        totalVO.setTotalSales(roundToTwoDecimal(pointsSalesTotal + membershipSalesTotal));
        totalVO.setPointsOrderCount(pointsOrdersTotal); // 总积分订单数量
        totalVO.setMembershipOrderCount(membershipOrdersTotal); // 总会员订单数量
        totalSalesVOList.add(totalVO);

        return totalSalesVOList;
    }

    @Override
    public List<UnpaidOrdersVO> getUnpaidOrders() {
        // 查询未支付的订单
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "待支付");
        List<Orders> ordersList = ordersMapper.selectList(queryWrapper);

        // 根据订单类型进行分类，并统计未支付订单的数量和总金额
        Map<String, UnpaidOrdersVO> unpaidOrdersMap = new HashMap<>();

        for (Orders order : ordersList) {
            String type = order.getOrderType();

            UnpaidOrdersVO vo = unpaidOrdersMap.getOrDefault(type, new UnpaidOrdersVO());
            vo.setType(type);
            vo.setUnpaidOrderCount(vo.getUnpaidOrderCount() + 1);
            vo.setUnpaidOrderAmount(vo.getUnpaidOrderAmount() + order.getAmount());
            if (vo.getUnpaidOrders() == null) {
                vo.setUnpaidOrders(new ArrayList<>());
            }
            vo.getUnpaidOrders().add(order);

            unpaidOrdersMap.put(type, vo);
        }

        // 将结果转换为 List 返回
        return new ArrayList<>(unpaidOrdersMap.values());
    }


    private double roundToTwoDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }




    /**
     * 自动更新订单是否过期
     *
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void processExpiredBookings() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredBookings = stringRedisTemplate.opsForZSet().rangeByScore("orders", 0, currentTime);
        System.out.println("===========轮询订单是否过期=============");
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




