package cache;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by swqsh on 2017/7/11.
 */
public class AspectUtilsTest {
    @Test
    public void generateNamesToObjMapper() throws Exception {

    }

    @Test
    public void isConditionValid() throws Exception {
        Boolean first = AspectUtils.isConditionValid("are you ok",null);
        Boolean second = AspectUtils.isConditionValid("true ",new HashMap());
        Map map = new HashMap();
        map.put("a",4);
        map.put("b",3);
        Boolean third = AspectUtils.isConditionValid("a>3&&b<4",map);
        assertFalse(first);
        assertTrue(second);
        assertTrue(third);
    }

    @Test
    public void resolveParams() throws Exception {
        int[] obj = new int[] {1,2,3,4,5};
        if(obj.getClass().isArray()){
            System.out.println("isArray");
            System.out.println(Array.getLength(obj));
            Object[] res = Arrays.asList(obj).toArray();
            for(int i=0;i<res.length;i++)
            System.out.println(res[i]);
            System.out.println(res.length);
        }
    }

}