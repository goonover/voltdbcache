package cache.exceptions;

/**
 * 在{@link cache.VoltCallback}初始化voltdb回调的过程中，出现任何初始化失败的情况下，都将抛出该异常
 * Created by swqsh on 2017/7/6.
 */
public class CallbackInitException extends RuntimeException {

    static final long serialVersionUID = 1080L;

    public CallbackInitException(){super();}

    public CallbackInitException(String message){super(message);}

    public CallbackInitException(String message,Throwable cause){
        super(message,cause);
    }

    public CallbackInitException(Throwable cause){
        super(cause);
    }

    public CallbackInitException(String message,Throwable cause,boolean enableSuppression,boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
    }

}
