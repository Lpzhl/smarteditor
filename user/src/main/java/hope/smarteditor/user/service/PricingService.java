package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.PriceUpdateDTO;
import hope.smarteditor.common.model.entity.Pricing;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.entity.PricingHistory;

import java.util.List;

/**
* @author LoveF
* @description 针对表【Pricing】的数据库操作Service
* @createDate 2024-08-10 19:12:54
*/
public interface PricingService extends IService<Pricing> {

    List<Pricing> getPriceTable();

    List<Pricing> getDeletedPriceTable();

    void managePriceTable(PriceUpdateDTO priceUpdateDTO);

    void deletePriceTable(Long id,Long userId);

    List<PricingHistory> getPriceTableLog();

}
