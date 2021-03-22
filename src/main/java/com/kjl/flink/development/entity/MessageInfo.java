package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {
    private String id;
    private String msgId;
    private String msgType;
    private Date dateCreated;
    private String rawData;
    private String upid;
    private String state;

    private String applyId;
    private String itemId;
    private String itemName;

}
