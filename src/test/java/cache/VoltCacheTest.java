package cache;

import cache.annotations.VoltCache;
import org.junit.Test;

/**
 * Created by swqsh on 2017/7/10.
 */
public class VoltCacheTest {

    @Test
    @VoltCache(procedureName = "hello",params = "condition,content,result")
    public String sayHello(String content,String condition){
        System.out.println(content);
        return "are you ok";
    }

    @Test
    @VoltCache(procedureName = "you",params = "con")
    public String sayYes(String content){
        return content;
    }

    @Test
    @VoltCache(procedureName = "hi",params = "null")
    public void testNull(){

    }

    public static void main(String[] args){
        VoltCacheTest test = new VoltCacheTest();
        test.sayHello("yoyo","three");
        test.sayYes("yes");
        test.testNull();
        /*Object t = 3;
        Object obj = new int[7];
        System.out.println(obj instanceof Object[]);
        System.out.println(obj.getClass().isArray());*/
    }
}
