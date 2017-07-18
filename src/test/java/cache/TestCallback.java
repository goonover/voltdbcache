package cache;

import cache.annotations.VoltCallback;
import cache.aop.User;
import org.junit.Test;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swqsh on 2017/7/7.
 */
@VoltCallback(callbackName = "TestCallback",shareable = false)
public class TestCallback implements ProcedureCallback {
    @Override
    public void clientCallback(ClientResponse clientResponse) throws Exception {
        System.out.println("enter TestCallback");
        VoltTable table = clientResponse.getResults()[0];
        table.advanceToRow(2);
        table.advanceRow();

    }

    @Test
    public void typeTrans(){
        VoltType type = VoltType.typeFromClass(int.class);
        System.out.println(type.toString());
        type = VoltType.typeFromClass(User.class);
        System.out.println(type.toString());
    }

    private void testCast(String[] values){
        for(String per:values){
            System.out.println(per);
        }
    }

    private void print(int i){
        System.out.println("received param:"+i);
    }

    @Test
    public void testCas(){
        int i =1;
        Long l = new Long(2);
        Integer i1 = new Integer(2);
        Byte b =new Byte("1");
        print(Integer.class.cast(i));
        print(i1);
    }

    private <T> T castObject(Class<T> clazz,Object obj){
        return (T) obj;
    }
}
