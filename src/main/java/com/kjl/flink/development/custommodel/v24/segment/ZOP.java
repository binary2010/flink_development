package com.kjl.flink.development.custommodel.v24.segment;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v24.datatype.CE;
import ca.uhn.hl7v2.model.v24.datatype.IS;
import ca.uhn.hl7v2.model.v24.datatype.ST;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.parser.ModelClassFactory;

/**
 * Created by KJL on 2018-06-05.
 */
public class ZOP extends AbstractSegment {
    /**
     * Adding a serial UID is always a good idea, but optional
     */
    private static final long serialVersionUID = 1;

    public ZOP(Group parent, ModelClassFactory factory) {
        super(parent, factory);

        // By convention, an init() method is created which adds
        // the specific fields to this segment class
        init(factory);
    }

    private void init(ModelClassFactory factory) {
        try {
            /*
             * For each field in the custom segment, the add() method is
             * called once. In this example, there are two fields in
             * the ZPI segment.
             *
             * See here for information on the arguments to this method:
             * http://hl7api.sourceforge.net/base/apidocs/ca/uhn/hl7v2/model/AbstractSegment.html#add%28java.lang.Class,%20boolean,%20int,%20int,%20java.lang.Object[],%20java.lang.String%29
             */
            //是否必填，最大重复数量，长度
            add(IS.class, false, 0, 2, new Object[]{getMessage()}, "IsICU");//1术后ICU
            add(CE.class, false, 0, 20, new Object[]{getMessage()}, "Operation Dept");//2手术科室(手术室)
            add(CE.class, false, 0, 10, new Object[]{getMessage()}, "Operation Room");//3手术间
            add(ST.class, false, 0, 10, new Object[]{getMessage()}, "Sequence");//4台次
            add(IS.class, false, 0, 10, new Object[]{getMessage()}, "Operation Scale");//5手术等级
            add(ST.class, false, 0, 10, new Object[]{getMessage()}, "SurgeryTime");//6手术时长
            add(IS.class, false, 0, 10, new Object[]{getMessage()}, "Anaesthesia Method");//7麻醉方法
            add(IS.class, false, 0, 10, new Object[]{getMessage()}, "Emergency Indicator");//8急诊标志
            add(IS.class, false, 0, 20, new Object[]{getMessage()}, "Isolation Indicator");//9隔离标志
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Surgeon");//10手术人员
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Anaesthesia Doctor");//11麻醉人员
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Perfusion Doctor");//12灌注医生
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Blood Tran Doctor");//13输血者
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Operation Nurse");//14台上护士
            add(XCN.class, false, 0, 250, new Object[]{getMessage()}, "Supply Nurse");//15供应护士
            add(CE.class, false, 0, 250, new Object[]{getMessage()}, "Universal Service Identifier");//16手术申请项目
            add(ST.class, false, 0, 10, new Object[]{getMessage()}, "Asepsis Scale");//17无菌程度
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Test results");//18乙肝-甲肝-丙肝-梅毒-艾滋
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Test Sensitivity");//19试敏
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Quality control program");//20质控项目
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "remark");//21备注
            add(IS.class, false, 0, 10, new Object[]{getMessage()}, "IsFastFrozen");//22是否快速冰冻
            add(IS.class, false, 0, 10, new Object[]{getMessage()}, "IsXRay ");//23是否进行X-Ray
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Profession");//24专业
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Qiekou Class");//25切口类型
            add(ST.class, false, 0, 2, new Object[]{getMessage()}, "IsnoPlanOperation");//26是否是非计划再次手术
            add(ST.class, false, 0, 250, new Object[]{getMessage()}, "Multidrug Resistant");//27多重耐药
        } catch (HL7Exception e) {
            //log.error("Unexpected error creating Z01 - this is probably a bug in the source code generator.", e);
        }
    }

    /**
     * This method must be overridden. The easiest way is just to return null.
     */
    @Override
    protected Type createNewTypeWithoutReflection(int field) {
        return null;
    }
}
