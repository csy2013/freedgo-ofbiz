package com.qihua.ofbiz.member;

import com.qihua.ofbiz.common.ExcelUtils;
import javolution.util.FastList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MemberExcelServices {


    public static void exportMember(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (GenericDelegator) request.getAttribute("delegator");

        //先查询出列表
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PT", "Party");
        dynamicView.addAlias("PT", "partyId");
        dynamicView.addAlias("PT", "partyTypeId");
        dynamicView.addAlias("PT", "statusId");
        dynamicView.addAlias("PT", "partyCategory");

        dynamicView.addMemberEntity("PS", "Person");
        dynamicView.addAlias("PS", "nickname");
        dynamicView.addAlias("PS", "gender");
        dynamicView.addAlias("PS", "name");
        dynamicView.addAlias("PS", "mobile");

        dynamicView.addMemberEntity("UL", "UserLogin");
        dynamicView.addAlias("UL", "userLoginId");

//        dynamicView.addMemberEntity("PL", "PartyLevel");
//        dynamicView.addAlias("PL", "partyId");
//
//        dynamicView.addMemberEntity("PLT", "PartyLevelType");
//        dynamicView.addAlias("PLT", "levelName");
//        dynamicView.addAlias("PLT", "levelId");
//
//        dynamicView.addViewLink("PT", "PL", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
//        dynamicView.addViewLink("PL", "PLT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("levelId", "levelId"));
        dynamicView.addViewLink("PT", "PS", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        dynamicView.addViewLink("PT", "UL", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        List<EntityCondition> andExprs = FastList.newInstance();

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("nickname");
        fieldsToSelect.add("gender");
        fieldsToSelect.add("mobile");
        fieldsToSelect.add("name");
//        fieldsToSelect.add("levelName");
        fieldsToSelect.add("partyCategory");
        fieldsToSelect.add("userLoginId");

        //现在只查询个人会员
        // 会员类型
        //现在只查询个人会员
        List<String> partyCategoryList = FastList.newInstance();
        partyCategoryList.add("MEMBER");
        partyCategoryList.add("BUSINESS");
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyCategory"), EntityOperator.IN, partyCategoryList));
        andExprs.add(EntityCondition.makeCondition("partyTypeId", EntityOperator.EQUALS, "PERSON"));
        andExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PARTY_ENABLED"));
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        try {

            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, null);
            //查询出所有的会员列表
            List<GenericValue> memberList = pli.getCompleteList();
            int membersize = memberList.size();
            //导出
//            String[] headStrs = {"会员编码", "登录账号", "真实姓名", "性别", "手机/联系方式", "会员等级"};
            String[] headStrs = {"会员编码", "登录账号", "真实姓名", "性别", "手机/联系方式"};
            String[][] contents = new String[membersize][headStrs.length];
            for (int i = 0; i < membersize; i++) {
                contents[i][0] = memberList.get(i).getString("partyId");
                contents[i][1] = memberList.get(i).getString("userLoginId");
                contents[i][2] = memberList.get(i).getString("name");
                String strGender="";
                if(UtilValidate.isNotEmpty(memberList.get(i).getString("gender")
                )){
                    strGender= ("M".equals(memberList.get(i).getString("gender").trim()))?"男":"女";
                }
                contents[i][3] = strGender;
                contents[i][4] = memberList.get(i).getString("mobile");
//                contents[i][5] = memberList.get(i).getString("levelName");
                System.out.println(contents[i].toString());
            }

            String fileName = "会员列表_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            //创建HSSFWorkbook
            HSSFWorkbook wb = ExcelUtils.getHSSFWorkbook("会员列表", headStrs, contents, null);
            //响应到客户端
            try {
                ExcelUtils.setResponseHeader(response, fileName);
                OutputStream os = response.getOutputStream();
                wb.write(os);
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
