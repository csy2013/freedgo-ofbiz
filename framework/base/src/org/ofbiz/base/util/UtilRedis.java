package org.ofbiz.base.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

/**
 * Created by zhajh on 2018/7/3.
 */
public class UtilRedis {

    public static final String module = UtilMisc.class.getName();
    private static JedisPool jedisPool = null;

    /**
     * 初始化Redis连接池
     */
    static {
        try {
            // 加载redis配置文件
            String strMaxActive = UtilProperties.getMessage("redis", "redis.pool.maxActive", Locale.getDefault());
            String strMaxIdle = UtilProperties.getMessage("redis", "redis.pool.maxIdle",Locale.getDefault());
            String strMaxWait = UtilProperties.getMessage("redis", "redis.pool.maxWait",Locale.getDefault());
            String strTestOnBorrow = UtilProperties.getMessage("redis", "redis.pool.testOnBorrow",Locale.getDefault());
            String strOnreturn = UtilProperties.getMessage("redis", "redis.pool.testOnReturn",Locale.getDefault());

            String strIp=UtilProperties.getMessage("redis", "redis.ip",Locale.getDefault());
            String strPort=UtilProperties.getMessage("redis", "redis.port",Locale.getDefault());

            int maxActivity = Integer.valueOf(strMaxActive);
            int maxIdle = Integer.valueOf(strMaxIdle);
            long maxWait = Long.valueOf(strMaxWait);
            boolean testOnBorrow = Boolean.valueOf(strTestOnBorrow);
            boolean onreturn = Boolean.valueOf(strOnreturn);
            // 创建jedis池配置实例
            JedisPoolConfig config = new JedisPoolConfig();
            // 设置池配置项值
            config.setMaxTotal(maxActivity);
            config.setMaxIdle(maxIdle);  //最大空闲连接数
            config.setMaxWaitMillis(maxWait);
            config.setTestOnBorrow(testOnBorrow);
            config.setTestOnReturn(onreturn);
            jedisPool = new JedisPool(config, strIp,
                    Integer.valueOf(strPort), 10000,
                    null);
        } catch (Exception e) {
            Debug.logError(e, "初始化Redis连接池 出错.", module);
        }
    }


    /**
     * 获取Jedis实例
     *
     * @return
     */
    public synchronized  static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            Debug.logError(e, "Redis缓存获取Jedis实例 出错！", module);
            return null;
        }
    }

    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
