package cache;

import cache.exceptions.CallbackInitException;
import org.voltdb.client.ProcedureCallback;

import static java.lang.reflect.Modifier.PUBLIC;

/**
 * 与{@link cache.annotations.VoltCallback}相对应的数据结构，由shareable决定是否共享。对于非共享的回调，每次调用存储
 * 过程时，都会创建一个新的实例
 * Created by swqsh on 2017/7/6.
 */
public class VoltCallback {

    //VoltCallback在{@link VoltCallbackManager}中的标识符，应该全局唯一。当标识符已被占用时，VoltCallbackManager应拒绝注册
    String callbackName;

    /**
     * Callback是否为单例的标识符，true表示才用单例模式，每次都提供实例化的{@link VoltCallback#callback}。否则每次都通过
     * {@link VoltCallback#classOfProcedureCallback}来进行实例化
     */
    boolean shareable;

    Class<?> classOfProcedureCallback;

    ProcedureCallback callback;

    Object callbackLock = new Object();

    public VoltCallback(String callbackName, boolean shareable, Class<?> classOfProcedureCallback) {
        this.callbackName = callbackName;
        this.shareable = shareable;
        this.classOfProcedureCallback = classOfProcedureCallback;
    }

    public boolean isShareable() {
        return shareable;
    }

    public ProcedureCallback getCallback() throws CallbackInitException {
        if (isShareable()) {
            /*synchronized (callbackLock) {
                if (callback == null) {
                    callback = callbackInit();
                }
            }*/
            if(callback == null){
                synchronized (callbackLock){
                    if (callback != null)
                        return callback;
                    else
                        callback = callbackInit();
                }
            }
            return callback;
        }else {
            return callbackInit();
        }
    }

    /**
     * 初始化失败时抛出CallbackInitException，目的是为了能够通过异常来通知CallbackManager去除所有不能初始化的callback
     * @return
     * @throws CallbackInitException
     */
    public ProcedureCallback callbackInit() throws  CallbackInitException{
        ProcedureCallback procedureCallback;
        try {
            //非public类均不能调用class.newInstance()
            if(classOfProcedureCallback.getModifiers()!=PUBLIC){
                throw new CallbackInitException("all the modifier must be public");
            }

            //初始化实例，任何非ProcedureCallback的类均不允许初始化
            Object object = classOfProcedureCallback.newInstance();
            if (object instanceof ProcedureCallback) {
                procedureCallback = (ProcedureCallback) object;
            }
            else {
                throw new CallbackInitException("voltdb callback must implements ProcedureCallback interface");
            }
        } catch (Exception e) {
            throw new CallbackInitException(e.getMessage(), e.getCause());
        }
        return procedureCallback;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof VoltCallback))
            return false;

        VoltCallback cmp=(VoltCallback) o;
        if(cmp.callbackName != this.callbackName)
            return false;
        if(!cmp.classOfProcedureCallback.equals(this.classOfProcedureCallback))
            return false;
        if(cmp.shareable!=this.shareable)
            return false;
        return true;
    }

    public int hashCode(){
        int hashCode=17;
        hashCode = 31*hashCode + callbackName.hashCode();
        hashCode = 31*hashCode + Boolean.hashCode(shareable);
        hashCode = 31*hashCode + classOfProcedureCallback.hashCode();
        return hashCode;
    }
}
