package cache;

import cache.annotations.VoltCache;
import cache.annotations.VoltCallback;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

/**
 * Created by swqsh on 2017/7/4.
 */
public class Main {
    @VoltCache(procedureName = "withCallback")
    public String testWithCallback(String str, ProcedureCallback callback){
        try {
            callback.clientCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "withCallback:"+str;
    }

    @VoltCache(procedureName = "withoutCallback")
    public int testWithoutCallback(int param1,int param2){
        return param1+param2;
    }

    public static void main(String[] args){
        Main test=new Main();
        ProcedureCallback callback=new ProcedureCallback() {
            @Override
            public void clientCallback(ClientResponse clientResponse) throws Exception {
                System.out.println("######  callback invoke  #######");
            }
        };
        test.testWithCallback("are you ok",callback);

        System.out.println("\n\n");
        test.testWithoutCallback(1,2);;
    }

}

