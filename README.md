# Atividade Prática de Sistemas Operacionais

Implementação dos exercícios sobre Comunicação entre Processos.

## Tecnologias

- Java 17
- Maven
- Concorrência com `Thread`, `BlockingQueue`, `Semaphore`, `ReentrantReadWriteLock`, `CountDownLatch` e `Vector`

## Como executar

### Compilar

```bash
mvn compile
```

Também é possível compilar sem Maven:

```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
```

### Exercício 1 - Barbeiro Dorminhoco

```bash
mvn exec:java -Dexec.mainClass="br.com.atividade.so.SleepingBarberProblem"
```

Sem Maven:

```bash
java -cp out br.com.atividade.so.SleepingBarberProblem
```

Implementa:

- 2 barbeiros.
- Fila com no máximo 10 clientes esperando.
- Mensagem quando a fila está cheia.
- Corte aleatório entre 5s e 15s.
- Chegada de novo cliente entre 4s e 6s.

### Exercício 2 - ArrayList thread safe

Classe implementada em:

```text
src/main/java/br/com/atividade/so/ThreadSafeArrayList.java
```

A implementação usa `ReentrantReadWriteLock`:

- Operações de leitura usam lock de leitura e podem acontecer simultaneamente.
- Inserções e remoções usam lock de escrita para evitar condição de corrida.

### Exercício 3 - Benchmark

```bash
mvn exec:java -Dexec.mainClass="br.com.atividade.so.ArrayListBenchmark"
```

Sem Maven:

```bash
java -cp out br.com.atividade.so.ArrayListBenchmark
```

O benchmark compara:

- `ArrayList` original x `ThreadSafeArrayList` com 1 thread.
- `ThreadSafeArrayList` x `Vector` com 16 threads.
- Operações de inserção, busca e remoção.
- Tempo em milissegundos e operações por segundo.

Os tamanhos testados estão no array `SIZES` da classe `ArrayListBenchmark` e podem ser alterados para testes maiores.

### Exercício 4 - Controle de acesso ao Banco de Dados

```bash
mvn exec:java -Dexec.mainClass="br.com.atividade.so.CrudDatabaseDemo"
```

Sem Maven:

```bash
java -cp out br.com.atividade.so.CrudDatabaseDemo
```

Implementa uma classe com operações CRUD:

- `create`
- `read`
- `update`
- `delete`

Regras atendidas:

- Até 10 consultas simultâneas.
- Apenas 1 escrita simultânea.
- Escrita bloqueia até que não haja consultas ativas.
- Consultas ficam bloqueadas quando uma escrita está em execução.

## Prints / saídas de execução

Os exemplos de execução estão na pasta `prints/`:

- `prints/exercicio1-execucao.txt`
- `prints/exercicio3-benchmark.txt`
- `prints/exercicio4-execucao.txt`
