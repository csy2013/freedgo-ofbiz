package com.yuaoq.yabiz.app.mobile.microservice.article.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.activity.api.v1.ActivityControllerV1;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.Paginate;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/article/v1")
public class ArticleV1Controller {

    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = ArticleV1Controller.class.getName();
    @RequestMapping(value = "/getArticleTags", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getArticleTags(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String sql ="SELECT DISTINCT(tag_id) tagId from ARTICLE_TAG_ASSOC";
        SQLProcessor sqlP = null;
        List<String> tagIdList = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            Map questionMap=null;
            tagIdList = FastList.newInstance();
            while (rs.next()) {
                tagIdList.add(rs.getString("tagId"));
            }
        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            return null;
        }finally {
            try {
                sqlP.close();
            } catch (GenericDataSourceException e) {
                e.printStackTrace();
            }
        }

        //查询所有的tag
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("T", "Tag");
        dve.addAlias("T","tagId");
        dve.addAlias("T","tagName");

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("tagId");
        fieldsToSelect.add("tagName");

        EntityCondition mainCond = EntityCondition.makeCondition("tagId", EntityOperator.IN,tagIdList);
        
        EntityListIterator eli =null;
        List<GenericValue> returnTags=null;
        boolean beganTransaction = false;
        try{
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, null, null);
            returnTags = eli.getCompleteList();
        }catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }

        List<Map> tagList = FastList.newInstance();
        for(GenericValue tag:returnTags){
            Map tagMap = FastMap.newInstance();
            tagMap.put("tagId",tag.getString("tagId"));
            tagMap.put("name",tag.getString("tagName"));
            tagList.add(tagMap);
        }

        resultData.put("tagList",tagList);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


        /**
         * 获得文章列表
         * @param request
         * @param paginate
         * @return
         * @throws GenericEntityException
         * @throws SQLException
         */
    @RequestMapping(value = "/articleList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> articleList(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer pageSize, String tagId) throws GenericEntityException, SQLException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 文章类型
        String articleTypeId = "STAT_MICRO_PAPER";
        // 文章状态--已审批
        String articleStatus = "2";

        //数据库查询到的数据长度
        int size = 0;

        int lowIndex = 0;
        int highIndex = 0;
        //开始页
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        //每页多少条
        int viewSize = 10;
        try {
            viewSize = pageSize;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));


        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("AC", "Article");

        dve.addAliasAll("AC", "", null);
        // 文章编号
        dve.addAlias("AC", "articleId");
        // 文章标题
        dve.addAlias("AC", "articleTitle");
        // 文章作者
        dve.addAlias("AC", "articleAuthor");
        // 文章类型
        dve.addAlias("AC", "articleTypeId");
        // 文章状态
        dve.addAlias("AC", "articleStatus");
        // 创建时间
        dve.addAlias("AC", "createdStamp");
        // 店铺信息
        dve.addAlias("AC", "productStoreId");
        List<EntityCondition> andExprs = FastList.newInstance();
        if(tagId!=null && !"".equals(tagId)){
            //用户传了标签
            dve.addMemberEntity("ATA", "ArticleTagAssoc");
            dve.addAlias("ATA", "articleId");
            dve.addAlias("ATA", "tagId");

            dve.addViewLink("AC", "ATA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("articleId", "articleId"));

            andExprs.add(EntityCondition.makeCondition("tagId", EntityOperator.EQUALS, tagId));

        }
        andExprs.add(EntityCondition.makeCondition("articleStatus", EntityOperator.EQUALS, articleStatus));
        andExprs.add(EntityCondition.makeCondition("articleTypeId", EntityOperator.EQUALS, articleTypeId));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("articleId");
        fieldsToSelect.add("articleTitle");
        fieldsToSelect.add("articleAuthor");
        fieldsToSelect.add("articleTypeId");
        fieldsToSelect.add("articleStatus");
        fieldsToSelect.add("createdStamp");

//        dve.setGroupBy(fieldsToSelect);

        List<String> orderBy = FastList.newInstance();
        orderBy.add("-createdStamp");


        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            beganTransaction = TransactionUtil.begin();

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            eli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);

            List<GenericValue> articleList = eli.getPartialList(lowIndex, viewSize);
            size = eli.getResultsSizeAfterPartialList();

            List<Map<String, Object>> artList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(articleList)) {
                for (GenericValue article : articleList) {
                    Map<String, Object> artMap = FastMap.newInstance();

                    String articleId = article.getString("articleId");
                    artMap.put("articleId", articleId);
                    artMap.put("articleTitle", article.getString("articleTitle"));
                    artMap.put("articleAuthor", article.getString("articleAuthor"));
                    List<GenericValue> articleContentImg = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articleId,
                            "articleContentTypeId", "ARTICLE_FIGURE"));
                    String imgUrl="";
                    if (UtilValidate.isNotEmpty(articleContentImg)) {
                        GenericValue articleimg = EntityUtil.getFirst(articleContentImg);
                        String contentId = (String) articleimg.get("contentId");
                        if (UtilValidate.isNotEmpty(contentId)) {
                            imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                        }
                    }
                    artMap.put("imgUrl", imgUrl);

                    //查找文章属性。
                    String sql="select TAG_NAME from TAG where TAG_ID in(select TAG_ID from ARTICLE_TAG_ASSOC where ARTICLE_ID='"+articleId+"')";
                    String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
                    //获得数据库的连接
                    Connection conn = ConnectionFactory.getConnection(groupHelperName);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    List<String> tags = FastList.newInstance();
                    while (rs.next()) {
                        String tagName = rs.getString("TAG_NAME");
                        tags.add(tagName);
                    }
                    stmt.close();
                    rs.close();
                    conn.close();

                    artMap.put("tags", tags);
                    artList.add(artMap);

                }

            }
            
            resultData.put("articleList", artList);
            resultData.put("articleListSize", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));

            resultData.put("paginate", new Paginate(viewIndex,viewSize,size));
            resultData.put("retCode", 1);

        }catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 根据文章id获取文章的信息
     * @param articleId 文章id
     * @return
     * @throws SQLException
     */
    @RequestMapping(value = "/getArticleById", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getArticleById(HttpServletRequest request, String articleId, HttpServletResponse response)   {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        if (articleId == null) {
            resultData.put("retCode", "0");
            resultData.put("message", "articleId不能为空！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        // 文章内容
        boolean beganTransaction = false;
        try {
    
            beganTransaction = TransactionUtil.begin();
            GenericValue article = delegator.findByPrimaryKey("Article", UtilMisc.toMap("articleId", articleId));
    
            if (article == null) {
                resultData.put("retCode", "0");
                resultData.put("message", "文章不存在");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
    
    
            resultData.put("articleId", articleId);
            resultData.put("articleTitle", article.getString("articleTitle"));
            resultData.put("articleAuthor", article.getString("articleAuthor"));
            resultData.put("createdStamp", CommonUtils.getStringDate(article.getTimestamp("createdStamp")));
    
            // 文章关键字
    
            List<GenericValue> articleKeyword = delegator.findByAnd("ArticleKeyword", UtilMisc.toMap("articleId", articleId));
    
            String words = "";
            for (int i = 0; i < articleKeyword.size(); i++) {
                if (i == articleKeyword.size() - 1) {
                    words += articleKeyword.get(i).get("keyword");
                } else {
                    words += articleKeyword.get(i).get("keyword") + "，";
                }
            }
            resultData.put("articleKeyword", words);
    
            //文章图片
            List<GenericValue> articleContentImg = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articleId, "articleContentTypeId", "ARTICLE_FIGURE"));
            String imgUrl = "";
            if (articleContentImg != null || articleContentImg.size() > 0) {
                GenericValue articleimg = EntityUtil.getFirst(articleContentImg);
                if (articleimg != null) {
                    String contentId = articleimg.get("contentId") == null ? null : (String) articleimg.get("contentId");
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                }
            }
    
            resultData.put("imgUrl", imgUrl);
    
    
            // 查询文章内容
            String articleContent = "";
            GenericValue articleConContent = EntityUtil.getFirst(delegator.findByAnd("ArticleContent", UtilMisc.toMap(
                    "articleId", articleId, "articleContentTypeId", "ARTICLE_CONTENT")));
    
            GenericValue Content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", articleConContent.get("contentId")));
            if (UtilValidate.isNotEmpty(Content)) {
                GenericValue Contenttext = delegator.findByPrimaryKey("ElectronicText",
                        UtilMisc.toMap("dataResourceId", Content.get("dataResourceId")));
                if (UtilValidate.isNotEmpty(Contenttext)) {
                    articleContent = Contenttext.getString("textData");
                }
            }
            resultData.put("articleContent", articleContent);
    
            //查询文章对应的商品
            List<GenericValue> articleProductAssos = delegator.findByAnd("ArticleProductAssoc", UtilMisc.toMap("articleId", articleId));
    
            List<String> productIds = FastList.newInstance();

            if (UtilValidate.isNotEmpty(articleProductAssos)) {
                for (GenericValue articleProductAsso : articleProductAssos) {
                    String productId = articleProductAsso.getString("productId");
                    productIds.add(productId);
                }
            }
            List<Map> productList = FastList.newInstance();
            for(String productId:productIds){
                BigDecimal activityPrice =ProductUtils.getProductActivityPrice(productId,dispatcher);
                if(activityPrice.doubleValue()>0L){
                    Map productInfo =ProductUtils.getOneTogetherProductsWithOutFeature(productId,activityPrice.doubleValue(),delegator,dispatcher,baseImgUrl);
                    productList.add(productInfo);
                }else{
                    Map productInfo =ProductUtils.getOneProductsWithOutFeature(productId,delegator,dispatcher,baseImgUrl);
                    productList.add(productInfo);
                }
            }


            resultData.put("products", productList);

        }catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        
        
        resultData.put("retCode", "1");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


}
