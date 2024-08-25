package ifmt.cba.dto;

public class QuantidadeProduzidasProdutosDTO {
    ProdutoDTO produto;
    int quantidadeProduzida;

    public ProdutoDTO getProduto() {
        return produto;
    }
    public void setProduto(ProdutoDTO produto) {
        this.produto = produto;
    }
    public int getQuantidadeProduzida() {
        return quantidadeProduzida;
    }
    public void setQuantidadeProduzida(int quantidadeProduzida) {
        this.quantidadeProduzida = quantidadeProduzida;
    }   
}
