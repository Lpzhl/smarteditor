package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class UserStatisticsVO implements Serializable {
    private Long totalUsers;
    private Map<String, Long> dailyNewUsers;
    private List<UserVO> userDetails;
}
