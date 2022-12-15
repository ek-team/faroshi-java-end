package cn.cuptec.faros.common.exception;

import cn.cuptec.faros.common.utils.MessageUtils;
import lombok.Getter;

/**
 * Creater: Miao
 * CreateTime: 2018/12/17 11:26
 * Description: 基础异常
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "未知错误，请稍后重试";

    public BizException(Integer errorCode, Object[] args){
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    @Override
    public String getMessage() {
        String defaultMessage;
        if (errorCode != null)
        {
            try{
                defaultMessage = MessageUtils.message(errorCode, args);
            }
            catch (Exception e){
                defaultMessage = DEFAULT_MESSAGE;
            }
        }else
            defaultMessage = DEFAULT_MESSAGE;
        return defaultMessage;
    }

}
