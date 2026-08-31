package pulso.api.infra;

public class RegistroNaoEncontradoException extends RuntimeException {
    public RegistroNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
