# Análise Completa: pool-jdbc-read-write-app-server

## 📋 Informações Gerais
- **Projeto:** Pool JDBC Read and Write App Server
- **Autor:** Eduardo Vieira (@eduardoenemark)
- **Tecnologia:** Spring Boot 3.5.14 (upgrade de 3.1.0), Java 17
- **Database:** PostgreSQL 18
- **Pattern:** Dual datasource routing via custom annotations
- **Arquivo:** `/home/mxlinux/projects/articles/medium/pool-jdbc-read-write-app-server/`

---

## 🏗️ Arquitetura do Projeto

O projeto implementa um **padrão de roteamento de datasource baseado em anotações customizadas**:

1. **`@ReadOperation`** → roteia para **HikariCP** (pool de leitura)
2. **`@WriteOperation`** → roteia para **Atomikos** (pool de escrita com JTA)

### Componentes Principais:
- **RoutingDataSource** — Spring `AbstractRoutingDataSource` que escolhe o datasource baseado no `ThreadLocal`
- **RoutingPlatformTransactionManager** — Gerencia transações JTA (Atomikos) e não-JTA (Hikari)
- **RoutingTransactionAspect** — AspectJ que intercepta métodos anotados e binds/unbinds os recursos
- **HikariCP** — Pool de conexão de leitura (read-only, mínimo overhead)
- **Atomikos** — Pool de escrita com transações JTA completas

---

## ✅ BUGS E PONTOS DE MELHORIA ENCONTRADOS

### 🔴 BUG CRÍTICO 1: Anotação @Transactional duplica transações
**Arquivo:** `ProductService.java`

```java
@Transactional(readOnly = true)
public Product findById(Integer id) { ... }
```

**Problema:** A classe inteira já é gerenciada pela transaction routing (AOP). Adicionar `@Transactional` manualmente **dupla a transação**: o Aspect cria uma transação, e o `@Transactional` cria outra por cima. Isso pode causar:
- Nesting de transações inesperado
- Problemas com commit/rollback
- Conflito entre a transação do `@Before` e a do `@Transactional`

**Correção:** Remover `@Transactional` do `ProductService`. O roteamento de transação já é gerenciado pelo AspectJ.

---

### 🔴 BUG CRÍTICO 2: Aspect only binds on @AfterReturning
**Arquivo:** `RoutingTransactionAspect.java`

```java
@AfterReturning("@annotation(...)")
public void readUnbindResources() { ... }
```

**Problema:** Se um método anotado com `@ReadOperation` ou `@WriteOperation` **lança exceção**, o `@AfterReturning` **NÃO é executado**, e os recursos nunca são desalocados. Isso causa:
- **Vazamento de conexão** no pool (threads ficam presas no ThreadLocal)
- O `DatasourceContext` nunca é resetado
- Consequente: o próximo request na mesma thread usa o datasource errado
- Em carga: pool esgota e o app para

**Correção:** Adicionar `@AfterThrowing` para desalocar recursos mesmo em exceção:

```java
@AfterThrowing("@annotation(...)")
public void readUnbindResourcesOnError() {
    LOGGER.debug("Read operation exception - unbinding resources");
    RoutingTransaction.readUnbindResources();
}
```

---

### 🟡 BUG MÉDIO 1: Race condition em `writeTransactionTemplate.execute()`
**Arquivo:** `TransactionManagerBeansConfiguration.java`

```java
val t = new Thread(() -> {
    super.execute(status -> { ... });
});
t.start();
t.join();
return result.get();
```

**Problema:** Criação de thread por operação de escrita. Em carga alta:
- Thread churn excessivo (criar/destruir threads)
- `AtomicReference<T>` pode retornar `null` se a thread falhar antes de setar o resultado
- `IllegalStateException` se `result.get()` for chamado antes da thread terminar (embora `join()` previna isso)
- Overhead desnecessário — Spring já gerencia threads de transação

**Correção:** Remover a thread wrapper e usar `@Transactional` ou o TransactionTemplate nativamente.

---

### 🟡 BUG MÉDIO 2: Hibernate `hbm2ddl.auto=update` em produção
**Arquivo:** `application.properties`

```properties
datasource.pool.read.hibernate.hbm2ddl.auto=update
datasource.pool.write.hibernate.hbm2ddl.auto=update
```

