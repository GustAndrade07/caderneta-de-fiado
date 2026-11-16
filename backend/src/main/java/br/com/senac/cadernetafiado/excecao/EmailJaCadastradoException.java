package br.com.senac.cadernetafiado.excecao;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("E-mail já cadastrado");
    }
}
