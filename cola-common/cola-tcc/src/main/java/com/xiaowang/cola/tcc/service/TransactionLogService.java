package com.xiaowang.cola.tcc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowang.cola.tcc.entity.TransActionLogState;
import com.xiaowang.cola.tcc.entity.TransCancelSuccessType;
import com.xiaowang.cola.tcc.entity.TransConfirmSuccessType;
import com.xiaowang.cola.tcc.entity.TransTrySuccessType;
import com.xiaowang.cola.tcc.entity.TransactionLog;
import com.xiaowang.cola.tcc.mapper.TransactionLogMapper;
import com.xiaowang.cola.tcc.request.TccRequest;
import com.xiaowang.cola.tcc.response.TransactionCancelResponse;
import com.xiaowang.cola.tcc.response.TransactionConfirmResponse;
import com.xiaowang.cola.tcc.response.TransactionTryResponse;

/**
 * 盲盒服务
 *
 * @author cola
 */
public class TransactionLogService extends ServiceImpl<TransactionLogMapper, TransactionLog> {

    /**
     * TCC事务的Try
     *
     * @param tccRequest
     * @return
     */
    public TransactionTryResponse tryTransaction(TccRequest tccRequest) {
        TransactionLog existTransactionLog = getExistTransLog(tccRequest);
        if (existTransactionLog == null) {
            TransactionLog transactionLog = new TransactionLog(tccRequest, TransActionLogState.TRY);
            if (this.save(transactionLog)) {
                return new TransactionTryResponse(true, TransTrySuccessType.TRY_SUCCESS);
            }
            return new TransactionTryResponse(false, "TRY_FAILED", "TRY_FAILED");
        }

        //幂等
        return new TransactionTryResponse(true, TransTrySuccessType.DUPLICATED_TRY);
    }

    /**
     * TCC事务的Confirm
     *
     * @param tccRequest
     * @return
     */
    public TransactionConfirmResponse confirmTransaction(TccRequest tccRequest) {
        TransactionLog existTransactionLog = getExistTransLog(tccRequest);
        if (existTransactionLog == null) {
            throw new UnsupportedOperationException("transacton can not confirm");
        }

        if (existTransactionLog.getState() == TransActionLogState.TRY) {
            existTransactionLog.setState(TransActionLogState.CONFIRM);
            if (this.updateById(existTransactionLog)) {
                return new TransactionConfirmResponse(true, TransConfirmSuccessType.CONFIRM_SUCCESS);
            }

            return new TransactionConfirmResponse(false, "CONFIRM_FAILED", "CONFIRM_FAILED");
        }

        //幂等
        if (existTransactionLog.getState() == TransActionLogState.CONFIRM) {
            return new TransactionConfirmResponse(true, TransConfirmSuccessType.DUPLICATED_CONFIRM);
        }

        throw new UnsupportedOperationException("transacton can not confirm :" + existTransactionLog.getState());
    }

    /**
     * TCC事务的Cancel
     *
     * @param tccRequest
     * @return
     */
    public TransactionCancelResponse cancelTransaction(TccRequest tccRequest) {
        TransactionLog existTransactionLog = getExistTransLog(tccRequest);
        //如果还没有Try，则直接记录一条状态为Cancel的数据，避免发生空回滚，并解决悬挂问题
        if (existTransactionLog == null) {
            TransactionLog transactionLog = new TransactionLog(tccRequest, TransActionLogState.CANCEL, TransCancelSuccessType.EMPTY_CANCEL);
            if (this.save(transactionLog)) {
                return new TransactionCancelResponse(true, TransCancelSuccessType.EMPTY_CANCEL);
            }
            return new TransactionCancelResponse(false, "EMPTY_CANCEL_FAILED", "EMPTY_CANCEL_FAILED");
        }

        if (existTransactionLog.getState() == TransActionLogState.TRY) {
            existTransactionLog.setState(TransActionLogState.CANCEL);
            existTransactionLog.setCancelType(TransCancelSuccessType.CANCEL_AFTER_TRY_SUCCESS);
            if (this.updateById(existTransactionLog)) {
                return new TransactionCancelResponse(true, TransCancelSuccessType.CANCEL_AFTER_TRY_SUCCESS);
            }
            return new TransactionCancelResponse(false, "CANCEL_FAILED", "CANCEL_FAILED");
        }

        if (existTransactionLog.getState() == TransActionLogState.CONFIRM) {
            existTransactionLog.setState(TransActionLogState.CANCEL);
            existTransactionLog.setCancelType(TransCancelSuccessType.CANCEL_AFTER_CONFIRM_SUCCESS);
            if (this.updateById(existTransactionLog)) {
                return new TransactionCancelResponse(true, TransCancelSuccessType.CANCEL_AFTER_CONFIRM_SUCCESS);
            }
            return new TransactionCancelResponse(false, "CANCEL_FAILED", "CANCEL_FAILED");
        }

        //幂等
        if (existTransactionLog.getState() == TransActionLogState.CANCEL) {
            return new TransactionCancelResponse(true, TransCancelSuccessType.DUPLICATED_CANCEL);
        }

        return new TransactionCancelResponse(false, "CANCEL_FAILED", "CANCEL_FAILED");
    }

    private TransactionLog getExistTransLog(TccRequest request) {
        QueryWrapper<TransactionLog> queryWrapper = new QueryWrapper<TransactionLog>();
        queryWrapper.eq("transaction_id", request.getTransactionId());
        queryWrapper.eq("business_scene", request.getBusinessScene());
        queryWrapper.eq("business_module", request.getBusinessModule());

        return this.getOne(queryWrapper);
    }

}
