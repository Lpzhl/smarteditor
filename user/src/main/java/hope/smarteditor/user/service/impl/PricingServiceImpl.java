package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.PriceUpdateDTO;
import hope.smarteditor.common.model.entity.Pricing;
import hope.smarteditor.common.model.entity.PricingHistory;
import hope.smarteditor.user.mapper.PricingHistoryMapper;
import hope.smarteditor.user.service.PricingService;
import hope.smarteditor.user.mapper.PricingMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
* @author LoveF
* @description 针对表【Pricing】的数据库操作Service实现
* @createDate 2024-08-10 19:12:54
*/
@Service
public class PricingServiceImpl extends ServiceImpl<PricingMapper, Pricing>
    implements PricingService{

    @Resource
    private PricingMapper pricingMapper;

    @Resource
    private PricingHistoryMapper pricingHistoryMapper;

    @Override
    public List<Pricing> getPriceTable() {
        return pricingMapper.selectList(null);
    }

    @Override
    public List<Pricing> getDeletedPriceTable() {
        return pricingMapper.selectDeletedList(null);
    }

    @Override
    public void managePriceTable(PriceUpdateDTO priceUpdateDTO) {
        // 获取当前价格记录
        Pricing pricing = pricingMapper.selectById(priceUpdateDTO.getId());
        if (pricing == null) {
            throw new RuntimeException("价格记录不存在");
        }

        // 记录历史价格
        PricingHistory pricingHistory = new PricingHistory();
        pricingHistory.setPricingId(pricing.getId());
        pricingHistory.setOldPrice(pricing.getPrice());
        pricingHistory.setNewPrice(priceUpdateDTO.getPrice());
        pricingHistory.setChangedAt(new Date());
        pricingHistory.setChangedBy(pricingHistory.getChangedBy());
        pricingHistory.setOldValue(pricing.getItemValue());
        pricingHistory.setNewValue(priceUpdateDTO.getNewValue());
        pricingHistory.setDescribe("价格更新");
        pricingHistoryMapper.insert(pricingHistory);

        // 更新当前价格表
        pricing.setPrice(priceUpdateDTO.getPrice());
        pricing.setItemValue(priceUpdateDTO.getNewValue());
        pricing.setUpdatedAt(new Date());
        pricingMapper.updateById(pricing);
    }

    @Override
    public void deletePriceTable(Long id,Long userId) {
        pricingMapper.deleteById(id);
        PricingHistory pricingHistory = new PricingHistory();
        pricingHistory.setPricingId(id);
        pricingHistory.setChangedAt(new Date());
        pricingHistory.setChangedBy(userId);
        pricingHistory.setDescribe("价格删除");
    }

    @Override
    public List<PricingHistory> getPriceTableLog() {
        return pricingHistoryMapper.selectList(null);
    }
}




