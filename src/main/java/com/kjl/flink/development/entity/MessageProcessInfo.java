package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageProcessInfo {
    private String messageId;
    private Date dateCreated;
    private String messageType;
    private String infoType;

    private String upid;
    private String localId;

    private String clinicNo;

    private String applyNo;
    private String itemCode;
    private String itemName;

    private String state;
}
