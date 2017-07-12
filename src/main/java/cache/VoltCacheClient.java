package cache;

import org.apache.log4j.Logger;
import org.voltdb.client.Client;
import org.voltdb.client.ClientConfig;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ProcedureCallback;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 维护voltdb服务器的地址以及连接，所有的缓存请求都经过该类去调用voltdb的存储过程
 * 可以指定一个{@link VoltCacheClient}维护多个{@link Client}，一般一个Client足以支撑20-30
 * 万的并发量，根据业务需要决定Client个数。如果连接有问题，可能启动过程会很慢，而且有异常出现，
 * 需要使用者自己确保每个连接是有效的
 * Created by swqsh on 2017/7/4.
 */
 public class VoltCacheClient {

    //voltdb客户端个数,默认为一个
    private int size;

    //voltdb服务器客户端，目前一个连接。
    private Client[] clients;

    //用于随机决定使用哪个客户端，只有size>1时，random才被使用
    private Random random = new Random();
    private Logger logger = Logger.getLogger(VoltCacheClient.class);

    /**
     * 服务器的ip地址以逗号分隔
     * @param servers
     */
    public VoltCacheClient(String servers){
        this(servers,"","");
    }

    public VoltCacheClient(String servers,String userName,String passWord){
        this.size=1;
        clients=new Client[1];
        clients[0]= getClient(servers,userName,passWord);
    }

    public VoltCacheClient(int size,String servers){
        this(size,servers,"","");
    }

    public VoltCacheClient(int size,String servers,String userName,String passWord){
        this.size = size;
        clients = new Client[size];
        for(int i=0;i<size;i++){
            clients[i] = getClient(servers,userName,passWord);
        }
    }

    private Client getClient(String servers,String userName,String passWord){
        ClientConfig clientConfig=new ClientConfig(userName,passWord);
        clientConfig.setTopologyChangeAware(true);
        String[] serverList=servers.split(",");
        Client client= ClientFactory.createClient(clientConfig);
        CountDownLatch connections=new CountDownLatch(serverList.length);
        for(String server:serverList){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.createConnection(server);
                        connections.countDown();
                    } catch (IOException e) {
                        logger.error("can't connect to server:"+server);
                        logger.info(e);
                    }
                }
            }).start();
        }
        try {
            connections.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * 所有的缓存都是经过client的cache来实现调用的，事实上，参数应该为Object[]格式，但是由于
     * Object[]属于Object，所以参数直接用Object类型
     * @param procedureName
     * @param params
     * @param callback
     */
    public void cache(String procedureName, Object[] params, ProcedureCallback callback){
        Client client;
        if(size==1)
            client = clients[0];
        else
            client = clients[random.nextInt(size)];
        try {
            if (params == null||params.length==0) {
                client.callProcedure(callback, procedureName);
            } else {
                client.callProcedure(callback, procedureName, params);
            }
        }catch (IOException e){
            logger.info(e);
        }
    }

    /**
     * 该方法本身非线程安全，但由于该方法应只有{@link VoltCacheManager#releaseAllClient()}调用，
     * 由上层调用保证其并发安全
     */
    public void close(){
        if(clients != null)
        for(Client client:clients){
            try {
                client.drain();
                client.close();
            }catch (Exception e){
                logger.info(e);
            }
        }
        clients = null;
    }
}
