package cache.aop;

/**
 * 用于从回调中拿回执行结果
 */
public class ExecutionResult {

    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
