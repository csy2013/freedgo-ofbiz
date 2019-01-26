java  -Xms1024M -Xmx1384M -Xdebug -Xnoagent -Djava.compiler=NONE  -Ddruid.log.stmt.executableSql=true -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5065 -jar ./framework/ofbiz.jar
