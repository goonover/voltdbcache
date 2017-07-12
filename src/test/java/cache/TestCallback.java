package cache;

import cache.annotations.VoltCallback;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

/**
 * Created by swqsh on 2017/7/7.
 */
@VoltCallback(callbackName = "TestCallback",shareable = false)
public class TestCallback implements ProcedureCallback {
    @Override
    public void clientCallback(ClientResponse clientResponse) throws Exception {
        System.out.println("enter TestCallback");
    }
}
