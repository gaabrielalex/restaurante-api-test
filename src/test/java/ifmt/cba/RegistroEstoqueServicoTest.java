package ifmt.cba;

import java.time.LocalDate;
import java.time.LocalTime;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.MovimentoEstoqueDTO;
import ifmt.cba.dto.RegistroEstoqueDTO;
import ifmt.cba.utils.ApiUtils;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;

public class RegistroEstoqueServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/registro-estoque";
    }

    @Test
    public void aoAdicionarRegistroEstoqueComMovimentoDeCompra_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        RegistroEstoqueDTO registroEstoque = obterRegistroEstoqueValido(MovimentoEstoqueDTO.COMPRA);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonRegistroEstoque = gson.toJson(registroEstoque);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoque)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("produto", Matchers.notNullValue())
                .body("produto.codigo", Matchers.equalTo(registroEstoque.getProduto().getCodigo()))
                .body("movimento", Matchers.notNullValue())
                .body("movimento", Matchers.equalTo("COMPRA"))
                .body("data", Matchers.notNullValue())
                .body("quantidade", Matchers.equalTo(registroEstoque.getQuantidade()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarRegistroEstoqueComMovimentoDeDanificado_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        RegistroEstoqueDTO registroEstoque = obterRegistroEstoqueValido(MovimentoEstoqueDTO.DANIFICADO);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonRegistroEstoque = gson.toJson(registroEstoque);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoque)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("produto", Matchers.notNullValue())
                .body("produto.codigo", Matchers.equalTo(registroEstoque.getProduto().getCodigo()))
                .body("movimento", Matchers.notNullValue())
                .body("movimento", Matchers.equalTo("DANIFICADO"))
                .body("data", Matchers.notNullValue())
                .body("quantidade", Matchers.equalTo(registroEstoque.getQuantidade()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoExcluirRegistroEstoqueComMovimentoDeCompra_DeveRetornarRespostaComStatus204() {
        RegistroEstoqueDTO registroEstoque = obterRegistroEstoqueValido(MovimentoEstoqueDTO.COMPRA);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonRegistroEstoque = gson.toJson(registroEstoque);

        String link = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoque)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("link");

        RestAssured
            .given()
                .log().all()
            .when()
                .log().all()
                .request(Method.DELETE, ApiUtils.urlBase + link)
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirRegistroEstoqueComMovimentoDeDanificado_DeveRetornarRespostaComStatus204() {
        RegistroEstoqueDTO registroEstoque = obterRegistroEstoqueValido(MovimentoEstoqueDTO.DANIFICADO);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonRegistroEstoque = gson.toJson(registroEstoque);

        String link = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoque)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("link");

        RestAssured
            .given()
                .log().all()
            .when()
                .log().all()
                .request(Method.DELETE, ApiUtils.urlBase + link)
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoObterRelatorioDeProdutosDescartados_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        RegistroEstoqueDTO registroEstoqueProducao = obterRegistroEstoqueValido(MovimentoEstoqueDTO.PRODUCAO);
        RegistroEstoqueDTO registroEstoqueVencimento = obterRegistroEstoqueValido(MovimentoEstoqueDTO.VENCIMENTO);
        RegistroEstoqueDTO registroEstoqueDanificado = obterRegistroEstoqueValido(MovimentoEstoqueDTO.DANIFICADO);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonRegistroEstoqueProducao = gson.toJson(registroEstoqueProducao);
        String jsonRegistroEstoqueVencimento = gson.toJson(registroEstoqueVencimento);
        String jsonRegistroEstoqueDanificado = gson.toJson(registroEstoqueDanificado);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoqueProducao)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoqueVencimento)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonRegistroEstoqueDanificado)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200);

        String dataInicio = LocalDate.now().minusDays(1).toString();
        String dataFim = LocalDate.now().toString();

        RestAssured
            .given()
                .log().all()
                .queryParam("dataInicio", dataInicio)
                .queryParam("dataFim", dataFim)
            .when()
                .log().all()
                .request(Method.GET, "/relatorio/produtos-descartados")
            .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(3))
                .body("[0].produto", Matchers.notNullValue())
                .body("[0].produto.codigo", Matchers.equalTo(registroEstoqueProducao.getProduto().getCodigo()))
                .body("[0].quantidadeDescartada", Matchers.equalTo(registroEstoqueProducao.getQuantidade()))
                .body("[1].produto", Matchers.notNullValue())
                .body("[1].produto.codigo", Matchers.equalTo(registroEstoqueVencimento.getProduto().getCodigo()))
                .body("[1].quantidadeDescartada", Matchers.equalTo(registroEstoqueVencimento.getQuantidade()))
                .body("[2].produto", Matchers.notNullValue())
                .body("[2].produto.codigo", Matchers.equalTo(registroEstoqueDanificado.getProduto().getCodigo()))
                .body("[2].quantidadeDescartada", Matchers.equalTo(registroEstoqueDanificado.getQuantidade()));
    }

    public static RegistroEstoqueDTO obterRegistroEstoqueValido(MovimentoEstoqueDTO movimento) {
        try {
            RegistroEstoqueDTO registroEstoqueDTO = new RegistroEstoqueDTO();
            registroEstoqueDTO.setProduto(ProdutoServicoTest.obterProdutoValidoDaApi(1));
            registroEstoqueDTO.setData(LocalDate.now());
            registroEstoqueDTO.setMovimento(movimento);
            registroEstoqueDTO.setQuantidade(1);
            return registroEstoqueDTO;
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Erro ao obter registro de estoque válido");
            return null;
        }
    }
}