//            jedisPool.close();
        }
    }

    /**
     * 向缓存中设置字符串内容
     *
     * @param key
     *            key
     * @param value
     *            value
     * @return
     * @throws Exception
     */
    public static boolean set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if(jedis != null){
                jedis.set(key, value);
            }
            return true;
        } catch (Exception e) {
            Debug.logError(e, "Redis缓存设置key值 出错！", module);
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public static boolean exists(String key){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis == null) {
                return false;
            } else {
                return jedis.exists(key);
            }
        } catch (Exception e) {
            Debug.logError(e, "Redis缓存判断key是否存在 出错！", module);
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 删除缓存中的对象，根据key
     * @param key
     * @return
     */
    public static boolean del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }


    //*******************key-value****************start

    /**
     * 向缓存中设置对象
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(String key, Object value) {
        Jedis jedis = null;
        try {
//            String objectJson = JSONObject.fromObject(value).toString();
            jedis = getJedis();
            if (jedis != null) {
                jedis.set(key.getBytes(),SerializeUtil.serialize(value));
//                jedis.set(key.getBytes(),JsonRedisSeriaziler.seriazileAsString(value).getBytes());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * NX是不存在时才set， XX是存在时才set， EX是秒，PX是毫秒
     * @param key
     * @param value
     * @param expireSecond
     * @return
     */
    public static boolean set(String key, Object value,long expireSecond) {
        Jedis jedis = null;
        try {
//            String objectJson = JSONObject.fromObject(value).toString();
            jedis = getJedis();
            if (jedis != null) {
                jedis.set(key.getBytes(), SerializeUtil.serialize(value),"NX".getBytes(), "PX".getBytes(), expireSecond);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    /**
     * 根据key 获取内容
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            byte[] data = jedis.get(key.getBytes());
//            Object resValue = JsonRedisSeriaziler.deserializeAsObject(new String(data),Map.class);
            Object resValue = SerializeUtil.unserialize(data);
            return resValue;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    //*******************key-value****************end

    //*************** 操作list****************start
    /**
     * 向缓存中设置对象
     * @param key
     * @param list
     * T string calss
     * @return
     */
    public static <T> boolean setList(String key,List<T> list){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                for (T vz : list) {
                    if (vz instanceof String) {
                        jedis.lpush(key, (String) vz);
                    } else {
                        String objectJson = JSONObject.fromObject(vz).toString();
                        jedis.lpush(key, objectJson);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> List<T> getListEntity(String key,Class<T> entityClass){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                List<String> valueJson = jedis.lrange(key, 0, -1);
                JSONArray json = new JSONArray();
                json.addAll(valueJson);
                JSONArray jsonArray = JSONArray.fromObject(json.toString());
                return (List<T>) JSONArray.toCollection(jsonArray, entityClass);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static List<String> getListString(String key){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                return jedis.lrange(key, 0, -1);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }
    //*************** 操作list****************end

    //*************** 操作map****************start
    public static <K,V> boolean setMap(String key,Map<String,V> map){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                Set<Map.Entry<String, V>> entry = map.entrySet();
                for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext();) {
                    Map.Entry<String, V> kv = ite.next();
                    if (kv.getValue() instanceof String) {
                        jedis.hset(key, kv.getKey(), (String) kv.getValue());
                    }else if (kv.getValue() instanceof List) {
                        jedis.hset(key, kv.getKey(), JSONArray.fromObject(kv.getValue()).toString());
                    } else {
                        jedis.hset(key, kv.getKey(), JSONObject.fromObject(kv.getValue()).toString());
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    public static boolean setMapKey(String key,String mapKey,Object value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                if (value instanceof String) {
                    jedis.hset(key, mapKey, String.valueOf(value));
                } else if (value instanceof List) {
                    jedis.hset(key, mapKey, JSONArray.fromObject(value).toString());
                } else {
                    jedis.hset(key, mapKey, JSONObject.fromObject(value).toString());
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }


    @SuppressWarnings("unchecked")
    public static <K,V> Map<String,V> getMap(String key){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
                return map;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }


    @SuppressWarnings("unchecked")
    public static <K,V> Map<String,List<V>> getMapList(String key,Class<V> clazz){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
                Set<Map.Entry<String, V>> entry = map.entrySet();
                for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext();) {
                    Map.Entry<String, V> kv = ite.next();
                    JSONArray jsonArray = JSONArray.fromObject(kv.getValue());
                    map.put(kv.getKey(), (V) JSONArray.toCollection(jsonArray, clazz));
                }
                return (Map<String, List<V>>) map;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getMapKeyListEntity(String key,String mapKey,
                                                  Class<T> entityClass){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                String valueJson = jedis.hget(key, mapKey);
                JSONArray jsonArray = JSONArray.fromObject(valueJson);
                return (List<T>) JSONArray.toCollection(jsonArray, entityClass);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMapKeyEntity(String key,String mapKey,
                                        Class<T> entityClass){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if(jedis != null){
                String valueJson=jedis.hget(key, mapKey);
                return (T) JSONObject.toBean(JSONObject.fromObject(valueJson), entityClass);
            }else{return null;}
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static Object getMapKey(String key,String mapKey){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if(jedis != null){
                return jedis.hget(key, mapKey);
            }else{return null;}
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    public static boolean delMapKey(String key,String mapKey){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.hdel(key, mapKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

}
