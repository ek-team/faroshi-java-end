package cn.cuptec.faros.common.exception;

import lombok.Data;

@Data
public class InnerException extends RuntimeException {


    public InnerException(String returnMsg) {
        this.returnMsg = returnMsg;
        this.args = new Object[]{};
        this.innerException = null;
    }


    public InnerException(String returnMsg, Object[] args, Exception innerException) {
        this.returnMsg = returnMsg;
        this.args = args;
        this.innerException = innerException;
    }

    private String returnMsg;

    private Object[] args;

    private Exception innerException;

}
