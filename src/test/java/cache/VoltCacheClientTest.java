package cache;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/10.
 */
public class VoltCacheClientTest {
    @Test
    public void cache() throws Exception {
        VoltCacheClient client=new VoltCacheClient(3,"10.201.0.113");
        VoltCacheManager.clientRegister("113",client);
        VoltCacheManager.setDefaultClient(client);
        VoltCacheManager.releaseAllClient();
    }

}