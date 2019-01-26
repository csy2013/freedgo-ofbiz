
def module = "TicketReport.groovy";


import org.ofbiz.base.util.*;




//productID
productId="";
if (parameters.ids){
    ids = parameters.ids;
}

birtParameters = [:];
try {
    println "zjhtest:======================================================================================"+productId;
    if (parameters.ids){
        birtParameters.ids = ids;
    }
    request.setAttribute("birtParameters", birtParameters);
} catch (e) {
    Debug.logError(e, module);
}
return "success";
