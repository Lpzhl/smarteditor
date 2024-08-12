package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.OrderBuyPointsDTO;
import hope.smarteditor.common.model.dto.OrderBuyVipDTO;
import hope.smarteditor.common.model.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.TotalSalesVO;
import hope.smarteditor.common.model.vo.UnpaidOrdersVO;
import hope.smarteditor.common.result.Result;

import java.util.List;

/**
* @author LoveF
* @description 针对表【orders】的数据库操作Service
* @createDate 2024-07-10 01:39:47
*/
public interface OrdersService extends IService<Orders> {

    Result createOrderBuyPoints(OrderBuyPointsDTO orderBuyPointsDTO);

    Result getUserAllOrder(String userId);

    Result cancelOrder(String orderId);

    Result createOrderBuyVip(OrderBuyVipDTO orderBuyVipDTO);

    void updateOrderStatus(int parseInt, String status);

    boolean deleteOrder(String orderId);

    List<TotalSalesVO> getScoreAndSales();

    List<UnpaidOrdersVO> getUnpaidOrders();

}

