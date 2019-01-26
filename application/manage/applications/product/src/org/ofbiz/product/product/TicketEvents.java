/*******************************************************************************
 * Add By AlexYao
 *******************************************************************************/
package org.ofbiz.product.product;

import org.apache.commons.lang.RandomStringUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Order Information Related Events
 */
public class TicketEvents {
    
    public static final String module = TicketEvents.class.getName();
    public static final String resource = "AccountingUiLabels";
    public static final String resourceErr = "AccountingErrorUiLabels";
    protected final static char[] smartChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    
    /**
     * 生成yabiz商城券号
     *
     * @param delegator
     * @return String specifying the exit status of this event
     */
    public static String getTicketNo(Delegator delegator) {
        int codeLength = 12;
        
        String newTicketNo = "";
        boolean foundUniqueNewCode = false;
        long count = 0;
        
        while (!foundUniqueNewCode) {
            newTicketNo = RandomStringUtils.random(codeLength, smartChars);
            List<GenericValue> existingTicket = null;
            try {
                existingTicket = delegator.findByAndCache("Ticket", UtilMisc.toMap("ticketNo", newTicketNo));
            } catch (GenericEntityException e) {
                Debug.logWarning("Could not find Ticket for just generated ID: " + existingTicket, module);
            }
            if (UtilValidate.isEmpty(existingTicket)) {
                foundUniqueNewCode = true;
            }
            
            count++;
            if (count > 999999) {
                return "Unable to locate unique TicketNo! Length [" + codeLength + "]";
            }
        }
        return newTicketNo;
    }
    
    /**
     * 支付成功生成券  Add By AlexYao
     *
     * @param dispatcher
     * @param delegator
     * @param productId  商品Id
     * @param activityId 活动Id
     * @param orderId    订单Id
     * @return
     */
    public static synchronized String createTickets(LocalDispatcher dispatcher, Delegator delegator, String productId, String activityId, String orderId, GenericValue orderItem) {
        List<GenericValue> tickets = null;
        try {
            tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(tickets)) {
            
            List<GenericValue> orderRoles = null;
            try {
                orderRoles = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(orderItem) && UtilValidate.isNotEmpty(orderRoles)) {
                
                List<GenericValue> toBeStored = new ArrayList<GenericValue>();
                int quantity = orderItem.getBigDecimal("quantity").intValue();
                int i = 0;
                while (i++ < quantity) {
                    String ticketId = delegator.getNextSeqId("Ticket");
                    GenericValue ticket = delegator.makeValue("Ticket");
                    String ticketNo = getTicketNo(delegator);
                    ticket.set("ticketId", ticketId);
                    ticket.set("ticketNo", ticketNo);
                    ticket.set("productId", productId);
                    ticket.set("activityId", activityId);
                    ticket.set("orderId", orderId);
                    ticket.set("partyId", orderRoles.get(0).get("partyId"));
                    ticket.set("ticketStatus", "notUsed");
                    ticket.set("ticketIndex", Long.valueOf(i));
                    ticket.set("amount",orderItem.get("unitPrice"));
                    ticket.set("ticketName",orderItem.get("itemDescription"));
                    ticket.set("contentId", null);
                    toBeStored.add(ticket);
                    
                }
                // store the changes
                if (toBeStored.size() > 0) {
                    try {
                        delegator.storeAll(toBeStored);
                    } catch (GenericEntityException e) {
                        return e.getMessage();
                    }
                }
                
            }
        }
        return "success";
    }
    
}