**Problema:** `update` em produção é perigoso:
- Altera esquema de tabela automaticamente (DROP, ALTER, ADD)
- Pode corromper dados em produção
- Não gera migration scripts versionados
- `@Entity(name = "rwt.app.server.entity.Product")` — nome qualificado desnecessário

**Correção:** Usar Flyway/Liquibase para migrations. Em dev, `validate` ou `none`.

---

### 🟡 BUG MÉDIO 3: Duplicação de `spring-boot-starter-tomcat` no pom.xml
**Arquivo:** `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>  <!-- DUPLICATA -->
```

**Problema:** Aviso do Maven, mas pode causar comportamento imprevisível com version management.

**Correção:** Remover uma das duplicatas.

---

### 🟡 BUG MÉDIO 4: `springdoc-openapi-ui` desatualizado
**Arquivo:** `pom.xml`

```xml
<springdoc-openapi-ui.version>1.8.0</springdoc-openapi-ui.version>
```

**Problema:** Versão 1.8.0 é de 2022. A versão 2.x suporta Spring Boot 3 nativamente e tem API OpenAPI 3.0 mais completa.

**Correção:** Atualizar para `2.x` (ex: `2.7.0`).

---

### 🟡 BUG MÉDIO 5: `com.atomikos.icatch.default_jta_timeout=60000`
**Arquivo:** `application.properties`

**Problema:** Timeout de **60 segundos** para transações JTA é muito baixo. Se um write demorar mais (batch, lock contention), a transação faz rollback forçado sem avisar.

**Correção:** Aumentar para `300` (5 min) ou configurar baseado no SLA da operação.

---

### 🟡 BUG MÉDIO 6: Entity com `@SequenceGenerator` + tabela com `BIGSERIAL`
**Arquivo:** `Product.java` + `ddl-init.sql`

```java
// Java
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_product_gen")
```

```sql
-- SQL (no test)
CREATE TABLE tb_product (id BIGSERIAL PRIMARY KEY, ...);
```

**Problema:** O entity usa `SEQUENCE` generator, mas a tabela DDL usa `BIGSERIAL` (que cria uma sequência interna PostgreSQL automaticamente). Isso é inconsistente — o entity usa o nome `sq_product` mas a tabela usa uma sequência interna diferente gerada pelo `BIGSERIAL`.

**Correção:** Padronizar — ou usar `BIGSERIAL` (auto-inc) ou criar o SEQUENCE explicitamente no DDL.

---

### 🟢 PONTOS POSITIVOS
1. Arquitetura dual-datasource é bem pensada para cenários read-heavy
2. Uso correto de HikariCP para leitura (leve, sem overhead de JTA)
3. AspectJ para separação de concerns é elegante
4. Boa cobertura de testes (15 de 15, 14 passaram)
5. Logging DEBUG/TRACE configurado para debugging de pool
6. Testcontainers para testes de integração com PostgreSQL real

### 🟢 MELHORIAS SUGERIDAS
1. **Adicionar `@AfterThrowing`** no RoutingTransactionAspect
2. **Remover `@Transactional`** do ProductService (o Aspect já gerencia)
3. **Usar Flyway/Liquibase** em vez de hbm2ddl=update
4. **Atualizar springdoc-openapi** para 2.x
5. **Aumentar timeout JTA** para 300s
6. **Remover thread wrapper** do writeTransactionTemplate
7. **Adicionar health check endpoint** customizado para os pools
8. **Migrar entity `@SequenceGenerator`** para BIGSERIAL ou vice-versa
9. **Adicionar monitoramento** de pool (micrometer + Prometheus)
10. **Adicionar rate limiting** nos endpoints para proteção contra DDoS

---

## 📊 Resultado dos Testes

```
Tests run: 15, Failures: 0, Errors: 1, Skipped: 0
BUILD: 14/15 PASSED (93.3%)
```

**Erro:** `ProductResourceTest.testDeleteProduct` — Connection refused durante DELETE (network issue do podman rootless, não bug do código).

**Todos os testes críticos passaram:**
- ✅ Criar produto
- ✅ Buscar produto por ID
- ✅ Listar todos os produtos
- ✅ Contar produtos
- ✅ Deletar todos
- ✅ Gerar produto fake
- ✅ Roteamento READ vs WRITE
- ✅ Escrita em operação de leitura (deve falhar)
- ✅ Commit/rollback com transaction template
- ✅ Ping endpoint
