import org.ofbiz.base.util.UtilMisc
import org.ofbiz.entity.GenericEntityException
import org.ofbiz.entity.GenericValue

/**
 * Created by dongxiao on 2016/1/4.
 */
List<GenericValue> productAndPriceViewList = null;
try {
    productAndPriceViewList = delegator.findByAnd("ProductAndPriceView",UtilMisc.toMap("productPriceTypeId","DEFAULT_PRICE"));
} catch (GenericEntityException e) {
    e.printStackTrace();
}
shoppingCart = session.getAttribute("shoppingCart");
currenPartyId = session.getAttribute("currenPartyId");
def productSize = 0;

if (shoppingCart){
    productSize = shoppingCart.items().size();
}
context.productAndPriceViewList = productAndPriceViewList;
context.shoppingCart = shoppingCart;
context.currenPartyId = currenPartyId;
context.productSize = (productSize == 0)? "" : productSize.toString();