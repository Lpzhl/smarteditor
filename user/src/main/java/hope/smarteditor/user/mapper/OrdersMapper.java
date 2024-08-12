package hope.smarteditor.user.mapper;

import hope.smarteditor.common.model.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【orders】的数据库操作Mapper
* @createDate 2024-07-10 01:39:47
* @Entity hope.smarteditor.common.model.entity.Orders
*/
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT * FROM orders WHERE status = '已支付' AND is_deleted = 0")
    List<Orders> getPaidOrders();

}




