package com.kjl.flink.development.util;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ADT_A42;
import ca.uhn.hl7v2.model.v24.message.*;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.ORC;
import ca.uhn.hl7v2.model.v24.segment.PID;
import ca.uhn.hl7v2.model.v24.segment.PV1;
import ca.uhn.hl7v2.parser.CustomModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.entity.MessageInfo;
import com.kjl.flink.development.entity.MessageProcessInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Slf4j
public class MessageDecodeUtil {
    private static final Parser parser;

    static {
        HapiContext context = new DefaultHapiContext();
        ModelClassFactory cmf = new CustomModelClassFactory("com.kjl.flink.development.custommodel");

        context.setModelClassFactory(cmf);
        NoValidation noValidation = new NoValidation();
        context.setValidationContext(noValidation);
        parser = context.getPipeParser();
        parser.getParserConfiguration().setDefaultObx2Type("NM");
    }

    public static List<MessageInfo> parseMessage(MessageInfo messageInfo) {
        List<MessageInfo> messageInfoList = Lists.newArrayList();
        Message msg = null;
        HapiContext context = new DefaultHapiContext();
        ModelClassFactory cmf = new CustomModelClassFactory("com.kjl.flink.development.custommodel");

        context.setModelClassFactory(cmf);
        NoValidation noValidation = new NoValidation();
        context.setValidationContext(noValidation);
        Parser parser = context.getPipeParser();
        parser.getParserConfiguration().setDefaultObx2Type("NM");
        try {
            msg = parser.parse(messageInfo.getRawData());
            if (msg != null) {
                MSH msh = (MSH) msg.get("MSH");
                if ("A08".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A10".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A42".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A31".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A11".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A13".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A25".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A32".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A12".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue())
                ) {
                    msh.getMsh12_VersionID().getVid1_VersionID().setValue("2.3");
                    String newMsg = parser.encode(msg);
                    msg = parser.parse(newMsg);
                }
            }
            if (msg != null) {
                //log.info("message version:{},name:{}",msg.getVersion(),msg.getName());
                switch (msg.getVersion()) {
                    case "2.3":
                        ca.uhn.hl7v2.model.v23.segment.MSH msh = (ca.uhn.hl7v2.model.v23.segment.MSH) msg.get("MSH");
                        messageInfo.setMsgId(msh.getMsh10_MessageControlID().getValue());
//                        try {
//                            messageInfo.setDateCreated(DateUtils.parseDate(msh.getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue(),
//                                    "yyyyMMddHHmmss"));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                        messageInfo.setMsgType(msh.getMsh9_MessageType().getCm_msg1_MessageType().getValue() + "_"
                                + msh.getMsh9_MessageType().getCm_msg2_TriggerEvent().getValue());
                        messageInfo.setState("NW");
                        switch (msg.getName()) {
                            case "ADT_A08":
                            case "ADT_A10":
                            case "ADT_A11":
                            case "ADT_A12":
                            case "ADT_A13":
                            case "ADT_A25":
                            case "ADT_A31":
                            case "ADT_A32":
                                ca.uhn.hl7v2.model.v23.segment.PID pid = (ca.uhn.hl7v2.model.v23.segment.PID) msg.get("PID");
                                messageInfo.setUpid(pid.getPid2_PatientIDExternalID().getCx1_ID().getValue());
                                break;
                            case "ADT_A42":
                                ADT_A42 adtA42 = (ADT_A42) msg;
                                messageInfo.setUpid(adtA42.getPATIENT().getPID().getPid2_PatientIDExternalID().getCx1_ID().getValue());
                                break;
                            default:
                                break;
                        }
                        break;
                    case "2.4":
                        MSH msh4 = (MSH) msg.get("MSH");
                        messageInfo.setMsgId(msh4.getMsh10_MessageControlID().getValue());
//                        try {
//                            messageInfo.setDateCreated(DateUtils.parseDate(msh4.getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue(),
//                                    "yyyyMMddHHmmss"));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                        messageInfo.setMsgType(msh4.getMsh9_MessageType().getMsg1_MessageType().getValue() + "_"
                                + msh4.getMsh9_MessageType().getMsg2_TriggerEvent().getValue());
                        PID pid = null;
                        ORC orc = null;
                        switch (msg.getName()) {
                            case "MFN_M01":
                            case "MFN_M04":
                            case "MFN_M15":
                            case "MFQ_M15":
                                break;
                            case "ADT_A01":
                            case "ADT_A02":
                                pid = (PID) msg.get("PID");
                                messageInfo.setUpid(pid.getPid2_PatientID().getCx1_ID().getValue());

                                break;
                            case "ADT_A03":
                            case "ADT_A04":
                                pid = (PID) msg.get("PID");
                                messageInfo.setUpid(pid.getPid2_PatientID().getCx1_ID().getValue());

                                break;

                            case "ADT_A15":
                            case "ADT_A16":

                            case "ADT_A54":
                            case "ADT_ZP4":
                            case "ADT_ZV3":
                            case "DFT_P03":
                                pid = (PID) msg.get("PID");
                                messageInfo.setUpid(pid.getPid2_PatientID().getCx1_ID().getValue());

//                                orc = (ORC) msg.get("ORC");
//                                messageInfo.setState(orc.getOrc1_OrderControl().getValue());
                                break;


                            case "OML_O21":
                                OML_O21 omlO21 = (OML_O21) msg;
                                messageInfo.setUpid(omlO21.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());

                                int orderCount = omlO21.getORDER_GENERAL().getORDERReps();
                                for (int i = 0; i < orderCount; i++) {
                                    MessageInfo sub = (MessageInfo) BeanUtils.cloneBean(messageInfo);
                                    sub.setState(omlO21.getORDER_GENERAL().getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyId(omlO21.getORDER_GENERAL().getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "OMP_O09":
                                OMP_O09 ompO09 = (OMP_O09) msg;
                                messageInfo.setUpid(ompO09.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(ompO09.getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;
                            case "OMG_O19":
                                OMG_O19 omgO19 = (OMG_O19) msg;
                                messageInfo.setUpid(omgO19.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(omgO19.getORDER().getORC().getOrc1_OrderControl().getValue());

                                int omgCount = omgO19.getORDERReps();
                                for (int i = 0; i < omgCount; i++) {
                                    MessageInfo sub = (MessageInfo) BeanUtils.cloneBean(messageInfo);
                                    sub.setState(omgO19.getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyId(omgO19.getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "ORG_O20":
                                ORG_O20 orgO20 = (ORG_O20) msg;
                                messageInfo.setUpid(orgO20.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(orgO20.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());

                                int orgCount = orgO20.getRESPONSE().getORDERReps();
                                for (int i = 0; i < orgCount; i++) {
                                    MessageInfo sub = (MessageInfo) BeanUtils.cloneBean(messageInfo);
                                    sub.setState(orgO20.getRESPONSE().getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyId(orgO20.getRESPONSE().getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;
                            case "ORL_O22":
                                ORL_O22 orlO22 = (ORL_O22) msg;
                                messageInfo.setUpid(orlO22.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(orlO22.getRESPONSE().getPATIENT().getGENERAL_ORDER().getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;

                            case "ORP_O10":
                                ORP_O10 orpO10 = (ORP_O10) msg;
                                messageInfo.setUpid(orpO10.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(orpO10.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;

                            case "RAS_O17":
                                RAS_O17 rasO17 = (RAS_O17) msg;
                                messageInfo.setUpid(rasO17.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(rasO17.getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;

                            case "ORM_O01":
                                ORM_O01 ormO01 = (ORM_O01) msg;
                                messageInfo.setUpid(ormO01.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(ormO01.getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;
                            case "OUL_R21":
                                OUL_R21 oulR21 = (OUL_R21) msg;
                                messageInfo.setUpid(oulR21.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(oulR21.getORDER_OBSERVATION().getOBSERVATION().getOBX().getObx2_ValueType().getValue());
                                break;

                            case "ORU_R01":
                                ORU_R01 oruR01 = (ORU_R01) msg;
                                messageInfo.setUpid(oruR01.getPATIENT_RESULT().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                //messageInfo.setState(oruR01.getORDER_OBSERVATION().getOBSERVATION().getOBX().getObx2_ValueType().getValue());
                                break;
                            case "ORR_O02":
                                ORR_O02 orrO02 = (ORR_O02) msg;
                                messageInfo.setUpid(orrO02.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState(orrO02.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());
                                break;

                            case "SRM_S01":
                                SRM_S01 srmS01 = (SRM_S01) msg;
                                messageInfo.setUpid(srmS01.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState("NW");
                                break;
                            case "RGV_O15":
                                RGV_O15 rgvO15 = (RGV_O15) msg;
                                messageInfo.setUpid(rgvO15.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState("NW");
                                break;
                            case "SIU_S12":
                                SIU_S12 siuS12 = (SIU_S12) msg;
                                messageInfo.setUpid(siuS12.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                messageInfo.setState("NW");
                                break;
                            case "SQM_ZQ1":

                                pid = (PID) msg.get("PID");
                                messageInfo.setUpid(pid.getPid2_PatientID().getCx1_ID().getValue());

//                                orc = (ORC) msg.get("ORC");
//                                messageInfo.setState(orc.getOrc1_OrderControl().getValue());
                                break;
                            default:
                                break;

                        }

                        break;
                    default:
                        break;
                }

            } else {
                messageInfo.setState("NW");
            }
            //log.info("message version:{},name:{},id:{},create date:{}", msg.getVersion(), msg.getName(),
            //        messageInfo.getMsgId(), DateFormatUtils.format(messageInfo.getDateCreated(), "yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.error("消息解析出错", e);
            log.info("消息内容:{}", messageInfo.getRawData());

            messageInfo.setState("NW");
        }
        if (messageInfoList.isEmpty()) {
            messageInfo.setState("NW");
            messageInfo.setApplyId("000");
            messageInfoList.add(messageInfo);
        }
        return messageInfoList;
    }


    public static List<MessageProcessInfo> transforMessage(MessageBaseInfo messageInfo) {
        List<MessageProcessInfo> messageInfoList = Lists.newArrayList();
        Message msg = null;

        try {
            //for(String messageStr:messageInfo.getRawData().split("MSH")) {
            msg = parser.parse(messageInfo.getMsg());
            if (msg != null) {
                MSH msh = (MSH) msg.get("MSH");
                if ("A08".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A10".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A42".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A31".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A11".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A13".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A25".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A32".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue()) ||
                        "A12".equals(msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue())
                ) {
                    msh.getMsh12_VersionID().getVid1_VersionID().setValue("2.3");
                    String newMsg = parser.encode(msg);
                    msg = parser.parse(newMsg);
                }
            }
            if (msg != null) {
                MessageProcessInfo processInfo = new MessageProcessInfo();
                processInfo.setState("NW");
                processInfo.setDateCreated(messageInfo.getDatecreated());
                processInfo.setMsgsender(messageInfo.getMsgsender());
                processInfo.setMsgreceiver(messageInfo.getMsgreceiver());

                switch (msg.getVersion()) {
                    case "2.3":
                        ca.uhn.hl7v2.model.v23.segment.MSH msh = (ca.uhn.hl7v2.model.v23.segment.MSH) msg.get("MSH");
                        processInfo.setMessageId(msh.getMsh10_MessageControlID().getValue());
                        processInfo.setMessageType(msh.getMsh9_MessageType().getCm_msg1_MessageType().getValue() + "_"
                                + msh.getMsh9_MessageType().getCm_msg2_TriggerEvent().getValue());

                        switch (msg.getName()) {
                            case "ADT_A08":
                            case "ADT_A10":
                            case "ADT_A11":
                            case "ADT_A12":
                            case "ADT_A13":
                            case "ADT_A25":
                            case "ADT_A31":
                            case "ADT_A32":
                                ca.uhn.hl7v2.model.v23.segment.PID pid = (ca.uhn.hl7v2.model.v23.segment.PID) msg.get("PID");
                                processInfo.setUpid(pid.getPid2_PatientIDExternalID().getCx1_ID().getValue());
                                ca.uhn.hl7v2.model.v23.segment.PV1 pv1 = (ca.uhn.hl7v2.model.v23.segment.PV1) msg.get("PV1");
                                processInfo.setClinicNo(pv1.getPv119_VisitNumber().getCx1_ID().getValue());

                                break;
                            case "ADT_A42":
                                ADT_A42 adtA42 = (ADT_A42) msg;
                                processInfo.setUpid(adtA42.getPATIENT().getPID().getPid2_PatientIDExternalID().getCx1_ID().getValue());
                                processInfo.setClinicNo(adtA42.getPATIENT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                break;
                            default:
                                break;
                        }
                        break;
                    case "2.4":
                        MSH msh4 = (MSH) msg.get("MSH");
                        processInfo.setMessageId(msh4.getMsh10_MessageControlID().getValue());

                        processInfo.setMessageType(msh4.getMsh9_MessageType().getMsg1_MessageType().getValue() + "_"
                                + msh4.getMsh9_MessageType().getMsg2_TriggerEvent().getValue());

                        PID pid = null;
                        ORC orc = null;
                        PV1 pv1 = null;
                        switch (msg.getName()) {
                            case "MFN_M01":
                            case "MFN_M04":
                            case "MFN_M15":
                            case "MFQ_M15":
                            case "ADT_ZP4":
                            case "ADT_ZV3":
                                break;
                            case "ADT_A01":
                            case "ADT_A02":
                            case "ADT_A03":
                            case "ADT_A04":
                            case "ADT_A15":
                            case "ADT_A16":
                            case "ADT_A54":

                            case "DFT_P03":
                            case "SQM_ZQ1":
                                pid = (PID) msg.get("PID");
                                processInfo.setUpid(pid.getPid2_PatientID().getCx1_ID().getValue());
                                pv1 = (PV1) msg.get("PV1");
                                processInfo.setClinicNo(pv1.getPv119_VisitNumber().getCx1_ID().getValue());
                                break;

                            case "OML_O21":
                                OML_O21 omlO21 = (OML_O21) msg;
                                processInfo.setUpid(omlO21.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setClinicNo(omlO21.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());

                                int orderCount = omlO21.getORDER_GENERAL().getORDERReps();
                                for (int i = 0; i < orderCount; i++) {
                                    MessageProcessInfo sub = (MessageProcessInfo) BeanUtils.cloneBean(processInfo);
                                    sub.setState(omlO21.getORDER_GENERAL().getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyNo(omlO21.getORDER_GENERAL().getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "OMP_O09":
                                OMP_O09 ompO09 = (OMP_O09) msg;
                                processInfo.setUpid(ompO09.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(ompO09.getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(ompO09.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                break;
                            case "OMG_O19":
                                OMG_O19 omgO19 = (OMG_O19) msg;
                                processInfo.setUpid(omgO19.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(omgO19.getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(omgO19.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());

                                int omgCount = omgO19.getORDERReps();
                                for (int i = 0; i < omgCount; i++) {
                                    MessageProcessInfo sub = (MessageProcessInfo) BeanUtils.cloneBean(processInfo);
                                    sub.setState(omgO19.getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyNo(omgO19.getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "ORG_O20":
                                ORG_O20 orgO20 = (ORG_O20) msg;
                                processInfo.setUpid(orgO20.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(orgO20.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());

                                int orgCount = orgO20.getRESPONSE().getORDERReps();
                                for (int i = 0; i < orgCount; i++) {
                                    MessageProcessInfo sub = (MessageProcessInfo) BeanUtils.cloneBean(processInfo);
                                    sub.setState(orgO20.getRESPONSE().getORDER(i).getORC()
                                            .getOrc1_OrderControl().getValue());
                                    sub.setApplyNo(orgO20.getRESPONSE().getORDER(i).getORC()
                                            .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    sub.setClinicNo(orgO20.getRESPONSE().getORDER(i).getORC().getOrc2_PlacerOrderNumber().getEi3_UniversalID().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "ORL_O22":
                                ORL_O22 orlO22 = (ORL_O22) msg;
                                processInfo.setUpid(orlO22.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());

                                processInfo.setClinicNo(orlO22.getRESPONSE().getPATIENT().getGENERAL_ORDER().getORDER()
                                        .getORC().getOrc2_PlacerOrderNumber().getEi3_UniversalID().getValue());

                                int confirmCount = orlO22.getRESPONSE().getPATIENT().getGENERAL_ORDER().getORDERReps();
                                for (int i = 0; i < confirmCount; i++) {
                                    MessageProcessInfo sub = (MessageProcessInfo) BeanUtils.cloneBean(processInfo);

                                    sub.setState(orlO22.getRESPONSE().getPATIENT().getGENERAL_ORDER().getORDER(i)
                                            .getORC().getOrc1_OrderControl().getValue());
                                    sub.setApplyNo(orlO22.getRESPONSE().getPATIENT().getGENERAL_ORDER().getORDER(i)
                                            .getORC().getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue());
                                    messageInfoList.add(sub);
                                }

                                break;

                            case "ORP_O10":
                                ORP_O10 orpO10 = (ORP_O10) msg;
                                processInfo.setUpid(orpO10.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(orpO10.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(orpO10.getRESPONSE().getORDER().getORC()
                                        .getOrc2_PlacerOrderNumber().getEi3_UniversalID().getValue());
                                break;

                            case "RAS_O17":
                                RAS_O17 rasO17 = (RAS_O17) msg;
                                processInfo.setUpid(rasO17.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(rasO17.getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(rasO17.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                break;

                            case "ORM_O01":
                                ORM_O01 ormO01 = (ORM_O01) msg;
                                processInfo.setUpid(ormO01.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(ormO01.getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(ormO01.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                break;
                            case "OUL_R21":
                                OUL_R21 oulR21 = (OUL_R21) msg;
                                processInfo.setUpid(oulR21.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(oulR21.getORDER_OBSERVATION().getOBSERVATION().getOBX().getObx2_ValueType().getValue());
                                processInfo.setClinicNo(oulR21.getVISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                break;

                            case "ORU_R01":
                                ORU_R01 oruR01 = (ORU_R01) msg;
                                processInfo.setUpid(oruR01.getPATIENT_RESULT().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setClinicNo(oruR01.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                processInfo.setState("NW");
                                break;
                            case "ORR_O02":
                                ORR_O02 orrO02 = (ORR_O02) msg;
                                processInfo.setUpid(orrO02.getRESPONSE().getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setState(orrO02.getRESPONSE().getORDER().getORC().getOrc1_OrderControl().getValue());
                                processInfo.setClinicNo(orrO02.getRESPONSE().getORDER().getORC().getOrc2_PlacerOrderNumber().getEi3_UniversalID().getValue());
                                break;

                            case "SRM_S01":
                                SRM_S01 srmS01 = (SRM_S01) msg;
                                processInfo.setUpid(srmS01.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setClinicNo(srmS01.getARQ().getArq2_FillerAppointmentID().getEi1_EntityIdentifier().getValue());
                                processInfo.setState("NW");
                                break;
                            case "RGV_O15":
                                RGV_O15 rgvO15 = (RGV_O15) msg;
                                processInfo.setUpid(rgvO15.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setClinicNo(rgvO15.getPATIENT().getPATIENT_VISIT().getPV1().getPv119_VisitNumber().getCx1_ID().getValue());
                                processInfo.setState("NW");
                                break;
                            case "SIU_S12":
                                SIU_S12 siuS12 = (SIU_S12) msg;
                                processInfo.setUpid(siuS12.getPATIENT().getPID().getPid2_PatientID().getCx1_ID().getValue());
                                processInfo.setClinicNo(siuS12.getSCH().getSch26_PlacerOrderNumber(0).getEi1_EntityIdentifier().getValue());
                                processInfo.setState("NW");
                                break;

                            default:
                                break;

                        }

                        break;
                    default:
                        break;
                }
                if (messageInfoList.isEmpty()) {
                    messageInfoList.add(processInfo);
                }
            }
            //}
        } catch (Exception e) {
            log.error("消息解析出错", e);
            log.info("消息内容:{}", messageInfo.getMsg());
        }
        return messageInfoList;
    }
}
