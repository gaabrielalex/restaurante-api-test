package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.ColaboradorDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.time.Instant;

public class ColaboradorServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/colaborador";
    }

    @Test
    public void aoAdicionarColaborador_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        ColaboradorDTO colaborador = obterColaboradorValido();

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaborador)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(colaborador.getNome()))
                .body("RG", Matchers.is(colaborador.getRG()))
                .body("CPF", Matchers.is(colaborador.getCPF()))
                .body("telefone", Matchers.is(colaborador.getTelefone()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarColaboradorJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        ColaboradorDTO colaboradorExistente = new ColaboradorDTO();
        try {
            colaboradorExistente = obterColaboradorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter colaborador da api");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaboradorExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse colaborador"));
    }

    @Test
    public void aoAlterarColaborador_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        ColaboradorDTO colaborador = obterColaboradorValido();

        // Adiciona o colaborador para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(colaborador)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        colaborador = gson.fromJson(responsePost.getBody().asString(), ColaboradorDTO.class);

        // Altera o colaborador
        colaborador.setNome(colaborador.getNome() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaborador)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(colaborador.getCodigo()))
                .body("nome", Matchers.is(colaborador.getNome()));
    }

    @Test
    public void aoAlterarColaboradorInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        ColaboradorDTO colaboradorInexistente = obterColaboradorValido();
        colaboradorInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(colaboradorInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse colaborador"));
    }

    @Test
    public void aoExcluirColaborador_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        ColaboradorDTO colaborador = obterColaboradorValido();

        // Adiciona o colaborador para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(colaborador)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        colaborador = gson.fromJson(responsePost.getBody().asString(), ColaboradorDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", colaborador.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirColaboradorInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse colaborador"));
    }

    @Test
    public void aoBuscarColaboradorPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        ColaboradorDTO colaboradorExistente = new ColaboradorDTO();
        try {
            colaboradorExistente = obterColaboradorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter colaborador da api");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", colaboradorExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(colaboradorExistente.getCodigo()))
                .body("nome", Matchers.is(colaboradorExistente.getNome()))
                .body("RG", Matchers.is(colaboradorExistente.getRG()))
                .body("CPF", Matchers.is(colaboradorExistente.getCPF()))
                .body("telefone", Matchers.is(colaboradorExistente.getTelefone()));
    }

    @Test
    public void aoBuscarColaboradorPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        ColaboradorDTO colaboradorExistente = new ColaboradorDTO();
        try {
            colaboradorExistente = obterColaboradorValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter colaborador válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", colaboradorExistente.getNome())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/colaborador");

        Assertions.assertEquals(200, response.getStatusCode());
        ColaboradorDTO colaborador = gson.fromJson(response.getBody().asString(), ColaboradorDTO.class);

        Assertions.assertEquals(colaboradorExistente.getCodigo(), colaborador.getCodigo());
        Assertions.assertEquals(colaboradorExistente.getNome(), colaborador.getNome());
    }

    public static ColaboradorDTO obterColaboradorValido() {
        ColaboradorDTO colaborador = new ColaboradorDTO();
        colaborador.setNome("Colab" + Instant.now());
        colaborador.setRG("12345678");
        colaborador.setCPF("CPF" + Instant.now());
        colaborador.setTelefone("123456789");
        return colaborador;
    }

    public static ColaboradorDTO obterColaboradorValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/colaborador/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter colaborador válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), ColaboradorDTO.class);
        }
    }
}
