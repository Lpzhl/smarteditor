package hope.smarteditor.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public void setLike(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, 10, TimeUnit.DAYS);
    }

    public Object getLike(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteLike(String key) {
        redisTemplate.delete(key);
    }
}
