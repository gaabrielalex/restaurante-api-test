package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.EntregadorDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.time.Instant;

public class EntregadorServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/entregador";
    }

    @Test
    public void aoAdicionarEntregador_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        EntregadorDTO entregador = obterEntregadorValido();

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregador)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(entregador.getNome()))
                .body("RG", Matchers.is(entregador.getRG()))
                .body("CPF", Matchers.is(entregador.getCPF()))
                .body("telefone", Matchers.is(entregador.getTelefone()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarEntregadorJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        EntregadorDTO entregadorExistente = new EntregadorDTO();
        try {
            entregadorExistente = obterEntregadorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter entregador da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregadorExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse entregador"));
    }

    @Test
    public void aoAlterarEntregador_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        EntregadorDTO entregador = obterEntregadorValido();

        // Adiciona o entregador para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(entregador)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        entregador = gson.fromJson(responsePost.getBody().asString(), EntregadorDTO.class);

        // Altera o entregador
        entregador.setNome(entregador.getNome() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregador)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(entregador.getCodigo()))
                .body("nome", Matchers.is(entregador.getNome()));
    }

    @Test
    public void aoAlterarEntregadorInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        EntregadorDTO entregadorInexistente = obterEntregadorValido();
        entregadorInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(entregadorInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse entregador"));
    }

    @Test
    public void aoExcluirEntregador_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        EntregadorDTO entregador = obterEntregadorValido();

        // Adiciona o entregador para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(entregador)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        entregador = gson.fromJson(responsePost.getBody().asString(), EntregadorDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", entregador.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirEntregadorInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse entregador"));
    }

    @Test
    public void aoBuscarEntregadorPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        EntregadorDTO entregadorExistente = new EntregadorDTO();
        try {
            entregadorExistente = obterEntregadorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter entregador da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", entregadorExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(entregadorExistente.getCodigo()))
                .body("nome", Matchers.is(entregadorExistente.getNome()))
                .body("RG", Matchers.is(entregadorExistente.getRG()))
                .body("CPF", Matchers.is(entregadorExistente.getCPF()))
                .body("telefone", Matchers.is(entregadorExistente.getTelefone()));
    }

    @Test
    public void aoBuscarEntregadorPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        EntregadorDTO entregadorExistente = new EntregadorDTO();
        try {
            entregadorExistente = obterEntregadorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter entregador válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", entregadorExistente.getNome())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/entregador");

        Assertions.assertEquals(200, response.getStatusCode());
        EntregadorDTO[] entregadores = gson.fromJson(response.getBody().asString(), EntregadorDTO[].class);

        Assertions.assertTrue(entregadores.length > 0, "Nenhum entregador encontrado com o nome especificado");
        Assertions.assertEquals(entregadorExistente.getCodigo(), entregadores[0].getCodigo());
        Assertions.assertEquals(entregadorExistente.getNome(), entregadores[0].getNome());
    }

    public static EntregadorDTO obterEntregadorValido() {
        EntregadorDTO entregador = new EntregadorDTO();
        entregador.setNome("Ent" + Instant.now());
        entregador.setRG(("R" + Instant.now().getEpochSecond()));
        entregador.setCPF("C" + Instant.now().getEpochSecond());
        entregador.setTelefone("T" + Instant.now().getEpochSecond());
        return entregador;
    }

    public static EntregadorDTO obterEntregadorValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/entregador/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter entregador válido");
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), EntregadorDTO.class);
        }
    }
}
