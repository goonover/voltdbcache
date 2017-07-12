package cache;

import org.apache.log4j.Logger;
import org.voltdb.client.ProcedureCallback;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维护voltdb服务器的地址以及连接，所有的缓存请求都经过该类去调用voltdb的存储过程
 * Created by swqsh on 2017/7/4.
 */
public class VoltCacheManager {

    private static ConcurrentHashMap<String,VoltCacheClient> clientHolder=new ConcurrentHashMap<>();

    private static VoltCacheClient defaultClient;

    private static VoltCallbackManager callbackManager = new VoltCallbackManager();
    private static Logger logger = Logger.getLogger(VoltCallbackManager.class);

    public synchronized static void setDefaultClient(VoltCacheClient client){
        if(defaultClient==null) {
            defaultClient = client;
            clientHolder.putIfAbsent("default",client);
        }
        else{
            logger.info("default client exists, if you want to set another client as the default," +
                    "please remove it at first");
        }
    }

    /**
     * 默认的客户端连接不能通过clientRegister直接注册，而是必须通过{@link VoltCacheManager#setDefaultClient(VoltCacheClient)}
     * 来完成对默认连接的设置，当已经存在默认的连接时，设置将会失败
     * @param clientName
     * @param client
     */
    public static void  clientRegister(String clientName,VoltCacheClient client){
        if(clientName.equals("default")) {
            setDefaultClient(client);
            return;
        }
        VoltCacheClient returnClient = clientHolder.putIfAbsent(clientName,client);
        if(returnClient!=null){
            logger.error("client register failed,client:"+clientName+" exists");
        }
    }

    public static void clientUnRegister(String clientName){
        VoltCacheClient client = clientHolder.remove(clientName);
        if(client==null)
            return;
        if(clientName.equals("default")){
            synchronized (defaultClient){
                defaultClient = null;
            }
        }
        client.close();
    }

    /**
     * 不指定具体的client，从而使用默认的VoltCacheClient
     * @param procedureName 存储过程名
     * @param params        调用voltdb存储过程的参数
     * @param callbackName 在{@link VoltCacheManager#callbackManager}中，callback的key值
     */
    public static void cache(String procedureName, Object[] params, String callbackName){
        ProcedureCallback callback=callbackManager.getCallback(callbackName);
        cache(procedureName,params,callback);
    }

    public static void cache(String procedureName, Object[] params, ProcedureCallback callback){
        if(defaultClient!=null){
            defaultClient.cache(procedureName,params,callback);
        }else{
            logger.error("no default voltdb client, give up call procedure :"+procedureName);
        }
    }

    public static void cache(String clientName, String procedureName, Object[] params, String callbackName){
        if(clientName.equals("default")){
            cache(procedureName,params,callbackName);
            return;
        }
        ProcedureCallback callback = callbackManager.getCallback(callbackName);
        cache(clientName,procedureName,params,callback);
    }

    public static void cache(String clientName,String procedureName,Object[] params,ProcedureCallback callback){
        if(clientName.equals("default")){
            cache(procedureName,params,callback);
            return;
        }
        if(!clientHolder.containsKey(clientName)) {
            logger.error("cannot find client by clientName:"+clientName+", give up call procedure:"+procedureName);
            return;
        }
        VoltCacheClient client=clientHolder.get(clientName);
        client.cache(procedureName,params,callback);
    }

    public static VoltCallbackManager getCallbackManager(){
        return callbackManager;
    }

    public static void setCallbackManager(VoltCallbackManager manager){
        callbackManager=manager;
    }

    public static synchronized void releaseAllClient(){
        Set<String> names = clientHolder.keySet();
        for(String name: names){
            VoltCacheClient client = clientHolder.get(name);
            client.close();
            clientHolder.remove(name,client);
        }
        defaultClient.close();
        defaultClient = null;
    }

}
