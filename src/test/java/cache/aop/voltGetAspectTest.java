package cache.aop;

import cache.VoltCacheClient;
import cache.VoltCacheManager;
import cache.annotations.VoltGet;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/17.
 */
public class voltGetAspectTest {

    Logger logger = Logger.getLogger(voltGetAspectTest.class);

    @Before
    public void init(){
        VoltCacheClient client = new VoltCacheClient("10.201.0.113");
        VoltCacheManager.setDefaultClient(client);
    }

    @VoltGet(procedureName = "voltget",target = "res:(*,*,*)",params = "none")
    public VoltTable[] getAllSuccess(String account, VoltTable[] res){
        VoltTable[] temp = res;
        if(temp!=null){
            System.out.println(temp[0].toJSONString());
        }
        return temp;
    }

    @VoltGet(procedureName = "voltget",params = "none",target = "res:(*,*,1)")
    public VoltTable[] getAllFailed(String account,VoltTable[] res){
        return res;
    }

    @VoltGet(procedureName = "voltget",params = "none",target = "res:(*,*,*)")
    public Object getAllFailed1(String account,String res){
        return res;
    }

    @VoltGet(procedureName = "voltget",target = "res:(*,*,*)",params = "none")
    public void allWithRetVoid(String account,VoltTable[] res){
        if(res!=null)
            System.out.println(res[0].toJSONString());
        else{
            System.out.println("res is null");
        }
    }

    @VoltGet(procedureName = "voltget",target = "account:(0,*,account)",params = "none")
    public List getAccounts(List account){
        return account;
    }

    @VoltGet(procedureName = "voltget",target = "account:(0,*,0)",params = "none")
    public List getAccounts1(List account){
        return account;
    }

    @VoltGet(procedureName = "voltget",target = "row:(0,0,*)",params = "none")
    public VoltTableRow getVoltRow(VoltTableRow row){
        return row;
    }

    @VoltGet(procedureName = "voltget",target = "name:(0,0,0)",params = "none")
    public String getSpecificShouldSuccess(String name){
        return name;
    }

    @VoltGet(procedureName = "voltget",target = "name:(0,0,3)",params = "none")
    public String getSpecificShouldFailed(String name){
        return name;
    }

    @VoltGet(procedureName = "voltget",target = "name:(0,3,0)",params = "none")
    public String getSpecificShouldFailed2(String name){
        return name;
    }

    @VoltGet(procedureName = "voltget",target = "name:(0,0,account);securitiesCode:(0,0,securities_code)",params = "none")
    public boolean getTwoParam(String name,String securitiesCode){
        logger.info(name);
        logger.info(securitiesCode);
        return name!=null&&securitiesCode!=null;
    }

    @Test
    public void getAll() throws InterruptedException {
        VoltTable[] res = getAllSuccess("hello",null);
        assertNotNull(res);
        assertTrue(res instanceof VoltTable[]);
        res = getAllFailed("hello",null);
        assertNull(res);
        Object result = getAllFailed1("hello",null);
        assertNull(result);
    }

    @Test
    public void testVoid(){
        allWithRetVoid("hello",null);
    }

    @Test
    public void testGetAccount(){
        List res = getAccounts(null);
        for(int i=0;i<res.size();i++){
            logger.info(res.get(i));
        }
        assertNotNull(res);
        assertTrue(res.size()==2);
        res = getAccounts1(null);
        for(int i=0;i<res.size();i++){
            logger.info(res.get(i));
        }
        assertNotNull(res);
        assertTrue(res.size()==2);
    }

    @Test
    public void testGetRow(){
        VoltTableRow row = getVoltRow(null);
        assertNotNull(row);
        logger.info(row.toString());
    }

    @Test
    public void testGetSpecific(){
       String name = getSpecificShouldSuccess(null);
       assertNotNull(name);
       logger.info(name);
       name = getSpecificShouldFailed(null);
       assertNull(name);
       name = getSpecificShouldFailed2(null);
       assertNull(name);
    }

    @Test
    public void testGetTwo(){
        boolean success = getTwoParam(null,null);
        assertTrue(success);
    }
}