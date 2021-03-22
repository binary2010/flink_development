package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageBaseInfo {
    private String id;
    private String msgid;
    private String msgtype;
    private Date datecreated;
    private String msg;
}
