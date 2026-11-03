package br.edu.filazap;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.Row;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final String LOCAL = "ru";

    public static void main(String[] args) {


        DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                .build();

        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withLocalDatacenter("datacenter1")
                .withConfigLoader(loader)
                .build();
             Scanner sc = new Scanner(System.in)) {

            filazapRepository repo = new filazapRepository(session);
            int op = -1;

            do {
                System.out.println("\n=== FilaZap ===");
                System.out.println("1 - Entrar na fila");
                System.out.println("2 - Ver minha posição");
                System.out.println("3 - Listar fila");
                System.out.println("4 - Chamar próximo");
                System.out.println("5 - Sair da fila (desistir)");
                System.out.println("6 - Estatísticas");
                System.out.println("7 - Confirmar atendimento");
                System.out.println("0 - Encerrar");
                System.out.print("Opção: ");

                try {
                    op = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Digite apenas um número.");
                    op = -1;
                    continue;
                }

                switch (op) {
                    case 1 -> {
                        System.out.print("Nome: ");
                        String nome = sc.nextLine().trim();
                        System.out.print("Curso: ");
                        String curso = sc.nextLine().trim();
                        repo.entrar(LOCAL, nome, curso);
                        System.out.println(nome + " entrou na fila.");
                    }
                    case 2 -> {
                        System.out.print("Nome: ");
                        int pos = repo.posicao(LOCAL, sc.nextLine().trim());
                        if (pos == -1) {
                            System.out.println("Nome não encontrado na fila.");
                        } else {
                            System.out.println("Sua posição: " + pos);
                        }
                    }
                    case 3 -> {
                        List<Row> fila = repo.listar(LOCAL);
                        if (fila.isEmpty()) {
                            System.out.println("A fila está vazia.");
                        }
                        for (Row r : fila) {
                            System.out.println(r.getString("nome") + " | "
                                    + r.getString("curso") + " | "
                                    + r.getString("status"));
                        }
                    }
                    case 4 -> {
                        Optional<Row> chamado = repo.chamarProximo(LOCAL);
                        if (chamado.isPresent()) {
                            System.out.println("Chamando: " + chamado.get().getString("nome")
                                    + " (tem 3 minutos para comparecer)");
                        } else {
                            System.out.println("Ninguém aguardando.");
                        }
                    }
                    case 5 -> {
                        System.out.print("Nome: ");
                        boolean removido = repo.sair(LOCAL, sc.nextLine().trim());
                        System.out.println(removido ? "Removido da fila." : "Nome não encontrado.");
                    }
                    case 6 -> System.out.println("Total de atendidos: " + repo.atendidos(LOCAL));
                    case 7 -> {
                        System.out.print("Nome: ");
                        boolean ok = repo.confirmarAtendimento(LOCAL, sc.nextLine().trim());
                        System.out.println(ok
                                ? "Atendimento confirmado."
                                : "Ninguém chamado com esse nome (ou o tempo expirou).");
                    }
                    case 0 -> System.out.println("Até mais!");
                    default -> System.out.println("Opção inválida.");
                }
            } while (op != 0);
        }
    }
}