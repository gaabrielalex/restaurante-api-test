package ifmt.cba.dto;

public class QuantidadeDescartadaProdutoDTO {

    private ProdutoDTO produto;
    private int quantidadeDescartada;

    public ProdutoDTO getProduto() {
        return produto;
    }

    public void setProduto(ProdutoDTO produto) {
        this.produto = produto;
    }

    public int getQuantidadeDescartada() {
        return quantidadeDescartada;
    }

    public void setQuantidadeDescartada(int quantidade) {
        this.quantidadeDescartada = quantidade;
    }
}
