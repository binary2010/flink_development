/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kjl.flink.development.custommodel.v24.segment;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v24.datatype.NM;
import ca.uhn.hl7v2.model.v24.datatype.ST;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.parser.ModelClassFactory;

/**
 * Created by KJL on 2018-06-05.
 */
public class Z01 extends AbstractSegment {

    /**
     * Adding a serial UID is always a good idea, but optional
     */
    private static final long serialVersionUID = 1;

    public Z01(Group parent, ModelClassFactory factory) {
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
            //add(ST.class, true, 0, 100, new Object[]{ getMessage() }, "Pet Name(s)");
            //add(NM.class, false, 1, 4, new Object[]{ getMessage() }, "Shoe Size");

            add(ST.class, true, 0, 4, new Object[]{getMessage()}, "DEPT_CODE");
            add(ST.class, true, 0, 4, new Object[]{getMessage()}, "DEPT_NAME");
            add(ST.class, true, 0, 30, new Object[]{getMessage()}, "DEPT_ENAME");
            add(ST.class, true, 0, 20, new Object[]{getMessage()}, "SIMPLE_NAME");
            add(ST.class, true, 0, 16, new Object[]{getMessage()}, "DEPT_TYPE");

            add(ST.class, true, 0, 4, new Object[]{getMessage()}, "REGDEPT_FLAG");
            add(ST.class, true, 0, 1, new Object[]{getMessage()}, "TATDEPT_FLAG");
            add(ST.class, true, 0, 1, new Object[]{getMessage()}, "DEPT_PRO");
            add(ST.class, true, 0, 1, new Object[]{getMessage()}, "VALID_STATE");
            add(NM.class, true, 0, 1, new Object[]{getMessage()}, "SORT_ID");

            add(ST.class, true, 0, 8, new Object[]{getMessage()}, "SPELL_CODE");
            add(ST.class, true, 0, 8, new Object[]{getMessage()}, "WB_CODE");
            add(ST.class, true, 0, 6, new Object[]{getMessage()}, "OPER_CODE");
            add(TS.class, true, 0, 26, new Object[]{getMessage()}, "OPER_DATE");


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

    /**
     * Create an accessor for each field
     * <p/>
     * public ST getPetName() throws HL7Exception {
     * return getTypedField(1, 0);
     * }
     */

    public ST getDEPT_CODE() throws HL7Exception {

        return getTypedField(1, 0);
    }

    public ST getDEPT_NAME() throws HL7Exception {
        return getTypedField(2, 0);
    }

    public ST getDEPT_ENAME() throws HL7Exception {
        return getTypedField(3, 0);
    }

    public ST getSIMPLE_NAME() throws HL7Exception {
        return getTypedField(4, 0);
    }

    public ST getDEPT_TYPE() throws HL7Exception {
        return getTypedField(5, 0);
    }

    public ST getREGDEPT_FLAG() throws HL7Exception {
        return getTypedField(6, 0);
    }

    public ST getTATDEPT_FLAG() throws HL7Exception {
        return getTypedField(7, 0);
    }

    public ST getDEPT_PRO() throws HL7Exception {
        return getTypedField(8, 0);
    }

    public ST getVALID_STATE() throws HL7Exception {
        return getTypedField(9, 0);
    }

    public NM getSORT_ID() throws HL7Exception {
        return getTypedField(10, 0);
    }

    public ST getSPELL_CODE() throws HL7Exception {
        return getTypedField(11, 0);
    }

    public ST getWB_CODE() throws HL7Exception {
        return getTypedField(12, 0);
    }

    public ST getOPER_CODE() throws HL7Exception {
        return getTypedField(13, 0);
    }

    public TS getOPER_DATE() throws HL7Exception {
        return getTypedField(14, 0);
    }

}
