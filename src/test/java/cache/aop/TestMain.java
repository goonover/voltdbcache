package cache.aop;

import cache.VoltCacheClient;
import cache.VoltCacheManager;
import cache.annotations.VoltCache;
import cache.annotations.VoltRemove;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by swqsh on 2017/7/12.
 */
public class TestMain {

    @Before
    public void register(){
        VoltCacheClient client = new VoltCacheClient("10.201.0.113");
        VoltCacheManager.clientRegister("113",client);
        VoltCacheManager.setDefaultClient(client);
    }

    @VoltCache(procedureName = "RISKCONTROL.select",clientName = "113",callbackName = "query",condition = "a+b>5")
    public String printName(String s,int a,int b){
        System.out.println(s);
        return s;
    }

    @VoltCache(procedureName = "RISKCONTROL.select",clientName = "113",callbackName = "query",
            condition = "user.name=='eric'&&user.cash>30",params = "tobeQuery")
    public void  printUserName(String tobeQuery,User user){
        System.out.println(tobeQuery);
    }

    @VoltRemove(tableName = "riskcontrol",value = "name",condition = "user.name=='eric'")
    public void removePk(String name,User user){
        System.out.println("ready to remove:"+name);
    }

    @VoltRemove(tableName = "riskcontrol",key = "notpk",value = "trade_volume=418709247")
    public void removeCommon(String name){
        System.out.println("common remove");
    }

    @Test
    public void testRemove(){
        User user = new User("eric",50);
        removePk("phmarptl",user);
        removeCommon("hello");
    }

    @Test
    public void call(){
        printName("mvfnxmup",3,3);
        printName("mvfnxmup",2,2);
    }

    @Test
    public void testCondition(){
        User user = new User("eric",50);
        printUserName("mvfnxmup",user);
    }

    @After
    public void after(){
        VoltCacheManager.releaseAllClient();
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientFactory.createClient();
        client.createConnection("10.201.0.113");
        VoltCacheClient client1=new VoltCacheClient("10.201.0.113");
        Object[] params = new Object[1];
        params[0] = "mvfnxmup";
        client.callProcedure(new QueryCallback(),"RISKCONTROL.select",params);
        TimeUnit.SECONDS.sleep(3);
        /*VoltTable[] res = response.getResults();
        for(VoltTable table:res){
            System.out.println(table.toJSONString());
        }*/
    }
}
