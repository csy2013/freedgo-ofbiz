package com.yuaoq.yabiz.mobile.order.shoppingcart;

import com.google.gson.internal.StringMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.order.shoppingcart.ShoppingCartHelper;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.order.shoppingcart.product.ProductPromoWorker;
import org.ofbiz.order.shoppingcart.shipping.ShippingEstimateWrapper;
import org.ofbiz.order.shoppinglist.ShoppingListEvents;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.config.ProductConfigWorker;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.*;

import static com.yuaoq.yabiz.mobile.order.shoppingcart.ShippingEvents.getShipGroupEstimate;

/**
 * Created by changsy on 16/6/28.
 */
public class ShoppingCartEvents {
    public static String module = ShoppingCartEvents.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
    
    private static final String NO_ERROR = "noerror";
    private static final String NON_CRITICAL_ERROR = "noncritical";
    private static final String ERROR = "error";
    
    public static final MathContext generalRounding = new MathContext(10);
    
    
    public static ShoppingCart getCartObject(HttpServletRequest request, Locale locale, String currencyUom) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //shoppingcart，wishCart,buyCart
        String cartType = request.getParameter("cartType");
        if (UtilValidate.isEmpty(cartType)) {
            cartType = (String) request.getAttribute("cartType");
        }
        ShoppingCart cart = null;
        // 在checkout 时,必须把cart 放到request中,这样可以保持cart 的数据变化而不会丢失,如果从shoppinglist取则还是旧数据。
        if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
            if (request.getAttribute("shoppingCart") != null) {
                cart = (ShoppingCart) request.getAttribute("shoppingCart");
                cart.setCartType("shoppingCart");
                return cart;
            }
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
            if (request.getAttribute("wishCart") != null) {
                cart = (ShoppingCart) request.getAttribute("wishCart");
                cart.setCartType("wishCart");
                return cart;
            }
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
            if (request.getAttribute("buyCart") != null) {
                cart.setCartType("buyCart");
                cart = (ShoppingCart) request.getAttribute("buyCart");
                return cart;
            }
        }
        //shoppingcart 根据应用的不同需要加载不同的webShoppingCart
        String webSiteId = request.getHeader("Client");
        
        
        String app = "app";
        
        String classstr = UtilProperties.getPropertyValue("order.properties", app + ".shopping.cart.class");
        
        if (classstr == null || classstr.equals("")) {
            app = "default";
            classstr = UtilProperties.getPropertyValue("order.properties", app + ".shopping.cart.class");
        }
        try {
            cart = (ShoppingCart) ObjectType.getInstance(classstr, new Class[]{HttpServletRequest.class, Locale.class, String.class}, new Object[]{request, locale, currencyUom});
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                request.setAttribute("shoppingCart", cart);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                request.setAttribute("wishCart", cart);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                request.setAttribute("buyCart", cart);
            }
            
        } catch (Exception e) {
            Debug.logError(e, "Unable to instance shopping cart : " + e.getMessage(), module);
        }
        //shoppinglist 中获取 类型为SLT_M_SHOP_LIST的购物车信息
        //获取userLoginId
        String userName = ShoppingCart.getUserNameFromRequest(request);
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            //没有店铺的
            String productStoreId = null;
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                cart.setCartType("shoppingCart");
                String shoppingListId = ShoppingListEvents.getAutoSaveListId(delegator, dispatcher, null, userLogin, productStoreId);
                ShoppingListEvents.addListToCart(delegator, dispatcher, cart, productStoreId, shoppingListId, false, true, true);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                cart.setCartType("wishCart");
                String shoppingListId = ShoppingListEvents.getWishListId(delegator, dispatcher, null, userLogin, productStoreId);
                ShoppingListEvents.addListToCart(delegator, dispatcher, cart, productStoreId, shoppingListId, false, true, true);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                cart.setCartType("buyCart");
                String shoppingListId = ShoppingListEvents.getBuyListId(delegator, dispatcher, null, userLogin, productStoreId);
                ShoppingListEvents.addListToCart(delegator, dispatcher, cart, productStoreId, shoppingListId, false, true, true);
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return cart;
    }
    
    /**
     * Empty the shopping cart.
     */
    public static String clearCart(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        String cartType = request.getParameter("cartType");
        // 在checkout 时,必须把cart 放到request中,这样可以保持cart 的数据变化而不会丢失,如果从shoppinglist取则还是旧数据。
        if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
            cart.clear();
            request.removeAttribute("shoppingCart");
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
            cart.wishCartClear();
            request.removeAttribute("wishCart");
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
            cart.buyCartClear();
            request.removeAttribute("buyCart");
        }
        
        Map<String, String> resultData = FastMap.newInstance();
        resultData.put("message", "清空购物车成功");
        resultData.put("result", "1");
        request.setAttribute("resultData", resultData);
        
        return "success";
    }
    
    /**
     * Main get cart method; uses the locale & currency from the session
     */
    public static ShoppingCart getCartObject(HttpServletRequest request) {
        return getCartObject(request, null, null);
    }
    
    
    /**
     * Update the items in the shopping cart.
     */
    public static String modifyCart(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultData = FastMap.newInstance();
        ShoppingCart cart = getCartObject(request);
        Locale locale = UtilHttp.getLocale(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(null, dispatcher, cart);
        String product_id = request.getParameter("product_id");
        String quantity = request.getParameter("quantity");
        try {
            String cartType = request.getParameter("cartType");
            cartHelper.modifyCart(product_id, quantity, locale);
            //存储shoppingcart 到 shippinglist
            
            // 在checkout 时,必须把cart 放到request中,这样可以保持cart 的数据变化而不会丢失,如果从shoppinglist取则还是旧数据。
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                ShoppingListEvents.fillAutoSaveList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                ShoppingListEvents.fillWishList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                ShoppingListEvents.fillBuyList(cartHelper.getCartObject(), dispatcher);
            }
            
            
            //Determine where to send the browser
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", "修改购物车商品失败");
            resultData.put("result", "0");
            return "error";
        } catch (GeneralException e) {
            e.printStackTrace();
            resultData.put("message", "修改购物车商品失败");
            resultData.put("result", "0");
            return "error";
        }
        
        
        request.setAttribute("resultData", resultData);
        resultData.put("message", "修改购物车商品成功");
        resultData.put("result", "1");
        return "success";
        
        
    }
    
    
    /**
     * Update the items in the shopping cart.
     */
    public static String deleteCart(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultData = FastMap.newInstance();
        ShoppingCart cart = getCartObject(request);
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(null, dispatcher, cart);
        
        
        String productIds = request.getParameter("product_ids");
        if (UtilValidate.isNotEmpty(productIds)) {
            String[] productIdsArray = productIds.split(",");
            try {
                cartHelper.deleteCart(productIdsArray);
                //存储shoppingcart 到 shippinglist
                String cartType = request.getParameter("cartType");
                if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                    ShoppingListEvents.fillAutoSaveList(cartHelper.getCartObject(), dispatcher);
                } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                    ShoppingListEvents.fillWishList(cartHelper.getCartObject(), dispatcher);
                } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                    ShoppingListEvents.fillBuyList(cartHelper.getCartObject(), dispatcher);
                }
                
                
                //Determine where to send the browser
            } catch (GenericEntityException e) {
                e.printStackTrace();
                resultData.put("message", "删除购物车商品失败");
                resultData.put("result", "0");
                return "error";
            } catch (GeneralException e) {
                e.printStackTrace();
                resultData.put("message", "删除购物车商品失败");
                resultData.put("result", "0");
                return "error";
            }
        } else {
            resultData.put("message", "请选择要删除的购物车商品");
            resultData.put("result", "0");
            return "error";
        }
        
        request.setAttribute("resultData", resultData);
        resultData.put("message", "删除购物车商品成功");
        resultData.put("result", "1");
        return "success";
        
        
    }
    
    /**
     * Update the items in the shopping cart.
     */
    public static String chooseProductToCart(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultData = FastMap.newInstance();
        
        List<StringMap> chooseList = (List) request.getAttribute("payLoadObj");
        for (StringMap choose : chooseList) {
            String cartType = (String) choose.get("cartType");
            if (UtilValidate.isNotEmpty(cartType)) {
                request.setAttribute("cartType", cartType);
            }
        }
        ShoppingCart cart = getCartObject(request);
        for (StringMap choose : chooseList) {
            String productId = (String) choose.get("product_id");
            String isChoose = (String) choose.get("isChoose");
            if (UtilValidate.isNotEmpty(productId) && UtilValidate.isNotEmpty(isChoose)) {
                List<ShoppingCartItem> items = cart.findAllCartItems(productId);
                for (ShoppingCartItem item : items) {
                    item.setIsChoose(isChoose);
                }
            }
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(null, dispatcher, cart);
        
        try {
            // 在checkout 时,必须把cart 放到request中,这样可以保持cart 的数据变化而不会丢失,如果从shoppinglist取则还是旧数据。
            String cartType = cart.getCartType();
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                ShoppingListEvents.fillAutoSaveList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                ShoppingListEvents.fillWishList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                ShoppingListEvents.fillBuyList(cartHelper.getCartObject(), dispatcher);
            }
        } catch (GeneralException e) {
            e.printStackTrace();
            resultData.put("message", "购物车商品选中状态修改失败:" + e.getMessage());
            resultData.put("result", "0");
            return "error";
        }
        
        
        request.setAttribute("resultData", resultData);
        resultData.put("message", "购物车商品选中状态修改成功");
        resultData.put("result", "1");
        return "success";
        
        
    }
    
    /**
     * Event to add an item to the shopping cart.
     */
    public static String addProductsToCart(HttpServletRequest request, HttpServletResponse response) {
        String cartType = request.getParameter("cartType");
        //礼品、团购、秒杀每次增加的时候清除之前的购物车信息
        if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
        
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
        
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
            clearCart(request, response);
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective = null;
        Map<String, Object> result = null;
        
        String parentProductId = null;
        String itemType = null;
        String itemDescription = null;
        String productCategoryId = null;
        
        BigDecimal price = null;
        
        BigDecimal quantity = BigDecimal.ZERO;
        String reservStartStr = null;
        String reservEndStr = null;
        Timestamp reservStart = null;
        Timestamp reservEnd = null;
        String reservLengthStr = null;
        BigDecimal reservLength = null;
        String reservPersonsStr = null;
        BigDecimal reservPersons = null;
        String accommodationMapId = null;
        String accommodationSpotId = null;
        String shipBeforeDateStr = null;
        String shipAfterDateStr = null;
        Timestamp shipBeforeDate = null;
        Timestamp shipAfterDate = null;
        String numberOfDay = null;
        
        // not used right now: Map attributes = null;
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        Locale locale = UtilHttp.getLocale(request);
        
        // Get the parameters as a MAP, remove the productId and quantity params.
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        
        String itemGroupNumber = (String) paramMap.get("itemGroupNumber");
        
        // Get shoppingList info if passed
        String shoppingListId = (String) paramMap.get("shoppingListId");
        String shoppingListItemSeqId = (String) paramMap.get("shoppingListItemSeqId");
        
        String productIds = (String) paramMap.remove("productIds");
        String productPrices = (String) paramMap.remove("productPrices");
        String productNums = (String) paramMap.remove("productNums");
        
        if (UtilValidate.isNotEmpty(productIds) && UtilValidate.isNotEmpty(productPrices) && UtilValidate.isNotEmpty(productNums)) {
            String[] productIdArray = productIds.split(",");
            String[] productPriceArray = productPrices.split(",");
            String[] productNumArray = productNums.split(",");
            for (int i = 0; i < productIdArray.length; i++) {
                String productId = productIdArray[i];
                String priceStr = productPriceArray[i];
                String quantityStr = productNumArray[i];
                // Get the ProductConfigWrapper (it's not null only for configurable items)
                ProductConfigWrapper configWrapper = null;
                configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, cart.getCurrency(), request);
                
                if (configWrapper != null) {
                    if (paramMap.containsKey("configId")) {
                        try {
                            configWrapper.loadConfig(delegator, (String) paramMap.remove("configId"));
                        } catch (Exception e) {
                            Debug.logWarning(e, "Could not load configuration", module);
                        }
                    } else {
                        // The choices selected by the user are taken from request and set in the wrapper
                        ProductConfigWorker.fillProductConfigWrapper(configWrapper, request);
                    }
                    if (!configWrapper.isCompleted()) {
                        // The configuration is not valid
                        request.setAttribute("product_id", productId);
                        request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.configureProductBeforeAddingToCart", locale));
                        return "error";
                    } else {
                        // load the Config Id
                        ProductConfigWorker.storeProductConfigWrapper(configWrapper, delegator);
                    }
                }
                
                
                //Check for virtual products
                if (ProductWorker.isVirtual(delegator, productId)) {
                    
                    
                    // get the selected features.
                    List<String> selectedFeatures = new LinkedList<String>();
                    Enumeration<String> paramNames = UtilGenerics.cast(request.getParameterNames());
                    while (paramNames.hasMoreElements()) {
                        String paramName = paramNames.nextElement();
                        if (paramName.startsWith("FT")) {
                            selectedFeatures.add(request.getParameterValues(paramName)[0]);
                        }
                    }
                    
                    // check if features are selected
                    if (UtilValidate.isEmpty(selectedFeatures)) {
                        request.setAttribute("paramMap", paramMap);
                        request.setAttribute("product_id", productId);
                        request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.chooseVariationBeforeAddingToCart", locale));
                        return "error";
                    }
                    
                    String variantProductId = ProductWorker.getVariantFromFeatureTree(productId, selectedFeatures, delegator);
                    if (UtilValidate.isNotEmpty(variantProductId)) {
                        productId = variantProductId;
                    } else {
                        request.setAttribute("paramMap", paramMap);
                        request.setAttribute("product_id", productId);
                        request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.incompatibilityVariantFeature", locale));
                        return "error";
                    }
                    
                    
                }
                
                
                if (priceStr == null) {
                    priceStr = "0";  // default price is 0
                }
                
                if ("ASSET_USAGE_OUT_IN".equals(ProductWorker.getProductTypeId(delegator, productId))) {
                    if (paramMap.containsKey("numberOfDay")) {
                        numberOfDay = (String) paramMap.remove("numberOfDay");
                        reservStart = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), 1);
                        reservEnd = UtilDateTime.addDaysToTimestamp(reservStart, Integer.valueOf(numberOfDay));
                    }
                }
                
                
                if (UtilValidate.isEmpty(quantityStr)) {
                    quantityStr = "1";  // default quantity is 1
                }
                
                // parse the price
                try {
                    price = (BigDecimal) ObjectType.simpleTypeConvert(priceStr, "BigDecimal", null, locale);
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing price string: " + priceStr, module);
                    price = null;
                }
                
                // parse the quantity
                try {
                    quantity = (BigDecimal) ObjectType.simpleTypeConvert(quantityStr, "BigDecimal", null, locale);
                    //For quantity we should test if we allow to add decimal quantity for this product an productStore : if not then round to 0
//            if (!ProductWorker.isDecimalQuantityOrderAllowed(delegator, productId, cart.getProductStoreId())) {
                    quantity = quantity.setScale(0, UtilNumber.getBigDecimalRoundingMode("order.rounding"));
//            } else {
//                quantity = quantity.setScale(UtilNumber.getBigDecimalScale("order.decimals"), UtilNumber.getBigDecimalRoundingMode("order.rounding"));
//            }
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityStr, module);
                    quantity = BigDecimal.ONE;
                }
                
                // get the selected amount
                String selectedAmountStr = null;
                if (paramMap.containsKey("ADD_AMOUNT")) {
                    selectedAmountStr = (String) paramMap.remove("ADD_AMOUNT");
                } else if (paramMap.containsKey("add_amount")) {
                    selectedAmountStr = (String) paramMap.remove("add_amount");
                }
                
                // parse the amount
                BigDecimal amount = null;
                if (UtilValidate.isNotEmpty(selectedAmountStr)) {
                    try {
                        amount = (BigDecimal) ObjectType.simpleTypeConvert(selectedAmountStr, "BigDecimal", null, locale);
                    } catch (Exception e) {
                        Debug.logWarning(e, "Problem parsing amount string: " + selectedAmountStr, module);
                        amount = null;
                    }
                } else {
                    amount = BigDecimal.ZERO;
                }
                
                // check for required amount
                if ((ProductWorker.isAmountRequired(delegator, productId)) && (amount == null || amount.doubleValue() == 0.0)) {
                    request.setAttribute("product_id", productId);
                    request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.enterAmountBeforeAddingToCart", locale));
                    return "error";
                }
                
                // get the ship before date (handles both yyyy-mm-dd input and full timestamp)
                shipBeforeDateStr = (String) paramMap.remove("shipBeforeDate");
                if (UtilValidate.isNotEmpty(shipBeforeDateStr)) {
                    if (shipBeforeDateStr.length() == 10) shipBeforeDateStr += " 00:00:00.000";
                    try {
                        shipBeforeDate = Timestamp.valueOf(shipBeforeDateStr);
                    } catch (IllegalArgumentException e) {
                        Debug.logWarning(e, "Bad shipBeforeDate input: " + e.getMessage(), module);
                        shipBeforeDate = null;
                    }
                }
                
                // get the ship after date (handles both yyyy-mm-dd input and full timestamp)
                shipAfterDateStr = (String) paramMap.remove("shipAfterDate");
                if (UtilValidate.isNotEmpty(shipAfterDateStr)) {
                    if (shipAfterDateStr.length() == 10) shipAfterDateStr += " 00:00:00.000";
                    try {
                        shipAfterDate = Timestamp.valueOf(shipAfterDateStr);
                    } catch (IllegalArgumentException e) {
                        Debug.logWarning(e, "Bad shipAfterDate input: " + e.getMessage(), module);
                        shipAfterDate = null;
                    }
                }
                // Translate the parameters and add to the cart
                result = cartHelper.addToCart(catalogId, shoppingListId, shoppingListItemSeqId, productId, productCategoryId,
                        itemType, itemDescription, price, amount, quantity, reservStart, reservLength, reservPersons,
                        accommodationMapId, accommodationSpotId,
                        shipBeforeDate, shipAfterDate, configWrapper, itemGroupNumber, paramMap, parentProductId);
                controlDirective = processResult(result, request);
                
                Integer itemId = (Integer) result.get("itemId");
                if (UtilValidate.isNotEmpty(itemId)) {
                    request.setAttribute("itemId", itemId);
                }
                
            }
        }
        
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        if (UtilValidate.isNotEmpty(shippingContactMechId)) {
            cart.setAllShippingContactMechId(shippingContactMechId);
        }
        //存储shoppingcart 到 shippinglist
        try {
            
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                ShoppingListEvents.fillAutoSaveList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                ShoppingListEvents.fillWishList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                ShoppingListEvents.fillBuyList(cartHelper.getCartObject(), dispatcher);
            }
            
        } catch (GeneralException e) {
            e.printStackTrace();
        }
        
        // Determine where to send the browser
        if (UtilValidate.isNotEmpty(controlDirective) && controlDirective.equals(ERROR)) {
            return "error";
        } else {
            Map<String, String> resultData = FastMap.newInstance();
            resultData.put("message", "新增成功");
            resultData.put("result", "1");
            request.setAttribute("resultData", resultData);
            return "success";
        }
    }
    
    
    public static String addToCart(HttpServletRequest request, HttpServletResponse response) {
        
        String cartType = request.getParameter("cartType");
        //礼品、团购、秒杀每次增加的时候清除之前的购物车信息
        if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
        
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
        
        } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
            clearCart(request, response);
        }
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective = null;
        Map<String, Object> result = null;
        String productId = null;
        String parentProductId = null;
        String itemType = null;
        String itemDescription = null;
        String productCategoryId = null;
        String priceStr = null;
        BigDecimal price = null;
        String quantityStr = null;
        BigDecimal quantity = BigDecimal.ZERO;
        
        Timestamp reservStart = null;
        Timestamp reservEnd = null;
        String reservLengthStr = null;
        BigDecimal reservLength = null;
        String reservPersonsStr = null;
        BigDecimal reservPersons = null;
        String accommodationMapId = null;
        String accommodationSpotId = null;
        String shipBeforeDateStr = null;
        String shipAfterDateStr = null;
        Timestamp shipBeforeDate = null;
        Timestamp shipAfterDate = null;
        String numberOfDay = null;
        
        // not used right now: Map attributes = null;
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        Locale locale = UtilHttp.getLocale(request);
        
        // Get the parameters as a MAP, remove the productId and quantity params.
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        
        String itemGroupNumber = (String) paramMap.get("itemGroupNumber");
        
        // Get shoppingList info if passed
        String shoppingListId = (String) paramMap.get("shoppingListId");
        String shoppingListItemSeqId = (String) paramMap.get("shoppingListItemSeqId");
        if (paramMap.containsKey("ADD_PRODUCT_ID")) {
            productId = (String) paramMap.remove("ADD_PRODUCT_ID");
        } else if (paramMap.containsKey("add_product_id")) {
            Object object = paramMap.remove("add_product_id");
            try {
                productId = (String) object;
            } catch (ClassCastException e) {
                List<String> productList = UtilGenerics.checkList(object);
                productId = productList.get(0);
            }
        }
        if (paramMap.containsKey("PRODUCT_ID")) {
            parentProductId = (String) paramMap.remove("PRODUCT_ID");
        } else if (paramMap.containsKey("product_id")) {
            parentProductId = (String) paramMap.remove("product_id");
        }
        
        Debug.logInfo("adding item product " + productId, module);
        Debug.logInfo("adding item parent product " + parentProductId, module);
        
        if (paramMap.containsKey("ADD_CATEGORY_ID")) {
            productCategoryId = (String) paramMap.remove("ADD_CATEGORY_ID");
        } else if (paramMap.containsKey("add_category_id")) {
            productCategoryId = (String) paramMap.remove("add_category_id");
        }
        if (productCategoryId != null && productCategoryId.length() == 0) {
            productCategoryId = null;
        }
        
        if (paramMap.containsKey("ADD_ITEM_TYPE")) {
            itemType = (String) paramMap.remove("ADD_ITEM_TYPE");
        } else if (paramMap.containsKey("add_item_type")) {
            itemType = (String) paramMap.remove("add_item_type");
        }
        if (UtilValidate.isEmpty(productId)) {
            // before returning error; check make sure we aren't adding a special item type
            if (UtilValidate.isEmpty(itemType)) {
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.noProductInfoPassed", locale));
                return "error"; // not critical return to same page
            }
        } else {
            try {
                String pId = ProductWorker.findProductId(delegator, productId);
                if (pId != null) {
                    productId = pId;
                }
            } catch (Throwable e) {
                Debug.logWarning(e, module);
            }
        }
        
        // check for an itemDescription
        if (paramMap.containsKey("ADD_ITEM_DESCRIPTION")) {
            itemDescription = (String) paramMap.remove("ADD_ITEM_DESCRIPTION");
        } else if (paramMap.containsKey("add_item_description")) {
            itemDescription = (String) paramMap.remove("add_item_description");
        }
        if (itemDescription != null && itemDescription.length() == 0) {
            itemDescription = null;
        }
        
        // Get the ProductConfigWrapper (it's not null only for configurable items)
        ProductConfigWrapper configWrapper = null;
        configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, cart.getCurrency(), request);
        
        if (configWrapper != null) {
            if (paramMap.containsKey("configId")) {
                try {
                    configWrapper.loadConfig(delegator, (String) paramMap.remove("configId"));
                } catch (Exception e) {
                    Debug.logWarning(e, "Could not load configuration", module);
                }
            } else {
                // The choices selected by the user are taken from request and set in the wrapper
                ProductConfigWorker.fillProductConfigWrapper(configWrapper, request);
            }
            if (!configWrapper.isCompleted()) {
                // The configuration is not valid
                request.setAttribute("product_id", productId);
                request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.configureProductBeforeAddingToCart", locale));
                return "error";
            } else {
                // load the Config Id
                ProductConfigWorker.storeProductConfigWrapper(configWrapper, delegator);
            }
        }
        //Check for virtual products
        if (ProductWorker.isVirtual(delegator, productId)) {
            // get the selected features.
            List<String> selectedFeatures = new LinkedList<String>();
            Enumeration<String> paramNames = UtilGenerics.cast(request.getParameterNames());
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                if (paramName.startsWith("FT")) {
                    selectedFeatures.add(request.getParameterValues(paramName)[0]);
                }
            }
            
            // check if features are selected
            if (UtilValidate.isEmpty(selectedFeatures)) {
                request.setAttribute("paramMap", paramMap);
                request.setAttribute("product_id", productId);
                request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.chooseVariationBeforeAddingToCart", locale));
                return "error";
            }
            
            String variantProductId = ProductWorker.getVariantFromFeatureTree(productId, selectedFeatures, delegator);
            if (UtilValidate.isNotEmpty(variantProductId)) {
                productId = variantProductId;
            } else {
                request.setAttribute("paramMap", paramMap);
                request.setAttribute("product_id", productId);
                request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.incompatibilityVariantFeature", locale));
                return "error";
            }
            
            
        }
        
        // get the override price
        if (paramMap.containsKey("PRICE")) {
            priceStr = (String) paramMap.remove("PRICE");
        } else if (paramMap.containsKey("price")) {
            priceStr = (String) paramMap.remove("price");
        }
        
        if ("ASSET_USAGE_OUT_IN".equals(ProductWorker.getProductTypeId(delegator, productId))) {
            if (paramMap.containsKey("numberOfDay")) {
                numberOfDay = (String) paramMap.remove("numberOfDay");
                reservStart = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), 1);
                reservEnd = UtilDateTime.addDaysToTimestamp(reservStart, Integer.valueOf(numberOfDay));
            }
        }
        
        // get the quantity
        if (paramMap.containsKey("QUANTITY")) {
            quantityStr = (String) paramMap.remove("QUANTITY");
        } else if (paramMap.containsKey("quantity")) {
            quantityStr = (String) paramMap.remove("quantity");
        }
        if (UtilValidate.isEmpty(quantityStr)) {
            quantityStr = "1";  // default quantity is 1
        }
        
        // parse the price
        try {
            price = (BigDecimal) ObjectType.simpleTypeConvert(priceStr, "BigDecimal", null, locale);
        } catch (Exception e) {
            Debug.logWarning(e, "Problems parsing price string: " + priceStr, module);
            price = null;
        }
        
        // parse the quantity
        try {
            quantity = (BigDecimal) ObjectType.simpleTypeConvert(quantityStr, "BigDecimal", null, locale);
            //For quantity we should test if we allow to add decimal quantity for this product an productStore : if not then round to 0
//            if (!ProductWorker.isDecimalQuantityOrderAllowed(delegator, productId, cart.getProductStoreId())) {
            quantity = quantity.setScale(0, UtilNumber.getBigDecimalRoundingMode("order.rounding"));
//            } else {
//                quantity = quantity.setScale(UtilNumber.getBigDecimalScale("order.decimals"), UtilNumber.getBigDecimalRoundingMode("order.rounding"));
//            }
        } catch (Exception e) {
            Debug.logWarning(e, "Problems parsing quantity string: " + quantityStr, module);
            quantity = BigDecimal.ONE;
        }
        
        // get the selected amount
        String selectedAmountStr = null;
        if (paramMap.containsKey("ADD_AMOUNT")) {
            selectedAmountStr = (String) paramMap.remove("ADD_AMOUNT");
        } else if (paramMap.containsKey("add_amount")) {
            selectedAmountStr = (String) paramMap.remove("add_amount");
        }
        
        // parse the amount
        BigDecimal amount = null;
        if (UtilValidate.isNotEmpty(selectedAmountStr)) {
            try {
                amount = (BigDecimal) ObjectType.simpleTypeConvert(selectedAmountStr, "BigDecimal", null, locale);
            } catch (Exception e) {
                Debug.logWarning(e, "Problem parsing amount string: " + selectedAmountStr, module);
                amount = null;
            }
        } else {
            amount = BigDecimal.ZERO;
        }
        
        // check for required amount
        if ((ProductWorker.isAmountRequired(delegator, productId)) && (amount == null || amount.doubleValue() == 0.0)) {
            request.setAttribute("product_id", productId);
            request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resource_error, "cart.addToCart.enterAmountBeforeAddingToCart", locale));
            return "error";
        }
        
        // get the ship before date (handles both yyyy-mm-dd input and full timestamp)
        shipBeforeDateStr = (String) paramMap.remove("shipBeforeDate");
        if (UtilValidate.isNotEmpty(shipBeforeDateStr)) {
            if (shipBeforeDateStr.length() == 10) shipBeforeDateStr += " 00:00:00.000";
            try {
                shipBeforeDate = Timestamp.valueOf(shipBeforeDateStr);
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e, "Bad shipBeforeDate input: " + e.getMessage(), module);
                shipBeforeDate = null;
            }
        }
        
        // get the ship after date (handles both yyyy-mm-dd input and full timestamp)
        shipAfterDateStr = (String) paramMap.remove("shipAfterDate");
        if (UtilValidate.isNotEmpty(shipAfterDateStr)) {
            if (shipAfterDateStr.length() == 10) shipAfterDateStr += " 00:00:00.000";
            try {
                shipAfterDate = Timestamp.valueOf(shipAfterDateStr);
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e, "Bad shipAfterDate input: " + e.getMessage(), module);
                shipAfterDate = null;
            }
        }
        // Translate the parameters and add to the cart
        result = cartHelper.addToCart(catalogId, shoppingListId, shoppingListItemSeqId, productId, productCategoryId,
                itemType, itemDescription, price, amount, quantity, reservStart, reservLength, reservPersons,
                accommodationMapId, accommodationSpotId,
                shipBeforeDate, shipAfterDate, configWrapper, itemGroupNumber, paramMap, parentProductId);
        controlDirective = processResult(result, request);
        
        Integer itemId = (Integer) result.get("itemId");
        if (UtilValidate.isNotEmpty(itemId)) {
            request.setAttribute("itemId", itemId);
        }
        
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        if (UtilValidate.isNotEmpty(shippingContactMechId)) {
            cart.setAllShippingContactMechId(shippingContactMechId);
        }
        
        //存储shoppingcart 到 shippinglist
        try {
            
            if (UtilValidate.isEmpty(cartType) || cartType.equals("shoppingCart")) {
                ShoppingListEvents.fillAutoSaveList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("wishCart")) {
                ShoppingListEvents.fillWishList(cartHelper.getCartObject(), dispatcher);
            } else if (UtilValidate.isNotEmpty(cartType) && cartType.equals("buyCart")) {
                ShoppingListEvents.fillBuyList(cartHelper.getCartObject(), dispatcher);
            }
            
        } catch (GeneralException e) {
            e.printStackTrace();
        }
        
        // Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            Map<String, String> resultData = FastMap.newInstance();
            resultData.put("message", (String) result.get("errorMessage"));
            resultData.put("result", "0");
            request.setAttribute("resultData", resultData);
            return "error";
        } else {
            Map<String, String> resultData = FastMap.newInstance();
            resultData.put("message", "新增成功");
            resultData.put("result", "1");
            request.setAttribute("resultData", resultData);
            return "success";
        }
    }
    
    /**
     * Totally wipe out the cart, removes all stored info.
     */
    public static String destroyCart(HttpServletRequest request, HttpServletResponse response) {
        clearCart(request, response);
        //delete shippinglist
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userName = ShoppingCart.getUserNameFromRequest(request);
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            List exprList = FastList.newInstance();
            exprList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, userLogin.get("partyId")));
            exprList.add(EntityCondition.makeCondition("listName", EntityOperator.NOT_EQUAL, "auto-save"));
            exprList.add(EntityCondition.makeCondition("shoppingListTypeId", EntityOperator.EQUALS, "SLT_M_SHOP_LIST"));
            EntityCondition condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
            delegator.removeByCondition("ShoppingList", condition);
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }
    
    /**
     * This should be called to translate the error messages of the
     * <code>ShoppingCartHelper</code> to an appropriately formatted
     * <code>String</code> in the request object and indicate whether
     * the result was an error or not and whether the errors were
     * critical or not
     *
     * @param result  The result returned from the
     *                <code>ShoppingCartHelper</code>
     * @param request The servlet request instance to set the error messages
     *                in
     * @return one of NON_CRITICAL_ERROR, ERROR or NO_ERROR.
     */
    public static String processResult(Map<String, Object> result, HttpServletRequest request) {
        //Check for errors
        StringBuilder errMsg = new StringBuilder();
        if (result.containsKey(ModelService.ERROR_MESSAGE_LIST)) {
            List<String> errorMsgs = UtilGenerics.checkList(result.get(ModelService.ERROR_MESSAGE_LIST));
            Iterator<String> iterator = errorMsgs.iterator();
            while (iterator.hasNext()) {
                errMsg.append(iterator.next() + "\n");
            }
            
        } else if (result.containsKey(ModelService.ERROR_MESSAGE)) {
            errMsg.append(result.get(ModelService.ERROR_MESSAGE));
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
        }
        
        //See whether there was an error
        if (errMsg.length() > 0) {
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
            if (result.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                return NON_CRITICAL_ERROR;
            } else {
                return ERROR;
            }
        } else {
            return NO_ERROR;
        }
    }
    
    
    /**
     * 获取购物车信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getShoppingCart(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart shoppingCart = ShoppingCartEvents.getCartObject(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
//        System.out.println("go to 1 = ");
        //购物车信息
        Map cartInfo = FastMap.newInstance();
        
        String cartType = request.getParameter("cartType");
        
        List<GenericValue> orderItems = shoppingCart.makeOrderItems();
        
        String innerStoreId = null;
        try {
            GenericValue inParty = EntityUtil.getFirst(delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y")));
            innerStoreId = inParty.getString("productStoreId");
        } catch (GenericEntityException e) {
        
        }
        //积分兑奖比例
        GenericValue integralPerMoney = null;//积分抵现规则表
        try {
            integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        //店铺促销信息、用户收货地址信息
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        if (UtilValidate.isEmpty(shippingContactMechId)) {
            //取用户的默认收货地址
            GenericValue userLogin = shoppingCart.getUserLogin();
            String partyId = userLogin.getString("partyId");
            try {
                GenericValue profileDefault = delegator.findByPrimaryKey("PartyProfileDefault", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(profileDefault)) {
                    shippingContactMechId = profileDefault.getString("defaultShipAddr");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        
        
        
        Map<String, Map<String, Object>> productStoreCartMap = FastMap.newInstance();
        Map<String, Object> productStoreCartObj = null;
    
    
    
//        System.out.println("go to 2 = ");
        
        ProductPromoWorker.doPromotions(shoppingCart, dispatcher);
//        System.out.println("go to 3 = ");
        //如果有收获地址的情况计算运费
        if (UtilValidate.isNotEmpty(shippingContactMechId)) {
            shoppingCart.setAllShippingContactMechId(shippingContactMechId);
            //先计算运费,每个shipinfo对应一个店铺的运费，计算总计
            List<org.ofbiz.order.shoppingcart.ShoppingCart.CartShipInfo> shipGroups = shoppingCart.getShipGroups();
            for (org.ofbiz.order.shoppingcart.ShoppingCart.CartShipInfo shipInfo : shipGroups) {
            
                Map<String, Object> result = getShipGroupEstimate(delegator, shipInfo);
                if (result.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                    return "error";
                }
                BigDecimal shippingTotal = (BigDecimal) result.get("shippingTotal");
                if (shippingTotal == null) {
                    shippingTotal = BigDecimal.ZERO;
                }
                shoppingCart.setItemShipGroupEstimate(shippingTotal, shipInfo.getProductStoreId());
            }
        }
        List<GenericValue> orderAdjustments = shoppingCart.makeAllAdjustments();
//        System.out.println("go to 4 = ");
    
        BigDecimal toalQuantity = BigDecimal.ZERO;
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.items();
        if (UtilValidate.isNotEmpty(shoppingCartItems)) {
            for (int i = 0; i < shoppingCartItems.size(); i++) {
                ShoppingCartItem cartLine = shoppingCartItems.get(i);
                if (UtilValidate.isNotEmpty(cartLine.getIsChoose()) && cartLine.getIsChoose().equalsIgnoreCase("Y")) {
                    toalQuantity = toalQuantity.add(cartLine.getQuantity());
                }
            }
        }
//        System.out.println("go to 5 = ");
        BigDecimal totalCouponAmount = BigDecimal.ZERO;
        
        if (UtilValidate.isNotEmpty(orderAdjustments)) {
            for (int i = 0; i < orderAdjustments.size(); i++) {
                GenericValue orderAdjustment = orderAdjustments.get(i);
                BigDecimal amount = orderAdjustment.getBigDecimal("amount");
                totalCouponAmount = totalCouponAmount.add(amount);
            }
        }
        //显示店铺的购物车信息
//        System.out.println("go to 6 = ");
        if (UtilValidate.isNotEmpty(shoppingCartItems)) {
            String baseUrl = UtilProperties.getMessage("application.properties", "image.base.url", Locale.CHINA);
            String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
            if(uploadType.equals("FTP")){
                baseUrl = "";
            }
            for (int i = 0; i < shoppingCartItems.size(); i++) {
                ShoppingCartItem cartLine = shoppingCartItems.get(i);
                String productStoreId = cartLine.getProductStoreId();
                if (productStoreCartMap.get(productStoreId) != null) {
                    productStoreCartObj = productStoreCartMap.get(productStoreId);
                } else {
                    productStoreCartObj = FastMap.newInstance();
                    productStoreCartMap.put(productStoreId, productStoreCartObj);
                }
                Map<String, Object> cartItemObj = FastMap.newInstance();
                cartItemObj.put("quantity", cartLine.getQuantity());
                cartItemObj.put("itemSeqId", cartLine.getOrderItemSeqId());
                cartItemObj.put("isGift", cartLine.getAttribute("isGift"));
                cartItemObj.put("isGroupProduct", cartLine.getAttribute("isGroupProduct"));
                cartItemObj.put("name", cartLine.getName());
                cartItemObj.put("description", cartLine.getItemTypeDescription());
//                cartItemObj.put("createDate", )
                cartItemObj.put("displayPrice", cartLine.getDisplayPrice());
                String isVariant = cartLine.getProduct().getString("isVariant");
                if (UtilValidate.isNotEmpty(isVariant) && isVariant.equalsIgnoreCase("Y")) {
                    cartItemObj.put("features", cartLine.getFeatureSet());
                }
                GenericValue miniProduct = cartLine.getProduct();
                ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(miniProduct, request);
                String mediumImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
                if(UtilValidate.isEmpty(mediumImageUrl)){
                    mediumImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                }
                cartItemObj.put("mediumImageUrl", baseUrl + mediumImageUrl);
               
                
                
                cartItemObj.put("isChoose", cartLine.getIsChoose());
                //明细合计
                cartItemObj.put("itemSubTotal", cartLine.getDisplayItemSubTotal());
                String productId = cartLine.getProductId();
                List<GenericValue> serviceAssoc = null;
                try {
                    serviceAssoc = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(serviceAssoc)) {
                    List<String> serviceTags = FastList.newInstance();
                    for (int j = 0; j < serviceAssoc.size(); j++) {
                        GenericValue assoc = serviceAssoc.get(j);
                        serviceTags.add(assoc.getString("enumId"));
                    }
                    cartItemObj.put("serviceTag", serviceTags);
                }
                cartItemObj.put("productTypeId", miniProduct.get("productTypeId"));
                cartItemObj.put("isBondedGoods", miniProduct.get("isBondedGoods"));
                cartItemObj.put("integralDeductionType", miniProduct.get("integralDeductionType"));//1.不可使用积分2:百分比抵扣3:固定金额抵扣
                cartItemObj.put("integralDeductionUpper", miniProduct.get("integralDeductionUpper")); //积分抵扣数
                cartItemObj.put("voucherAmount", miniProduct.get("voucherAmount"));//代金劵面值
                cartItemObj.put("isSku", miniProduct.get("isVirtual"));
                cartItemObj.put("isPromo", cartLine.getIsPromo() ?"Y":"N");
                
                /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
                Long integralValue = integralPerMoney.getLong("integralValue");
                String integralDeductionType = miniProduct.getString("integralDeductionType");
                BigDecimal integralDeductionUpper = miniProduct.getBigDecimal("integralDeductionUpper");
                if (integralDeductionType.equalsIgnoreCase("2")) {
                    BigDecimal scorePrice = cartLine.getBasePrice().multiply(integralDeductionUpper).divide(new BigDecimal(100));
                    BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                    cartItemObj.put("scorePrice", scorePrice); //积分抵扣的金额
                    cartItemObj.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
                } else if (integralDeductionType.equalsIgnoreCase("3")) {
                    cartItemObj.put("scorePrice", integralDeductionUpper);//积分抵扣的金额
                    cartItemObj.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
                }
                
                //variant product
                cartItemObj.put("productId", productId);
                if (UtilValidate.isNotEmpty(productId)) {
                    String parentProductId = cartLine.getParentProductId();
                    cartItemObj.put("parentProductId", parentProductId);
                }
                //variant product feature
                ProductConfigWrapper configWrapper = cartLine.getConfigWrapper();
                if (UtilValidate.isNotEmpty(configWrapper)) {
                    List<ProductConfigWrapper.ConfigOption> features = cartLine.getConfigWrapper().getSelectedOptions();
                    cartItemObj.put("features", features);
                }
                
                if (productStoreCartObj.get("shoppingCartItems") != null) {
                    List cartItems = (List) productStoreCartObj.get("shoppingCartItems");
                    if (UtilValidate.isNotEmpty(cartItems)) {
                        cartItems.add(cartItemObj);
                    } else {
                        cartItems = FastList.newInstance();
                        cartItems.add(cartItemObj);
                        productStoreCartObj.put("shoppingCartItems", cartItems);
                    }
                } else {
                    List cartItems = FastList.newInstance();
                    cartItems.add(cartItemObj);
                    productStoreCartObj.put("shoppingCartItems", cartItems);
                }
                productStoreCartObj.put("productStoreId", cartLine.getProductStoreId());
                productStoreCartObj.put("productStoreName", cartLine.getProductStoreName());
                productStoreCartObj.put("innerStoreId", innerStoreId);
            }
            
            //去除productPromoId相同的
            Map<String, GenericValue> filterOrderAdjustmentsMap = FastMap.newInstance();
            
            if (UtilValidate.isNotEmpty(orderAdjustments)) {
                String productPromoId = "";
                for (int i = 0; i < orderAdjustments.size(); i++) {
                    GenericValue orderAdjustment = orderAdjustments.get(i);
                    productPromoId = orderAdjustment.getString("productPromoId");
                    if (UtilValidate.isEmpty(filterOrderAdjustmentsMap.get(productPromoId))) {
                        filterOrderAdjustmentsMap.put(productPromoId, orderAdjustment);
                    }
                }
            }
            List<GenericValue> filterOrderAdjustments = FastList.newInstance();
            if (UtilValidate.isNotEmpty(filterOrderAdjustmentsMap)) {
                Iterator keyIter = filterOrderAdjustmentsMap.keySet().iterator();
                while (keyIter.hasNext()) {
                    String promoId = (String) keyIter.next();
                    filterOrderAdjustments.add(filterOrderAdjustmentsMap.get(promoId));
                }
            }
            if (UtilValidate.isNotEmpty(productStoreCartMap)) {
                Set<String> proSet = productStoreCartMap.keySet();
                for (String proStoreId : proSet) {
                    List<GenericValue> storeOrderAdjustments = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(filterOrderAdjustments)) {
                        for (int i = 0; i < filterOrderAdjustments.size(); i++) {
                            GenericValue orderAdjustment = filterOrderAdjustments.get(i);
                            if (orderAdjustment.get("productStoreId").equals(proStoreId)) {
                                storeOrderAdjustments.add(orderAdjustment);
                            }
                        }
                    }
                    productStoreCartMap.get(proStoreId).put("storeOrderAdjustments", storeOrderAdjustments);
                }
                
            }
        }
//        System.out.println("go to 7 = ");
        //订单调整价
        cartInfo.put("orderPromoTotal", totalCouponAmount);
        cartInfo.put("orderShippingTotal", shoppingCart.getTotalShipping());
        //cart 订单项的价格
        BigDecimal displaySubTotal = shoppingCart.getDisplaySubTotal();
        //购物车小计
        cartInfo.put("displaySubTotal", displaySubTotal);
        //cart 运费+订单调整+subtotal
        BigDecimal displayGrandTotal = shoppingCart.getDisplayGrandTotal1();
        cartInfo.put("displayGrandTotal", displayGrandTotal);
        //总的商品金额不包括促销
//        System.out.println("go to 8 = ");
        cartInfo.put("orderSubTotal", OrderReadHelper.getOrderItemsSubTotal(orderItems, null));
        
        cartInfo.put("orderShippingTotal", shoppingCart.getTotalShipping());
//        System.out.println("go to 9 = ");
        cartInfo.put("totalQuantity", toalQuantity);
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("storeCarts", productStoreCartMap.values());
        resultData.put("shoppingCart", cartInfo);
        resultData.put("retCode", 1);
        resultData.put("message", "查询购物车成功");
        request.setAttribute(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    
    //    下面是token应用使用
    public static String getShoppingCartTotal(HttpServletRequest request, HttpServletResponse response) {
        
        ShoppingCart shoppingCart = ShoppingCartEvents.getCartObject(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //购物车信息
        Map cartInfo = FastMap.newInstance();
        
        String cartType = request.getParameter("cartType");
        
        List<GenericValue> orderItems = shoppingCart.makeOrderItems();
        
        String innerStoreId = null;
        try {
            GenericValue inParty = EntityUtil.getFirst(delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y")));
            innerStoreId = inParty.getString("productStoreId");
        } catch (GenericEntityException e) {
        
        }
        //积分兑奖比例
        GenericValue integralPerMoney = null;//积分抵现规则表
        try {
            integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        //店铺促销信息、用户收货地址信息
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        if (UtilValidate.isEmpty(shippingContactMechId)) {
            //取用户的默认收货地址
            GenericValue userLogin = shoppingCart.getUserLogin();
            String partyId = userLogin.getString("partyId");
            try {
                GenericValue profileDefault = delegator.findByPrimaryKey("PartyProfileDefault", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(profileDefault)) {
                    shippingContactMechId = profileDefault.getString("defaultShipAddr");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
       
        
        
        Map<String, Map<String, Object>> productStoreCartMap = FastMap.newInstance();
        Map<String, Object> productStoreCartObj = null;
        
        
        BigDecimal toalQuantity = BigDecimal.ZERO;
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.items();
        if (UtilValidate.isNotEmpty(shoppingCartItems)) {
            for (int i = 0; i < shoppingCartItems.size(); i++) {
                ShoppingCartItem cartLine = shoppingCartItems.get(i);
                if (UtilValidate.isNotEmpty(cartLine.getIsChoose()) && cartLine.getIsChoose().equalsIgnoreCase("Y")) {
                    toalQuantity = toalQuantity.add(cartLine.getQuantity());
                }
            }
        }
        
        ProductPromoWorker.doPromotions(shoppingCart, dispatcher);
    
        //促销后计算邮费，如果有收获地址的情况计算运费
        if (UtilValidate.isNotEmpty(shippingContactMechId)) {
            shoppingCart.setAllShippingContactMechId(shippingContactMechId);
            //先计算运费,每个shipinfo对应一个店铺的运费，计算总计
            List<org.ofbiz.order.shoppingcart.ShoppingCart.CartShipInfo> shipGroups = shoppingCart.getShipGroups();
            for (org.ofbiz.order.shoppingcart.ShoppingCart.CartShipInfo shipInfo : shipGroups) {
            
                Map<String, Object> result = getShipGroupEstimate(delegator, shipInfo);
                if (result.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                    return "error";
                }
                BigDecimal shippingTotal = (BigDecimal) result.get("shippingTotal");
                if (shippingTotal == null) {
                    shippingTotal = BigDecimal.ZERO;
                }
                shoppingCart.setItemShipGroupEstimate(shippingTotal, shipInfo.getProductStoreId());
            }
        }
        
        
        List<GenericValue> orderAdjustments = shoppingCart.makeAllAdjustments();
        
        BigDecimal totalCouponAmount = BigDecimal.ZERO;
        
        if (UtilValidate.isNotEmpty(orderAdjustments)) {
            for (int i = 0; i < orderAdjustments.size(); i++) {
                GenericValue orderAdjustment = orderAdjustments.get(i);
                BigDecimal amount = orderAdjustment.getBigDecimal("amount");
                totalCouponAmount = totalCouponAmount.add(amount);
            }
        }
        //显示店铺的购物车信息
        shoppingCartItems = shoppingCart.items();
        if (UtilValidate.isNotEmpty(shoppingCartItems)) {
            String baseUrl = UtilProperties.getMessage("application.properties", "image.base.url", Locale.CHINA);
            String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
            if(uploadType.equals("FTP")){
                baseUrl = "";
            }
            for (int i = 0; i < shoppingCartItems.size(); i++) {
                ShoppingCartItem cartLine = shoppingCartItems.get(i);
                String productStoreId = cartLine.getProductStoreId();
                if (productStoreCartMap.get(productStoreId) != null) {
                    productStoreCartObj = productStoreCartMap.get(productStoreId);
                } else {
                    productStoreCartObj = FastMap.newInstance();
                    productStoreCartMap.put(productStoreId, productStoreCartObj);
                }
                Map<String, Object> cartItemObj = FastMap.newInstance();
                cartItemObj.put("quantity", cartLine.getQuantity());
                cartItemObj.put("isGift", cartLine.getAttribute("isGift"));
                cartItemObj.put("isGroupProduct", cartLine.getAttribute("isGroupProduct"));
                cartItemObj.put("name", cartLine.getName());
                cartItemObj.put("description", cartLine.getItemTypeDescription());
//                cartItemObj.put("createDate", )
                cartItemObj.put("displayPrice", cartLine.getDisplayPrice());
                String isVariant = cartLine.getProduct().getString("isVariant");
                if (UtilValidate.isNotEmpty(isVariant) && isVariant.equalsIgnoreCase("Y")) {
                    cartItemObj.put("features", cartLine.getFeatureSet());
                }
                GenericValue miniProduct = cartLine.getProduct();
                ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(miniProduct, request);
                String mediumImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
                
                cartItemObj.put("mediumImageUrl", baseUrl + mediumImageUrl);
                //调整
                cartItemObj.put("otherAdjustments", cartLine.getAdjustments());
                cartItemObj.put("isChoose", cartLine.getIsChoose());
                //明细合计
                cartItemObj.put("itemSubTotal", cartLine.getDisplayItemSubTotal());
                String productId = cartLine.getProductId();
                List<GenericValue> serviceAssoc = null;
                try {
                    serviceAssoc = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(serviceAssoc)) {
                    List<String> serviceTags = FastList.newInstance();
                    for (int j = 0; j < serviceAssoc.size(); j++) {
                        GenericValue assoc = serviceAssoc.get(j);
                        serviceTags.add(assoc.getString("enumId"));
                    }
                    cartItemObj.put("serviceTag", serviceTags);
                }
                cartItemObj.put("productTypeId", miniProduct.get("productTypeId"));
                cartItemObj.put("isBondedGoods", miniProduct.get("isBondedGoods"));
                cartItemObj.put("integralDeductionType", miniProduct.get("integralDeductionType"));//1.不可使用积分2:百分比抵扣3:固定金额抵扣
                cartItemObj.put("integralDeductionUpper", miniProduct.get("integralDeductionUpper")); //积分抵扣数
                cartItemObj.put("voucherAmount", miniProduct.get("voucherAmount"));//代金劵面值
                cartItemObj.put("isSku", miniProduct.get("isVirtual"));
                
                /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
                Long integralValue = integralPerMoney.getLong("integralValue");
                String integralDeductionType = miniProduct.getString("integralDeductionType");
                BigDecimal integralDeductionUpper = miniProduct.getBigDecimal("integralDeductionUpper");
                if (integralDeductionType.equalsIgnoreCase("2")) {
                    BigDecimal scorePrice = cartLine.getBasePrice().multiply(integralDeductionUpper).divide(new BigDecimal(100));
                    BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                    cartItemObj.put("scorePrice", scorePrice); //积分抵扣的金额
                    cartItemObj.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
                } else if (integralDeductionType.equalsIgnoreCase("3")) {
                    cartItemObj.put("scorePrice", integralDeductionUpper);//积分抵扣的金额
                    cartItemObj.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
                }
                
                //variant product
                cartItemObj.put("productId", productId);
                if (UtilValidate.isNotEmpty(productId)) {
                    String parentProductId = cartLine.getParentProductId();
                    cartItemObj.put("parentProductId", parentProductId);
                }
                //variant product feature
                ProductConfigWrapper configWrapper = cartLine.getConfigWrapper();
                if (UtilValidate.isNotEmpty(configWrapper)) {
                    List<ProductConfigWrapper.ConfigOption> features = cartLine.getConfigWrapper().getSelectedOptions();
                    cartItemObj.put("features", features);
                }
                
                if (productStoreCartObj.get("shoppingCartItems") != null) {
                    List cartItems = (List) productStoreCartObj.get("shoppingCartItems");
                    if (UtilValidate.isNotEmpty(cartItems)) {
                        cartItems.add(cartItemObj);
                    } else {
                        cartItems = FastList.newInstance();
                        cartItems.add(cartItemObj);
                        productStoreCartObj.put("shoppingCartItems", cartItems);
                    }
                } else {
                    List cartItems = FastList.newInstance();
                    cartItems.add(cartItemObj);
                    productStoreCartObj.put("shoppingCartItems", cartItems);
                }
                productStoreCartObj.put("productStoreId", cartLine.getProductStoreId());
                productStoreCartObj.put("productStoreName", cartLine.getProductStoreName());
                productStoreCartObj.put("innerStoreId", innerStoreId);
            }
            
            if (UtilValidate.isNotEmpty(productStoreCartMap)) {
                Set<String> proSet = productStoreCartMap.keySet();
                for (String proStoreId : proSet) {
                    List<GenericValue> storeOrderAdjustments = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(orderAdjustments)) {
                        for (int i = 0; i < orderAdjustments.size(); i++) {
                            GenericValue orderAdjustment = orderAdjustments.get(i);
                            if (orderAdjustment.get("productStoreId").equals(proStoreId)) {
                                storeOrderAdjustments.add(orderAdjustment);
                            }
                        }
                    }
                    productStoreCartMap.get(proStoreId).put("storeOrderAdjustments", storeOrderAdjustments);
                }
                
            }
        }
    
        //订单调整价
        cartInfo.put("orderPromoTotal", totalCouponAmount);
        cartInfo.put("orderShippingTotal", shoppingCart.getTotalShipping());
        //cart 订单项的价格
        BigDecimal displaySubTotal = shoppingCart.getDisplaySubTotal();
        //购物车小计
        cartInfo.put("displaySubTotal", displaySubTotal);
        //cart 运费+订单调整+subtotal
        BigDecimal displayGrandTotal = shoppingCart.getDisplayGrandTotal1();
        cartInfo.put("displayGrandTotal", displayGrandTotal);
        //总的商品金额不包括促销
        cartInfo.put("orderSubTotal", OrderReadHelper.getOrderItemsSubTotal(orderItems, null));
    
        cartInfo.put("orderShippingTotal", shoppingCart.getTotalShipping());
        cartInfo.put("totalQuantity", toalQuantity);
        Map<String, Object> resultData = FastMap.newInstance();
//        resultData.put("storeCarts", productStoreCartMap.values());
        resultData.put("shoppingCart", cartInfo);
        resultData.put("retCode", 1);
        resultData.put("message", "查询购物车小计成功");
        request.setAttribute(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    public static String getShoppingCartCount(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart shoppingCart = ShoppingCartEvents.getCartObject(request);
        int size = shoppingCart.size();
        
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("cartCount", size);
        request.setAttribute(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    public static String getCartShipments(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        Map<String, Object> resultData = FastMap.newInstance();
        List cartShipments = FastList.newInstance();
        if (UtilValidate.isNotEmpty(cart)) {
            ShippingEstimateWrapper shippingEstWpr = new ShippingEstimateWrapper(dispatcher, cart, 0);
            List<GenericValue> carrierShipmentMethodList = shippingEstWpr.getShippingMethods();
            if (UtilValidate.isNotEmpty(carrierShipmentMethodList)) {
                for (int i = 0; i < carrierShipmentMethodList.size(); i++) {
                    GenericValue carrierShipmentMethod = carrierShipmentMethodList.get(i);
                    Map<String, Object> cartShipment = FastMap.newInstance();
                    cartShipment.put("estimate", shippingEstWpr.getShippingEstimate(carrierShipmentMethod));
                    cartShipment.put("shipmentMethodTypeId", carrierShipmentMethod.get("shipmentMethodTypeId"));
                    cartShipment.put("partyId", carrierShipmentMethod.get("partyId"));
                    cartShipment.put("description", carrierShipmentMethod.get("description"));
                    
                    if (carrierShipmentMethod.get("partyId").equals("_NA_")) {
                        cartShipment.put("estimate", "0");
                        cartShipment.put("shipmentMethodTypeId", carrierShipmentMethod.get("shipmentMethodTypeId"));
                        cartShipment.put("description", carrierShipmentMethod.get("description"));
                    }
                    
                    cartShipments.add(cartShipment);
                }
            }
            
            resultData.put("carrierShipmentMethodList", cartShipments);
            request.setAttribute(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        } else {
            request.setAttribute(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
        }
        
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    
}
