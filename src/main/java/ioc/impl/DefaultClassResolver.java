package ioc.impl;

import ioc.BeanDefinition;
import ioc.ClassResolver;

/**
 * =============================================
 *
 * @author wu
 * @create 2018-07-20 23:33
 * =============================================
 */
public class DefaultClassResolver implements ClassResolver {
    @Override
    public void resolver(BeanDefinition bean) throws ClassNotFoundException {
        String clazz = bean.getBeanClass();
        bean.setType(Class.forName(clazz));
    }
}