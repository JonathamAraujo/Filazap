# **FilaZap: Fila Virtual de Restaurante Universitário**

## 1. Principais características

- **Banco NoSQL de família de colunas (wide-column)**: os dados ficam em tabelas, mas cada partição pode ter um número enorme de linhas ordenadas.
- **Arquitetura distribuída peer-to-peer (anel)**: todos os nós são iguais, não existe nó mestre e, portanto, não há ponto único de falha.
- **Escalabilidade horizontal linear**: para ganhar capacidade, basta adicionar nós ao cluster.
- **Particionamento por hash**: a *partition key* define em qual nó cada dado fica.
- **Consistência ajustável**: o nível (ONE, QUORUM, ALL etc.) é escolhido por consulta, equilibrando consistência e disponibilidade (teorema CAP: o Cassandra prioriza AP).
- **Escrita muito rápida**: usa uma estrutura LSM-tree (commit log, memtable e SSTables), com escritas sequenciais.
- **Linguagem CQL**: sintaxe parecida com SQL, mas sem JOINs e sem subconsultas.
- **Modelagem query-first**: as tabelas são desenhadas a partir das consultas da aplicação, com desnormalização.
- **TTL por dado**: registros podem expirar automaticamente.

## 2. Nichos de aplicação

- **IoT e séries temporais**: sensores, métricas, telemetria.
- **Logs e eventos**: auditoria, rastreamento de atividades.
- **Históricos e mensageria**: feeds, mensagens, notificações.
- **Perfis, sessões e carrinhos**: acesso por chave, com muita leitura e escrita.
- **Sistemas com alto volume de escrita**: filas de atendimento, check-ins, pedidos.
- **Aplicações multirregião**: alta disponibilidade entre data centers.

## 3. Prós e contras

| Prós                                           | Contras                                                             |
| ---------------------------------------------- | ------------------------------------------------------------------- |
| Alta disponibilidade, sem ponto único de falha | Sem JOINs, subconsultas ou agregações complexas                     |
| Escala horizontal quase linear                 | Consultas limitadas à chave de partição (sem filtros livres)        |
| Escrita muito rápida                           | Modelagem rígida: mudar a consulta costuma exigir nova tabela       |
| Replicação multi-datacenter nativa             | Consistência eventual por padrão                                    |
| Consistência configurável                      | Deletes geram *tombstones* que podem degradar a leitura             |
| Bom para dados com TTL e séries temporais      | Curva de aprendizado e operação mais complexas (repair, compaction) |
| Código aberto e maduro                         | Uso de memória (heap da JVM) e disco relativamente alto             |

## 4. Capacidades do banco

### Redundância

- **Replication factor (RF)**: número de cópias de cada dado. Em produção, o comum é RF = 3.
- **Estratégias**: `SimpleStrategy` (um data center, desenvolvimento) e `NetworkTopologyStrategy` (vários data centers e racks).
- **Mecanismos de recuperação**: *hinted handoff* (guarda escritas destinadas a nós fora do ar), *read repair* (corrige divergências durante leituras) e *anti-entropy repair* (`nodetool repair`).
- **Detecção de falhas**: protocolo *gossip* entre os nós.

### Backup

- O Cassandra **não faz backup agendado por padrão**; ele oferece as ferramentas.
- **Snapshots**: `nodetool snapshot` cria um backup instantâneo (links físicos para as SSTables).
- **Backup incremental**: `incremental_backups: true` copia cada nova SSTable.
- **Auto snapshot**: ligado por padrão, gera um snapshot antes de `DROP` e `TRUNCATE`.
- **Restauração ponto-no-tempo**: via arquivamento do commit log.
- **Agendamento**: via `cron` ou ferramentas como o Medusa.
- **Exportação lógica**: `COPY` (cqlsh) para tabelas pequenas e DSBulk para grandes volumes.

## Alunos:
- **Artur Peres dos Santos
- **Jonatham Alves Araujo
