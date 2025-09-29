package com.xiaowang.cola.stream.consumer;

import com.alibaba.fastjson2.JSON;
import com.xiaowang.cola.stream.param.MessageBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

import static com.xiaowang.cola.stream.producer.StreamProducer.ROCKET_MQ_MESSAGE_ID;
import static com.xiaowang.cola.stream.producer.StreamProducer.ROCKET_MQ_TOPIC;
import static com.xiaowang.cola.stream.producer.StreamProducer.ROCKET_TAGS;


/**
 * MQ消费基类
 * author: cola
 */
@Slf4j
public class AbstractStreamConsumer {

    /**
     * 从msg中解析出消息对象
     *
     * @param msg
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getMessage(Message<MessageBody> msg, Class<T> type) {
        String messageId = msg.getHeaders().get(ROCKET_MQ_MESSAGE_ID, String.class);
        String tag = msg.getHeaders().get(ROCKET_TAGS, String.class);
        String topic = msg.getHeaders().get(ROCKET_MQ_TOPIC, String.class);
        Object object = JSON.parseObject(msg.getPayload().getBody(), type);
        log.info("Received Message topic:{} messageId:{},object:{}，tag:{}", topic, messageId, JSON.toJSONString(object), tag);
        return (T) object;
    }
}
