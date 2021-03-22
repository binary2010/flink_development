package com.kjl.flink.development.custommodel.v24.message;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.v24.segment.EVN;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.PID;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;

/**
 * Created by KJL on 2018-06-05.
 */
public class ADT_ZP4 extends AbstractMessage {
    public ADT_ZP4() {
        this(new DefaultModelClassFactory());
    }

    public ADT_ZP4(ModelClassFactory theFactory) {
        super(theFactory);
        init(theFactory);
    }

    private void init(ModelClassFactory factory) {
        try {
            this.add(MSH.class, true, false);
            this.add(EVN.class, true, false);
            this.add(PID.class, true, false);
        } catch (HL7Exception e) {
            log.error("Unexpected error creating ADT_ZP4 - this is probably a bug in the source code generator.", e);
        }
    }

    @Override
    public String getVersion() {
        return "2.4";
    }

    public MSH getMSH() throws HL7Exception {
        return getTyped("MSH", MSH.class);
    }

    public EVN getEVN() throws HL7Exception {
        return getTyped("EVN", EVN.class);
    }

    public PID getPID() throws HL7Exception {
        return getTyped("PID", PID.class);
    }

}
