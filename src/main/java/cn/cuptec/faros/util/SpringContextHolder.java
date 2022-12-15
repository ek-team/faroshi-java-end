package cn.cuptec.faros.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * 获取Spring上下文对象
 *
 * @author ICode Studio
 * @version 1.0 2017年8月25日
 */
@Service
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        if(arg0 == null) return;

        applicationContext = arg0;
    }

    /**
     * 根据Bean的名字获取Bean
     *
     * @param beanName Bean的名字
     * @return Spring容器中的Bean
     */
    public static Object getBean(String beanName) {
        if(applicationContext == null) {
            return null;
        }

        return applicationContext.getBean(beanName);
    }
}