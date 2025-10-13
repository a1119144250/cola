package com.xiaowang.cola.tools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaowang.cola.tools.domain.entity.StockRecord;
import com.xiaowang.cola.tools.domain.resp.StockDeductResponse;
import com.xiaowang.cola.tools.domain.service.StockRecordCleanupService;
import com.xiaowang.cola.tools.domain.service.StockService;
import com.xiaowang.cola.tools.param.StockDeductParam;
import com.xiaowang.cola.tools.param.StockRecordBatchDeleteParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StockController 全面测试类
 * 
 * 测试覆盖范围：
 * 1. 库存扣减接口 - 成功、失败、异常场景
 * 2. 库存初始化接口 - 正常、参数验证
 * 3. 查询接口 - 当前库存、流水记录、时间范围查询
 * 4. 管理接口 - 批量删除、下架处理、对账相关
 * 5. 参数验证 - 各种边界条件和非法参数
 * 6. 异常处理 - 系统异常的处理
 *
 * @author cola
 */
@WebMvcTest(StockController.class)
@DisplayName("StockController 接口测试")
class StockControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private StockService stockService;

  @MockBean
  private StockRecordCleanupService cleanupService;

  private static final String BASE_URL = "/api/stock";
  private static final String TEST_PRODUCT_ID = "TEST_PRODUCT_001";
  private static final String TEST_USER_ID = "TEST_USER_001";
  private static final String TEST_RECORD_ID = "TEST_RECORD_001";

  @BeforeEach
  void setUp() {
    // 重置所有 Mock 对象
    reset(stockService, cleanupService);
  }

  @Nested
  @DisplayName("库存扣减接口测试")
  class DeductStockTests {

    @Test
    @DisplayName("成功扣减库存")
    void testDeductStockSuccess() throws Exception {
      // 准备测试数据
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(10)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .orderId("ORDER_001")
          .build();

      StockDeductResponse mockResponse = StockDeductResponse.success(
          TEST_RECORD_ID, TEST_PRODUCT_ID, 10, 90);

      when(stockService.deductStock(any(StockDeductParam.class)))
          .thenReturn(mockResponse);

      // 执行请求
      ResultActions result = mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)));

      // 验证结果
      result.andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.success").value(true))
          .andExpect(jsonPath("$.data.recordId").value(TEST_RECORD_ID))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.amount").value(10))
          .andExpect(jsonPath("$.data.remainingStock").value(90));

      // 验证服务调用
      verify(stockService, times(1)).deductStock(any(StockDeductParam.class));
    }

    @Test
    @DisplayName("库存不足扣减失败")
    void testDeductStockInsufficientStock() throws Exception {
      // 准备测试数据
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(150)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .build();

      StockDeductResponse mockResponse = StockDeductResponse.failure(
          TEST_PRODUCT_ID, 150, "商品库存不足");

      when(stockService.deductStock(any(StockDeductParam.class)))
          .thenReturn(mockResponse);

      // 执行请求
      ResultActions result = mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)));

      // 验证结果
      result.andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("DEDUCT_FAILED"))
          .andExpect(jsonPath("$.errorMessage").value("商品库存不足"));
    }

    @Test
    @DisplayName("系统异常处理")
    void testDeductStockSystemException() throws Exception {
      // 准备测试数据
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(10)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .build();

      when(stockService.deductStock(any(StockDeductParam.class)))
          .thenThrow(new RuntimeException("Redis连接异常"));

      // 执行请求
      ResultActions result = mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)));

      // 验证结果
      result.andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("SYSTEM_ERROR"))
          .andExpect(jsonPath("$.errorMessage").value("系统异常，请稍后重试"));
    }

    @Test
    @DisplayName("参数验证 - 商品ID为空")
    void testDeductStockValidationProductIdEmpty() throws Exception {
      StockDeductParam param = StockDeductParam.builder()
          .productId("") // 空字符串
          .amount(10)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .build();

      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("参数验证 - 扣减数量为0")
    void testDeductStockValidationAmountZero() throws Exception {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(0) // 无效数量
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .build();

      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("参数验证 - 用户ID为空")
    void testDeductStockValidationUserIdEmpty() throws Exception {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(10)
          .userId("") // 空字符串
          .scene("SECKILL")
          .build();

      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("库存初始化接口测试")
  class InitStockTests {

    @Test
    @DisplayName("成功初始化库存")
    void testInitStockSuccess() throws Exception {
      doNothing().when(stockService).initStock(TEST_PRODUCT_ID, 100);

      mockMvc.perform(post(BASE_URL + "/init")
          .param("productId", TEST_PRODUCT_ID)
          .param("stock", "100"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data").value("库存初始化成功"));

      verify(stockService, times(1)).initStock(TEST_PRODUCT_ID, 100);
    }

    @Test
    @DisplayName("初始化库存异常")
    void testInitStockException() throws Exception {
      doThrow(new RuntimeException("Redis连接异常"))
          .when(stockService).initStock(TEST_PRODUCT_ID, 100);

      mockMvc.perform(post(BASE_URL + "/init")
          .param("productId", TEST_PRODUCT_ID)
          .param("stock", "100"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("INIT_FAILED"))
          .andExpect(jsonPath("$.errorMessage").value("库存初始化失败"));
    }

    @Test
    @DisplayName("参数验证 - 商品ID为空")
    void testInitStockValidationProductIdEmpty() throws Exception {
      mockMvc.perform(post(BASE_URL + "/init")
          .param("productId", "")
          .param("stock", "100"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("参数验证 - 库存数量为负数")
    void testInitStockValidationStockNegative() throws Exception {
      mockMvc.perform(post(BASE_URL + "/init")
          .param("productId", TEST_PRODUCT_ID)
          .param("stock", "-1"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("查询接口测试")
  class QueryTests {

    @Test
    @DisplayName("查询当前库存 - 商品存在")
    void testGetCurrentStockExists() throws Exception {
      when(stockService.getCurrentStock(TEST_PRODUCT_ID)).thenReturn(50);

      mockMvc.perform(get(BASE_URL + "/current/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.currentStock").value(50))
          .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("查询当前库存 - 商品不存在")
    void testGetCurrentStockNotExists() throws Exception {
      when(stockService.getCurrentStock(TEST_PRODUCT_ID)).thenReturn(null);

      mockMvc.perform(get(BASE_URL + "/current/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.currentStock").isEmpty())
          .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @DisplayName("查询库存异常")
    void testGetCurrentStockException() throws Exception {
      when(stockService.getCurrentStock(TEST_PRODUCT_ID))
          .thenThrow(new RuntimeException("Redis连接异常"));

      mockMvc.perform(get(BASE_URL + "/current/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("QUERY_FAILED"));
    }

    @Test
    @DisplayName("查询流水记录ID列表")
    void testGetStockRecordIds() throws Exception {
      List<String> recordIds = Arrays.asList("RECORD_001", "RECORD_002", "RECORD_003");
      when(stockService.getStockRecordIds(TEST_PRODUCT_ID)).thenReturn(recordIds);

      mockMvc.perform(get(BASE_URL + "/records/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data.length()").value(3))
          .andExpect(jsonPath("$.data[0]").value("RECORD_001"));
    }

    @Test
    @DisplayName("查询流水记录详情 - 记录存在")
    void testGetStockRecordExists() throws Exception {
      StockRecord mockRecord = StockRecord.builder()
          .recordId(TEST_RECORD_ID)
          .productId(TEST_PRODUCT_ID)
          .operationType("DEDUCT")
          .amount(10)
          .beforeStock(100)
          .afterStock(90)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .createTime(LocalDateTime.now())
          .build();

      when(stockService.getStockRecord(TEST_PRODUCT_ID, TEST_RECORD_ID))
          .thenReturn(mockRecord);

      mockMvc.perform(get(BASE_URL + "/record/{productId}/{recordId}",
          TEST_PRODUCT_ID, TEST_RECORD_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.recordId").value(TEST_RECORD_ID))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.operationType").value("DEDUCT"))
          .andExpect(jsonPath("$.data.amount").value(10));
    }

    @Test
    @DisplayName("查询流水记录详情 - 记录不存在")
    void testGetStockRecordNotExists() throws Exception {
      when(stockService.getStockRecord(TEST_PRODUCT_ID, TEST_RECORD_ID))
          .thenReturn(null);

      mockMvc.perform(get(BASE_URL + "/record/{productId}/{recordId}",
          TEST_PRODUCT_ID, TEST_RECORD_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
          .andExpect(jsonPath("$.errorMessage").value("流水记录不存在"));
    }

    @Test
    @DisplayName("根据时间范围查询流水记录")
    void testGetRecordsByTimeRange() throws Exception {
      List<StockRecord> mockRecords = Arrays.asList(
          StockRecord.builder()
              .recordId("RECORD_001")
              .productId(TEST_PRODUCT_ID)
              .operationType("DEDUCT")
              .amount(10)
              .createTime(LocalDateTime.now())
              .build());

      when(cleanupService.getRecordsByTimeRange(eq(TEST_PRODUCT_ID),
          any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(mockRecords);

      mockMvc.perform(get(BASE_URL + "/records/time-range/{productId}", TEST_PRODUCT_ID)
          .param("startTime", "2023-01-01T00:00:00")
          .param("endTime", "2023-12-31T23:59:59"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("时间范围查询 - 时间格式错误")
    void testGetRecordsByTimeRangeInvalidFormat() throws Exception {
      mockMvc.perform(get(BASE_URL + "/records/time-range/{productId}", TEST_PRODUCT_ID)
          .param("startTime", "invalid-time")
          .param("endTime", "2023-12-31T23:59:59"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.errorCode").value("QUERY_FAILED"));
    }
  }

  @Nested
  @DisplayName("管理接口测试")
  class ManagementTests {

    @Test
    @DisplayName("批量删除流水记录")
    void testBatchDeleteStockRecords() throws Exception {
      StockRecordBatchDeleteParam param = StockRecordBatchDeleteParam.builder()
          .productId(TEST_PRODUCT_ID)
          .recordIds(Arrays.asList("RECORD_001", "RECORD_002"))
          .build();

      when(stockService.batchDeleteStockRecords(any(StockRecordBatchDeleteParam.class)))
          .thenReturn(2);

      mockMvc.perform(post(BASE_URL + "/records/batch-delete")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.requestCount").value(2))
          .andExpect(jsonPath("$.data.deletedCount").value(2));
    }

    @Test
    @DisplayName("商品下架设置流水过期时间")
    void testSetStockRecordsExpireOnOffline() throws Exception {
      when(stockService.setStockRecordsExpireOnOffline(TEST_PRODUCT_ID))
          .thenReturn(5);

      mockMvc.perform(post(BASE_URL + "/offline/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.updatedCount").value(5))
          .andExpect(jsonPath("$.data.expireTime").value("24小时"));
    }

    @Test
    @DisplayName("批量标记流水记录为已对账")
    void testMarkRecordsAsReconciled() throws Exception {
      List<String> recordIds = Arrays.asList("RECORD_001", "RECORD_002", "RECORD_003");

      when(cleanupService.markRecordsAsReconciled(recordIds)).thenReturn(3);

      mockMvc.perform(post(BASE_URL + "/records/mark-reconciled")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(recordIds)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.requestCount").value(3))
          .andExpect(jsonPath("$.data.updatedCount").value(3));
    }

    @Test
    @DisplayName("商品下架处理")
    void testProcessProductOffline() throws Exception {
      StockRecordCleanupService.ProcessResult mockResult = new StockRecordCleanupService.ProcessResult();
      mockResult.setPersistedCount(10);
      mockResult.setExpiredCount(5);

      when(cleanupService.processProductOffline(TEST_PRODUCT_ID))
          .thenReturn(mockResult);

      mockMvc.perform(post(BASE_URL + "/product-offline/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID))
          .andExpect(jsonPath("$.data.persistedCount").value(10))
          .andExpect(jsonPath("$.data.expiredCount").value(5));
    }

    @Test
    @DisplayName("查询待对账的流水记录")
    void testGetPendingReconcileRecords() throws Exception {
      List<StockRecord> mockRecords = Arrays.asList(
          StockRecord.builder()
              .recordId("RECORD_001")
              .productId(TEST_PRODUCT_ID)
              .operationType("DEDUCT")
              .amount(10)
              .status("PENDING")
              .build());

      when(cleanupService.getPendingReconcileRecords(TEST_PRODUCT_ID))
          .thenReturn(mockRecords);

      mockMvc.perform(get(BASE_URL + "/records/pending-reconcile/{productId}", TEST_PRODUCT_ID))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
  }

  @Nested
  @DisplayName("边界条件和异常场景测试")
  class EdgeCaseTests {

    @Test
    @DisplayName("路径参数验证 - 商品ID为空字符串")
    void testPathVariableValidation() throws Exception {
      mockMvc.perform(get(BASE_URL + "/current/ ")) // 空格
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("请求体为空")
    void testEmptyRequestBody() throws Exception {
      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("请求体格式错误")
    void testInvalidJsonFormat() throws Exception {
      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content("invalid json"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("批量删除 - 记录ID列表为空")
    void testBatchDeleteEmptyRecordIds() throws Exception {
      StockRecordBatchDeleteParam param = StockRecordBatchDeleteParam.builder()
          .productId(TEST_PRODUCT_ID)
          .recordIds(Arrays.asList()) // 空列表
          .build();

      mockMvc.perform(post(BASE_URL + "/records/batch-delete")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("大数值测试")
    void testLargeValues() throws Exception {
      StockDeductParam param = StockDeductParam.builder()
          .productId(TEST_PRODUCT_ID)
          .amount(Integer.MAX_VALUE)
          .userId(TEST_USER_ID)
          .scene("SECKILL")
          .build();

      StockDeductResponse mockResponse = StockDeductResponse.failure(
          TEST_PRODUCT_ID, Integer.MAX_VALUE, "扣减数量过大");

      when(stockService.deductStock(any(StockDeductParam.class)))
          .thenReturn(mockResponse);

      mockMvc.perform(post(BASE_URL + "/deduct")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(param)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(false));
    }
  }
}
