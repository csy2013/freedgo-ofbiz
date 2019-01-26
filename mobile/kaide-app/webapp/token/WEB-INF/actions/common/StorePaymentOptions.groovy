import org.ofbiz.product.store.ProductStoreWorker

productStoreId = ProductStoreWorker.getProductStoreId(request);


serviceIn = [productStoreId: productStoreId];

result = dispatcher.runSync("getStorePaymentOptions", serviceIn);
request.setAttribute("resultData", result);

return 'success'