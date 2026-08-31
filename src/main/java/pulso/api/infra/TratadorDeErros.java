package pulso.api.infra;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException erro) {
        List<ErroCampo> erros = erro.getFieldErrors().stream()
                .map(campo -> new ErroCampo(campo.getField(), campo.getDefaultMessage()))
                .toList();

        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", List.of());
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RegistroNaoEncontradoException erro) {
        return responder(HttpStatus.NOT_FOUND, erro.getMessage(), List.of());
    }

    @ExceptionHandler({ConflitoException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErroResposta> tratarConflito(RuntimeException erro) {
        String mensagem = erro instanceof ConflitoException
                ? erro.getMessage()
                : "A operação viola uma regra de unicidade ou relacionamento";
        return responder(HttpStatus.CONFLICT, mensagem, List.of());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(AcessoNegadoException erro) {
        return responder(HttpStatus.FORBIDDEN, erro.getMessage(), List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> tratarAutenticacao(AuthenticationException erro) {
        return responder(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos", List.of());
    }

    @ExceptionHandler(LimiteTentativasExcedidoException.class)
    public ResponseEntity<ErroResposta> tratarLimiteTentativas(LimiteTentativasExcedidoException erro) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, erro.getMessage(), List.of());
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String erro, List<ErroCampo> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResposta(status.value(), erro, campos, LocalDateTime.now()));
    }
}
