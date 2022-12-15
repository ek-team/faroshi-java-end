package cn.cuptec.faros.common.exception;

import lombok.Data;

@Data
public class OuterException extends RuntimeException{

    public OuterException(String returnMsg) {
        this.returnMsg = returnMsg;
        this.args = new Object[]{};
        this.outerException = null;
    }



    public OuterException(String returnMsg, Object[] args, Exception outerException) {
        this.returnMsg = returnMsg;
        this.args = args;
        this.outerException = outerException;
    }

    private String returnMsg;

    private Object[] args;

    private Exception outerException;

}
