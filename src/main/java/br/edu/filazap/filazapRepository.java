package br.edu.filazap;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class filazapRepository {
    private final CqlSession session;
    private final PreparedStatement insert, insertChamado, select, delete, incAtendidos, selAtendidos;

    public filazapRepository(CqlSession session) {
        this.session = session;
        criarEsquema();
        insert = session.prepare("INSERT INTO filazap.fila (local, entrada, senha_id, nome, curso, status) VALUES (?, ?, ?, ?, ?, 'aguardando')");
        insertChamado = session.prepare("INSERT INTO filazap.fila (local, entrada, senha_id, nome, curso, status) VALUES (?, ?, ?, ?, ?, 'chamado') USING TTL 180");
        select = session.prepare("SELECT entrada, senha_id, nome, curso, status FROM filazap.fila WHERE local = ?");
        delete = session.prepare("DELETE FROM filazap.fila WHERE local = ? AND entrada = ?");
        incAtendidos = session.prepare("UPDATE filazap.atendidos SET total = total + 1 WHERE local = ?");
        selAtendidos = session.prepare("SELECT total FROM filazap.atendidos WHERE local = ?");
    }

    private void criarEsquema() {
        session.execute("CREATE KEYSPACE IF NOT EXISTS filazap WITH replication = {'class':'SimpleStrategy','replication_factor':1}");
        session.execute("CREATE TABLE IF NOT EXISTS filazap.fila (local text, entrada timeuuid, senha_id uuid, nome text, curso text, status text, PRIMARY KEY ((local), entrada)) WITH CLUSTERING ORDER BY (entrada ASC)");
        session.execute("CREATE TABLE IF NOT EXISTS filazap.atendidos (local text PRIMARY KEY, total counter)");
    }

    // CREATE
    public void entrar(String local, String nome, String curso) {
        session.execute(insert.bind(local, Uuids.timeBased(), UUID.randomUUID(), nome, curso));
    }

    // READ
    public List<Row> listar(String local) {
        return session.execute(select.bind(local)).all();
    }

    public int posicao(String local, String nome) {
        int pos = 0;
        for (Row r : listar(local)) {
            if ("aguardando".equals(r.getString("status"))) {
                pos++;
                if (r.getString("nome").equalsIgnoreCase(nome)) return pos;
            }
        }
        return -1;
    }

    // UPDATE (chama o próximo; a senha chamada expira em 180 s)
    public Optional<Row> chamarProximo(String local) {
        for (Row r : listar(local)) {
            if ("aguardando".equals(r.getString("status"))) {
                session.execute(insertChamado.bind(local, r.getUuid("entrada"),
                        r.getUuid("senha_id"), r.getString("nome"), r.getString("curso")));
                session.execute(incAtendidos.bind(local));
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    // DELETE
    public boolean sair(String local, String nome) {
        for (Row r : listar(local)) {
            if (r.getString("nome").equalsIgnoreCase(nome)) {
                session.execute(delete.bind(local, r.getUuid("entrada")));
                return true;
            }
        }
        return false;
    }

    public long atendidos(String local) {
        Row r = session.execute(selAtendidos.bind(local)).one();
        return r == null ? 0 : r.getLong("total");
    }
}