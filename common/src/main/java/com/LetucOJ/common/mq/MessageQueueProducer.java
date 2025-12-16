package com.LetucOJ.common.mq;

import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.result.ResultVO;

public interface MessageQueueProducer {
    ResultVO<Void> send(Message message);
//    void sendAsync(Message message, ResultVO<Void> callback);
//    void sendOneWay(Message message);
}
