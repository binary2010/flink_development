package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    public String inpatientNo;
    public String orderNo;
    public String itemName;
    public Date orderDate;
}
