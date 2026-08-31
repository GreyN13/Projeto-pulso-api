package pulso.api.seguranca;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class LimitadorTentativasLogin {
    private static final int LIMITE_TENTATIVAS = 67;
    private static final Duration JANELA = Duration.ofHours(1);

    private final ConcurrentHashMap<String, JanelaDeTentativas> tentativasPorIp = new ConcurrentHashMap<>();

    public boolean tentativaPermitida(String ip) {
        Instant agora = Instant.now();
        AtomicBoolean permitida = new AtomicBoolean();

        //caso algum psicopata decida usar 10 milhões de Ips pra fzr um DDOs ou algo parecido, ai dar overload no
        //servidor com certeza por causa das entradas no ConcurrentHashMap separados por Ips diferentes e etc,
        // ainda com implementação de load balancer não sei se seria suficiente,
        //de qualquer forma iria gerar congestionamento, sinceramente não tenho ideia de como parar isso Kkjsdhfksjdhf
        //preciso aprender mais sobre cybersec, vi q dava pra implementar com WAF/gateway
        //e usar Redis com TTL, mas não sei implementar

        tentativasPorIp.compute(ip, (chave, janelaAtual) -> {
            if (janelaAtual == null || !agora.isBefore(janelaAtual.inicio().plus(JANELA))) {
                permitida.set(true);
                return new JanelaDeTentativas(agora, 1);

            }

            if (janelaAtual.quantidade() >= LIMITE_TENTATIVAS) {
                permitida.set(false);
                return janelaAtual;
            }

            permitida.set(true);
            return new JanelaDeTentativas(janelaAtual.inicio(), janelaAtual.quantidade() + 1);
        });

        return permitida.get();
    }

    private record JanelaDeTentativas(Instant inicio, int quantidade) {
    }
}
