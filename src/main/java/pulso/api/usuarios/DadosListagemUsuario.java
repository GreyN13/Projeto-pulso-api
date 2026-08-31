package pulso.api.usuarios;

import java.util.Date;

public record DadosListagemUsuario(Long id, String nome, String email, Date criadoEm) {
    public DadosListagemUsuario(Usuario dados){
        this(dados.getId(),dados.getNome(),dados.getEmail(),dados.getCriadoEm());
    }
}
