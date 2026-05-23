package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OperationContext implements Serializable {

    OperationType operationType;
    long startOperationMillis;
    long endOperationMillis;
    String correlationId;

    public OperationContext() {
        this.setCorrelationId();
    }

    public void start(OperationType operationType) {
        this.operationType = operationType;
        this.startOperationMillis = System.currentTimeMillis();
    }

    public void end() {
        this.endOperationMillis = System.currentTimeMillis();
    }

    public long diffMillis() {
        return this.endOperationMillis - this.startOperationMillis;
    }

    public void setCorrelationId() {
        this.correlationId = UUID.randomUUID().toString();
    }

    public long getDiff() {
        return this.diffMillis();
    }
}
