package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordInfo {
    public String inpatientNo;
    public String orderNo;
    public String itemName;
    public Date orderDate;
    public List<ExecInfo> execInfoList;

    public RecordInfo(OrderInfo orderInfo, ExecInfo execInfo) {
        if (this.execInfoList == null) {
            this.execInfoList = Lists.newArrayList();
        }
        this.inpatientNo = orderInfo.inpatientNo;
        this.orderNo = orderInfo.orderNo;
        this.itemName = orderInfo.itemName;
        this.orderDate = orderInfo.orderDate;

        this.execInfoList.add(execInfo);
    }

    public void add(ExecInfo execInfo) {
        this.execInfoList.add(execInfo);
    }
}
