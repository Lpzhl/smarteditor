package hope.smarteditor.api;


import hope.smarteditor.common.model.entity.Document;

import java.util.List;

/**
 * author lzh
 */
public interface UserDubboService  {

    /**
     * 根据用户id获取用户名
     */
    String getUserNameByUserId(Long userId);

}