package com.mycompany.app.models;

import com.mycompany.app.handlers.AluguelHandler;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

public class Aluguel {
    private static final Map<UUID, Aluguel> storage = new HashMap<>();
    private static final Map<UUID, Aluguel> alugueisAtivos = new HashMap<>();

    private UUID id;
    private UUID ciclistaId;
    private UUID trancaInicioId;
    private UUID bicicletaId;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFim;
    private UUID trancaFimId;
    private Double valorFixo = 10.00;
    private Double valorExtra = 0.00;

    private Aluguel(UUID ciclistaId, UUID trancaInicioId, UUID bicicletaId) {
        this.id = UUID.randomUUID();
        this.ciclistaId = ciclistaId;
        this.trancaInicioId = trancaInicioId;
        this.bicicletaId = bicicletaId;
        this.horaInicio = LocalDateTime.now();

        storage.put(this.id, this);
        alugueisAtivos.put(this.ciclistaId, this);
    }

    public static Aluguel getAluguelAtivoPorCiclistaId(UUID ciclistaId) {
        return alugueisAtivos.get(ciclistaId);
    }

    public static List<Aluguel> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    public static void restaurar() {
        storage.clear();
        alugueisAtivos.clear();
    }

    public static Aluguel realizarAluguel(UUID ciclistaId, UUID trancaInicioId) {
        Ciclista ciclista = Ciclista.getCiclistaPorId(ciclistaId);
        if (ciclista == null || !ciclista.verificarPermissaoAluguel()) {
            throw new IllegalArgumentException("Ciclista não pode alugar (inativo ou sem permissão).");
        }

        if (alugueisAtivos.containsKey(ciclistaId)) {
            throw new IllegalStateException("Ciclista já possui um aluguel ativo.");
        }

        UUID bicicletaId = simularBuscarBicicletaLivre(trancaInicioId);
        if (bicicletaId == null) {
            throw new IllegalStateException("Nenhuma bicicleta disponível nesta tranca.");
        }


        if (!simularCobrarValorFixo(ciclistaId, 10.00)) {
            throw new IllegalStateException("Cobrança inicial falhou. Aluguel não autorizado.");
        }

        Aluguel novoAluguel = new Aluguel(ciclistaId, trancaInicioId, bicicletaId);

        simularLiberarTrancaENotificar(trancaInicioId, bicicletaId, ciclistaId);

        return novoAluguel;
    }

    // --- Lógica de Negócio (Devolução) ---

    public static Aluguel realizarDevolucao(UUID trancaFimId, UUID bicicletaId) {
        UUID ciclistaId = simularBuscarCiclistaPorBicicleta(bicicletaId);
        if (ciclistaId == null) {
            throw new IllegalArgumentException("Esta bicicleta não está alugada ou ID inválido.");
        }

        Aluguel aluguel = alugueisAtivos.get(ciclistaId);
        if (aluguel == null) {
            throw new IllegalStateException("Aluguel ativo não encontrado para esta bicicleta.");
        }

        aluguel.horaFim = LocalDateTime.now();
        aluguel.trancaFimId = trancaFimId;

        aluguel.valorExtra = simularCalcularCustoAdicional(aluguel.horaInicio, aluguel.horaFim);

        if (aluguel.valorExtra > 0) {
            if (!simularCobrarValorAdicional(ciclistaId, aluguel.valorExtra)) {
                System.out.println("Aviso: Cobrança extra falhou para o ciclista " + ciclistaId);
            }
        }

        simularAtualizarTrancaENotificar(trancaFimId, bicicletaId, ciclistaId, aluguel.valorExtra);

        alugueisAtivos.remove(ciclistaId);

        return aluguel;
    }

    private static UUID simularBuscarBicicletaLivre(UUID trancaId) {
        // Simula buscar a primeira bicicleta disponível na tranca
        // O ID real da bicicleta seria retornado aqui.
        return UUID.randomUUID();
    }

    private static Boolean simularCobrarValorFixo(UUID ciclistaId, Double valor) {
        // Simula chamada ao Microsserviço de Cobrança
        return true;
    }

    private static void simularLiberarTrancaENotificar(UUID trancaId, UUID bicicletaId, UUID ciclistaId) {
        // Simula chamada ao Microsserviço de Tranca e Notificação
        System.out.println("Tranca " + trancaId + " liberada. Bicicleta " + bicicletaId + " retirada. Ciclista " + ciclistaId + " notificado.");
    }

    private static UUID simularBuscarCiclistaPorBicicleta(UUID bicicletaId) {
        // Simula buscar o ciclista que alugou a bicicleta
        // Retorna o ID do ciclista.
        return alugueisAtivos.entrySet().stream()
                .filter(entry -> entry.getValue().bicicletaId.equals(bicicletaId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static Double simularCalcularCustoAdicional(LocalDateTime inicio, LocalDateTime fim) {
        // Simula cálculo de custo extra baseado no tempo (Ex: 1 hora extra)
        long duracaoMinutos = java.time.Duration.between(inicio, fim).toMinutes();
        if (duracaoMinutos > 60) {
            return (double) ((duracaoMinutos - 60) / 60) * 5.00; // Cobra R$5 por hora extra
        }
        return 0.00;
    }

    private static Boolean simularCobrarValorAdicional(UUID ciclistaId, Double valor) {
        // Simula chamada ao Microsserviço de Cobrança para taxa extra
        return true;
    }

    private static void simularAtualizarTrancaENotificar(UUID trancaFimId, UUID bicicletaId, UUID ciclistaId, Double valorExtra) {
        // Simula chamada ao Microsserviço de Tranca e Notificação
        System.out.println("Tranca " + trancaFimId + " travada. Bicicleta " + bicicletaId + " devolvida. Ciclista " + ciclistaId + " notificado sobre taxa extra de R$" + valorExtra);
    }
}