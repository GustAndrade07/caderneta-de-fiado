package br.com.senac.cadernetafiado.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Uma anotação na caderneta: quem comprou, quanto deve e quando.
 * Vira a tabela "fiados" no banco.
 */
@Entity
@Table(name = "fiados")
@Getter
@Setter
@NoArgsConstructor
public class Fiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    // BigDecimal evita erros de arredondamento com dinheiro (double erra centavos)
    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    // Opcional: o que foi comprado
    private String descricao;

    // Todo fiado começa como "não pago"
    @Column(nullable = false)
    private boolean pago = false;

    // Muitos fiados pertencem a um usuário.
    // Cria a coluna "usuario_id" (chave estrangeira para a tabela usuarios).
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Fiado(String nomeCliente, BigDecimal valor, LocalDate data, String descricao, Usuario usuario) {
        this.nomeCliente = nomeCliente;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
        this.usuario = usuario;
    }
}
