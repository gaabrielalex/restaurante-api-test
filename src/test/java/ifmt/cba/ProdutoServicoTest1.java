package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.GrupoAlimentarDTO;
import ifmt.cba.dto.ProdutoDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.time.Instant;

public class ProdutoServicoTest1 {

    @BeforeAll
    public static void setup() {
        // Configuração básica para o Rest Assured
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/produto";
    }

    @Test
    public void aoAdicionarProduto_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        ProdutoDTO produtoInserido = new ProdutoDTO();
        try {
            produtoInserido = obterProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoInserido)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(produtoInserido.getNome()))
                .body("custoUnidade", Matchers.is(produtoInserido.getCustoUnidade()))
                .body("valorEnergetico", Matchers.is(produtoInserido.getValorEnergetico()))
                .body("estoque", Matchers.is(produtoInserido.getEstoque()))
                .body("estoqueMinimo", Matchers.is(produtoInserido.getEstoqueMinimo()))
                .body("grupoAlimentar.codigo", Matchers.is(produtoInserido.getGrupoAlimentar().getCodigo()))
                .body("link", Matchers.is(Matchers.notNullValue()))
                .body("link", Matchers.is(Matchers.not(Matchers.emptyString())));
    }

    @Test
    public void aoAdicionarProdutoJaExistente_DeveRetornarRespostaComStatus400EMesangemDeErroCorrespodente() {
        ProdutoDTO produtoExistente = new ProdutoDTO();
        try {
            produtoExistente = obterProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("texto", Matchers.is("Ja existe esse produto"));
    }

    @Test
    public void aoAlterarProduto_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        Gson gson = new Gson();
        ProdutoDTO produtoInserido = new ProdutoDTO();
        try {
            produtoInserido = obterProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(produtoInserido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        produtoInserido = gson.fromJson(responsePost.getBody().asString(), ProdutoDTO.class);

        produtoInserido.setEstoque(produtoInserido.getEstoque() - 10);
        produtoInserido.setEstoqueMinimo(produtoInserido.getEstoqueMinimo() - 5);
        produtoInserido.setCustoUnidade(produtoInserido.getCustoUnidade() + 5.0f);
        produtoInserido.setValorEnergetico(produtoInserido.getValorEnergetico() + 10);
        produtoInserido.setNome(produtoInserido.getNome() + " - Alterado");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoInserido)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("codigo", Matchers.is(produtoInserido.getCodigo()))
                .body("nome", Matchers.is(produtoInserido.getNome()))
                .body("custoUnidade", Matchers.is(produtoInserido.getCustoUnidade()))
                .body("valorEnergetico", Matchers.is(produtoInserido.getValorEnergetico()))
                .body("estoque", Matchers.is(produtoInserido.getEstoque()))
                .body("estoqueMinimo", Matchers.is(produtoInserido.getEstoqueMinimo()))
                .body("grupoAlimentar.codigo", Matchers.is(produtoInserido.getGrupoAlimentar().getCodigo()))
                .body("link", Matchers.is(Matchers.notNullValue()))
                .body("link", Matchers.is(Matchers.not(Matchers.emptyString())));
    }

    @Test
    public void aoAlterarProdutoInexistente_DeveRetornarRespostaComStatus400EMesangemDeErroCorrespodente() {
        ProdutoDTO produtoInexistente = new ProdutoDTO();
        try {
            produtoInexistente = obterProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        produtoInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("texto", Matchers.is("Nao existe esse produto"));
    }

    @Test
    public void aoExcluirProduto_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        ProdutoDTO produtoInserido = new ProdutoDTO();
        try {
            produtoInserido = obterProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(produtoInserido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        produtoInserido = gson.fromJson(responsePost.getBody().asString(), ProdutoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", produtoInserido.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirProdutoInexistente_DeveRetornarRespostaComStatus400EMesangemDeErroCorrespodente() {
        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", -1)
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(400)
                .body("texto", Matchers.is("Nao existe esse produto"));
    }

    @Test
    public void aoBuscarProdutoPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        ProdutoDTO produtoExistente = new ProdutoDTO();
        try {
            produtoExistente = obterProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", produtoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(produtoExistente.getCodigo()))
                .body("nome", Matchers.is(produtoExistente.getNome()))
                .body("custoUnidade", Matchers.is(produtoExistente.getCustoUnidade()))
                .body("valorEnergetico", Matchers.is(produtoExistente.getValorEnergetico()))
                .body("estoque", Matchers.is(produtoExistente.getEstoque()))
                .body("estoqueMinimo", Matchers.is(produtoExistente.getEstoqueMinimo()))
                .body("grupoAlimentar.codigo", Matchers.is(produtoExistente.getGrupoAlimentar().getCodigo()))
                .body("link", Matchers.is(Matchers.notNullValue()))
                .body("link", Matchers.is(Matchers.not(Matchers.emptyString())));
    }

    @Test
    public void aoBuscarProdutoPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        Gson gson = new Gson();
        ProdutoDTO produtoExistente = new ProdutoDTO();
        try {
            produtoExistente = obterProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter produto válido");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", produtoExistente.getNome())
            .when()
                .log().all()
                .get();

        Assertions.assertEquals(200, response.getStatusCode());
        ProdutoDTO[] produtos = gson.fromJson(response.getBody().asString(), ProdutoDTO[].class);

        Assertions.assertEquals(produtoExistente.getCodigo(), produtos[0].getCodigo());
        Assertions.assertEquals(produtoExistente.getNome(), produtos[0].getNome());
        Assertions.assertEquals(produtoExistente.getCustoUnidade(), produtos[0].getCustoUnidade());
        Assertions.assertEquals(produtoExistente.getValorEnergetico(), produtos[0].getValorEnergetico());
        Assertions.assertEquals(produtoExistente.getEstoque(), produtos[0].getEstoque());
        Assertions.assertEquals(produtoExistente.getEstoqueMinimo(), produtos[0].getEstoqueMinimo());
        Assertions.assertEquals(produtoExistente.getGrupoAlimentar().getCodigo(), produtos[0].getGrupoAlimentar().getCodigo());
    }

    @Test
    public void aoObterProdutosAbaixoDoEstoqueMinimo_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        Gson gson = new Gson();

        Response response = RestAssured
            .given()
                .log().all()
            .when()
                .log().all()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/estoquebaixo");

        Assertions.assertEquals(200, response.getStatusCode());

        ProdutoDTO[] produtos = gson.fromJson(response.getBody().asString(), ProdutoDTO[].class);

        for (ProdutoDTO produto : produtos) {
            Assertions.assertTrue(produto.getEstoque() < produto.getEstoqueMinimo());
        }
    }
    
    public static GrupoAlimentarDTO obterGrupoAlimentarValidoDaApi() throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/grupoalimentar/codigo/1");
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter grupo alimentar válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);
        }
    }

    public static ProdutoDTO obterProdutoValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + RestAssured.basePath + "/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter produto válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), ProdutoDTO.class);
        }
    }

    public static ProdutoDTO obterProdutoValido() throws Exception {
        GrupoAlimentarDTO grupoAlimentarDTO = obterGrupoAlimentarValidoDaApi();

        ProdutoDTO produtoInserido = new ProdutoDTO();
        produtoInserido.setNome("Alcatra bovina - " + Instant.now());
        produtoInserido.setCustoUnidade(10.0f);
        produtoInserido.setValorEnergetico(200);
        produtoInserido.setEstoque(100);
        produtoInserido.setEstoqueMinimo(10);
        produtoInserido.setGrupoAlimentar(grupoAlimentarDTO);
        
        return produtoInserido;
    }
}
