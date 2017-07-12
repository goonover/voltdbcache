package cache;

import cache.exceptions.CallbackInitException;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import org.apache.log4j.Logger;
import org.voltdb.client.NullCallback;
import org.voltdb.client.ProcedureCallback;

import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维护一个VoltCallback与名称映射表，每次初始化时，都会自动寻找所有被{@link cache.annotations.VoltCallback}标识的类
 * Created by swqsh on 2017/7/6.
 */
public class VoltCallbackManager {

    private Logger logger = Logger.getLogger(VoltCallbackManager.class);

    ConcurrentHashMap<String,VoltCallback> callbacks = new ConcurrentHashMap<String,VoltCallback>();

    private ProcedureCallback nullCallback = new NullCallback();

    public VoltCallbackManager(){
        scanCallback();
    }

    /**
     * 在工作目录的所有jar包、依赖以及路径文件中寻找有{@link cache.annotations.VoltCallback}标志的类，添加到
     * {@link VoltCallbackManager#callbacks}中
     * TODO:支持黑名单及白名单机制
     */
    private void scanCallback() {
        FastClasspathScanner scanner = new FastClasspathScanner();
        scanner.setAnnotationVisibility(RetentionPolicy.RUNTIME).
                matchClassesWithAnnotation(cache.annotations.VoltCallback.class, new ClassAnnotationMatchProcessor() {
                    @Override
                    public void processMatch(Class<?> classWithAnnotation) {
                        cache.annotations.VoltCallback anno = classWithAnnotation.getAnnotation(cache.annotations.VoltCallback.class);
                        String name = anno.callbackName();
                        boolean shareable = anno.shareable();
                        VoltCallback callbackMeta = new VoltCallback(name, shareable, classWithAnnotation);
                        callbacks.putIfAbsent(name, callbackMeta);
                    }
                });
        scanner.scan();
    }

    /**
     * 获取ProcedureCallback，任何初始化失败都说明使用者在不适当的地方使用了{@link cache.annotations.VoltCallback}标志，
     * 将相关的元数据移除，对于所有不适当初始化以及不存在的回调，都应该返回NullCallbak
     * @param name
     * @return
     */
    public ProcedureCallback getCallback(String name){
        VoltCallback callbackMeta = callbacks.get(name);
        if(callbackMeta == null){
            //throw new IllegalArgumentException("can not find procedure by name:"+name);
            logger.warn("callback:"+name+" can't be found, use NullCallback instead");
            return nullCallback;
        }
        ProcedureCallback res;
        try{
            res = callbackMeta.getCallback();
        }catch (CallbackInitException e){
            logger.warn(e);
            logger.info("remove callback:"+name+" from callback manager");
            callbacks.remove(name);
            return nullCallback;
        }
        return res;
    }

    /**
     * 向{@link VoltCallbackManager#callbacks}中添加注册新callback
     * @param name  callbacks中的key
     * @param callback
     * @return  返回true表示注册成功，false表示注册失败；
     */
    public boolean register(String name,VoltCallback callback){
        VoltCallback res=callbacks.putIfAbsent(name,callback);
        if(res!=null) {
            logger.info(name+" exists, register failed");
            return false;
        }
        return true;
    }

    public VoltCallback remove(String name){
       return callbacks.remove(name);
    }

}
