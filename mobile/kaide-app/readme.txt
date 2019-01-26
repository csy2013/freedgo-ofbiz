curl -i -H "Content-Type: application/json" -X POST -d '{"username":"yabiz","password":"changsy"}' http://localhost:8080/mobile-token/auth


curl -i -H "Content-Type: application/json" -X POST -d '{"loginId":"yabiz"}'  -H "X-Auth-Token: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ5YWJpeiIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTQ3NzEwMDQwNjU3NCwiZXhwIjoxNDc3NzA1MjA2fQ.97JIV5Efc99iEtA01Pj-jV6kqIOQHuyk3vcFTxoZ9W9C8AIwfUpJhDnBccrJCj9Qz6fSTT5y29BEFMofbtbGww" -X POST http://localhost:8080/mobile-token/api/party.personBaseQuery


1、需要token/build/lib/mobile-token.jar copy 到 webapp/WEB-INF/lib下
2、CatalinaContainer.java文件中:
// create the Default Servlet instance to mount
        if(appInfo.name.equals("images")) {
            StandardWrapper defaultServlet = new StandardWrapper();
            defaultServlet.setParent(context);
            defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
            defaultServlet.setServletName("default");
            defaultServlet.setLoadOnStartup(1);
            defaultServlet.addInitParameter("debug", "0");
            defaultServlet.addInitParameter("listing", "true");
            defaultServlet.addMapping("/");
            context.addChild(defaultServlet);
            context.addServletMapping("/", "default");
        }
 3、所有的appjson 返回的形式是: "resultData"

4、存在classloader的问题，token 访问不了上级的java classpath，


 antlr-2.7.7.jar
 aspectjrt-1.8.5.jar
 aspectjweaver-1.8.5.jar

 dom4j-1.6.1.jar
 h2-1.4.188.jar
 hibernate-commons-annotations-4.0.5.Final.jar
 hibernate-core-4.3.8.Final.jar
 hibernate-entitymanager-4.3.8.Final.jar
 hibernate-jpa-2.1-api-1.0.0.Final.jar

 jandex-1.1.0.Final.jar
 javassist-3.18.1-GA.jar
 javax.transaction-api-1.2.jar

 jboss-logging-annotations-1.2.0.Beta1.jar

 snakeyaml-1.14.jar
 spring-aspects-4.1.6.RELEASE.jar


 spring-boot-starter-aop-1.2.3.RELEASE.jar
 spring-boot-starter-data-jpa-1.2.3.RELEASE.jar
 spring-boot-starter-jdbc-1.2.3.RELEASE.jar


 spring-data-commons-1.9.2.RELEASE.jar
 spring-data-jpa-1.7.2.RELEASE.jar
 spring-jdbc-4.1.6.RELEASE.jar
 spring-mobile-device-1.1.3.RELEASE.jar
 spring-orm-4.1.6.RELEASE.jar
 spring-tx-4.1.6.RELEASE.jar
 tomcat-jdbc-8.0.20.jar
 tomcat-juli-8.0.20.jar
 xml-apis-1.0.b2.jar