package pulso.api.infra;

public class LimiteTentativasExcedidoException extends RuntimeException {
    public LimiteTentativasExcedidoException(String mensagem) {
        super(mensagem);
    }
}
