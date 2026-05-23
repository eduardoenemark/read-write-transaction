package br.com.eduardoenemark.rwt.core.config.routing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoutingTransactionAspect {

    @Around("@annotation(br.com.eduardoenemark.rwt.core.operation.annotation.ReadOperation)")
    public Object readOperation(ProceedingJoinPoint pjp) throws Throwable {
        try {
            RoutingTransaction.readBindResources();
            return pjp.proceed();
        } finally {
            RoutingTransaction.readUnbindResources();
        }
    }

    @Around("@annotation(br.com.eduardoenemark.rwt.core.operation.annotation.WriteOperation)")
    public Object writeOperation(ProceedingJoinPoint pjp) throws Throwable {
        try {
            RoutingTransaction.writeBindResources();
            return pjp.proceed();
        } finally {
            RoutingTransaction.writeUnbindResources();
        }
    }
}
