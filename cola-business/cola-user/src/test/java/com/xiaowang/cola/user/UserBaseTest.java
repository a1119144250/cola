package com.xiaowang.cola.user;

import com.alicp.jetcache.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ColaUserApplication.class })
@ActiveProfiles("test")
public class UserBaseTest {

  @MockBean
  private RedissonClient redissonClient;

  @MockBean
  private CacheManager cacheManager;

  @Test
  public void test() {

  }
}
