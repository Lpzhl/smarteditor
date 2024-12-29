package hope.smarteditor.common.utils;

import java.util.UUID;

public class OrderUtil {

    public static String generateOrderId() {
        // 生成UUID并去掉其中的'-'
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 返回前14位作为订单号，保证长度不超过32位（UUID长度）
        return uuid.substring(0, 7);
    }

}
