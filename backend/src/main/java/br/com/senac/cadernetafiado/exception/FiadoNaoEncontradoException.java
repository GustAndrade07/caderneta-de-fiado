package br.com.senac.cadernetafiado.exception;

public class FiadoNaoEncontradoException extends RuntimeException {

    public FiadoNaoEncontradoException() {
        super("Fiado não encontrado");
    }
}
