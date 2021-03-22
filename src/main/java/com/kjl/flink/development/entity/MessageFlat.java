package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageFlat {
    public String msgType;
    public List<MessageInfo> messageInfoList;

    public MessageFlat(MessageInfo ride, MessageInfo fare) {
        if (this.messageInfoList == null) {
            this.messageInfoList = Lists.newArrayList();
        }
        this.messageInfoList.add(ride);
        this.messageInfoList.add(fare);
    }
}
