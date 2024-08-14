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
import io.restassured.path.json.JsonPath;
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
        GrupoAlimentarDTO grupoAlimentarDTO = new GrupoAlimentarDTO();
        try {
            grupoAlimentarDTO = obterGrupoAlimentarValido();

        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido");
        }

        ProdutoDTO produtoInserido = new ProdutoDTO();
        produtoInserido.setNome("Alcatra bovina - " + Instant.now());
        produtoInserido.setCustoUnidade(10.0f);
        produtoInserido.setValorEnergetico(200);
        produtoInserido.setEstoque(100);
        produtoInserido.setEstoqueMinimo(10);
        produtoInserido.setGrupoAlimentar(grupoAlimentarDTO);

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
    public void testConsultarPorCodigoVerificandoValores1() {
        Response resposta = RestAssured.request(Method.GET, "http://localhost:8080/produto/codigo/1");

        JsonPath jsonPath = new JsonPath(resposta.asString());
        Assertions.assertEquals(1, jsonPath.getInt("codigo"));
        Assertions.assertEquals("Alcatra bovina", jsonPath.getString("nome"));
        Assertions.assertEquals("Proteinas", jsonPath.getString("grupoAlimentar.nome"));
    }

    @Test
    public void testConsultarPorCodigoVerificandoValores2() {

        RestAssured
            .given()
            .when()
                .get("http://localhost:8080/produto/estoquebaixo")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(2))
                .body("nome", Matchers.hasItems("Batata Doce", "Costela suina"))
                .body("estoque[0]", Matchers.greaterThan(0));
    }

    @Test
    public void testInclusaoComDadosCorretos() {

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/grupoalimentar/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Gson gson = new Gson();

        GrupoAlimentarDTO grupoAlimentarDTO = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome("Teste de inclusão");
        produtoDTO.setCustoUnidade(3.0f);
        produtoDTO.setEstoque(100);
        produtoDTO.setEstoqueMinimo(10);
        produtoDTO.setValorEnergetico(50);
        produtoDTO.setGrupoAlimentar(grupoAlimentarDTO);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO)
            .when()
                .post("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    public void testAlteracaoComDadosCorretos() {

        Response response = RestAssured.request(Method.GET, "http://localhost:8080/produto/codigo/1");
        Assertions.assertEquals(200, response.getStatusCode());

        Gson gson = new Gson();
        ProdutoDTO produtoDTO = gson.fromJson(response.getBody().asString(), ProdutoDTO.class);

        int estoqueAtual = produtoDTO.getEstoque();
        produtoDTO.setEstoque(estoqueAtual - 20);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(produtoDTO)
            .when()
                .put("http://localhost:8080/produto/")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(Matchers.notNullValue()))
                .body("estoque", Matchers.is(estoqueAtual-20));
    }

    public static GrupoAlimentarDTO obterGrupoAlimentarValido() throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/grupoalimentar/codigo/1");
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter grupo alimentar válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);
        }
    }
}
