

JAVA_OPTS="-server -showversion -Xms12g -Xmx12g -XX:PermSize=800m -XX:MaxPermSize=512m  -Xms2048M -Xmx2048M "
JAVA_OPTS="$JAVA_OPTS -d64 -XX:CICompilerCount=8 -XX:+UseCompressedOops"
JAVA_OPTS="$JAVA_OPTS -XX:SurvivorRatio=4 -XX:TargetSurvivorRatio=90"
JAVA_OPTS="$JAVA_OPTS -XX:ReservedCodeCacheSize=512m -XX:-UseAdaptiveSizePolicy"
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Shanghai -XX:-DontCompileHugeMethods"
JAVA_OPTS="$JAVA_OPTS -Xss256k -XX:+AggressiveOpts -XX:+UseBiasedLocking"
JAVA_OPTS="$JAVA_OPTS -XX:MaxTenuringThreshold=31 -XX:+CMSParallelRemarkEnabled "
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=512m -XX:+UseFastAccessorMethods"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSInitiatingOccupancyOnly -Djava.awt.headless=true"
JAVA_OPTS="$JAVA_OPTS -XX:+UseGCOverheadLimit -XX:AllocatePrefetchDistance=256 -XX:AllocatePrefetchStyle=1"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:MaxGCPauseMillis=200"

//替换生产环境：
sed -i "s/\/Users\/tusm\/javaProject\/svn\/yabiz/\/home\/yabiz/g" `grep \/Users\/tusm\/javaProject\/svn\/yabiz -rl /home/yabiz/daojia_muti`

//替换生产数据库配置
cp /home/daojia/framework/entity/config/entityengine.xml /home/yabiz/daojia_muti/daojia/daojia/docker/base/config/


//生产机compenent 增加：/home/yabiz/daojia_muti/daojia/daojia/docker/base/config/component-load.xml
    <load-components parent-directory="/home/mall/yabiz1.0/mobile"/>
    <load-components parent-directory="/home/mall/yabiz1.0/specialpurpose"/>
    <load-components parent-directory="/home/daojia/hot-deploy"/>
    <load-components parent-directory="/home/mall/yabiz1.0/ecommerce"/>



./ant load-file -Ddata-file=../framework/service/data/ServiceSecurityData.xml -Ddelegator=default

./ant load-file -Ddata-file=./manage/services/toolsService/data/SystemMgrSecurityData.xml

./ant load-file -Ddata-file=../framework/workflow/data/WorkFlowSecurityData.xml

./ant load-file -Ddata-file=./manage/services/productService/data/ProdPromoSecurityData.xml

约定当前系统：
1、只有一个店铺 productStoreId 唯一
2、只有一个目录 catalogId 唯一



// 对元素 进行事件注册
$("#e11")
.on("change", function(e) { log("change "+JSON.stringify({val:e.val, added:e.added, removed:e.removed})); })  // 改变事件
.on("select2-opening", function() { log("opening"); })  // select2 打开中事件
.on("select2-open", function() { log("open"); })     // select2 打开事件
.on("select2-close", function() { log("close"); })  // select2 关闭事件
.on("select2-highlight", function(e) { log ("highlighted val="+ e.val+" choice="+ JSON.stringify(e.choice));})  // 高亮
.on("select2-selecting", function(e) { log ("selecting val="+ e.val+" choice="+ JSON.stringify(e.choice));})  // 选中事件
.on("select2-removing", function(e) { log ("removing val="+ e.val+" choice="+ JSON.stringify(e.choice));})  // 移除中事件
.on("select2-removed", function(e) { log ("removed val="+ e.val+" choice="+ JSON.stringify(e.choice));})  // 移除完毕事件
.on("select2-loaded", function(e) { log ("loaded (data property omitted for brevity)");})  // 加载中事件
.on("select2-focus", function(e) { log ("focus");})    //  获得焦点事件
.on("select2-blur", function(e) { log ("blur");});     //  失去焦点事件
$("#e11").click(function() { $("#e11").val(["AK","CO"]).trigger("change"); });



<request-map uri="uploadImage.html"> 生成
contentId，datasourceId


application/manage/services/accountService/src
application/manage/services/commonService/src
application/manage/services/contentService/src
application/manage/services/humanresService/src
application/manage/services/manufacturingService/src
application/manage/services/marketService/src
application/manage/services/orderService/src
application/manage/services/partyService/src
application/manage/services/productService/src
application/manage/services/securityext/src
application/manage/services/toolsService/src
application/manage/services/workeffortService/src