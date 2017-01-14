package aub.product.spring.boot.dynamic.datasource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Order(0)// 保证该AOP在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {


    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint point, TargetDataSource targetDataSource) {
        String master = targetDataSource.master();
        DynamicDataSourcePolicy slavePolicy = targetDataSource.slavePolicy();
        if (master == null) {
            throw new RuntimeException("TargetDataSource error: master is null");
        }

        DynamicDataSourceContextHolder.setDataSourceType(master, slavePolicy);
    }

    @After("@annotation(targetDataSource)")
    public void restoreDataSource(JoinPoint point, TargetDataSource targetDataSource) {
        DynamicDataSourceContextHolder.clearDataSourceType();
    }

}