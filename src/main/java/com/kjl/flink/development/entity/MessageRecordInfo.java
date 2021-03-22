package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRecordInfo {
    private String id;
    private String patientName;
    private String upid;
    private String localId;
    private String clinicNo;

    private String patientType;
    private String clinicDate;
    private List<MessageInfo> messageInfoList = Lists.newArrayList();

}
