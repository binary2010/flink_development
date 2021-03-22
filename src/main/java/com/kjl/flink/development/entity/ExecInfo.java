package com.kjl.flink.development.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecInfo {
    public String inpatientNo;
    public String orderNo;
    public String itemName;
    public Date execDate;
    public String execSqn;
}
