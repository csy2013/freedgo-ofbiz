messageTemplateId = parameters.get("messageTemplateId") ;
messageTemplate = delegator.findOne("MessageTemplate",["messageTemplateId":messageTemplateId],false);
context.put("messageTemplateDTO",messageTemplate) ;