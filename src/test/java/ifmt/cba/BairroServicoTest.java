package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.BairroDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.time.Instant;

public class BairroServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/bairro";
    }

    @Test
    public void aoAdicionarBairro_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        BairroDTO bairro = obterBairroValido();

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairro)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(bairro.getNome()))
                .body("custoEntrega", Matchers.equalTo((float) bairro.getCustoEntrega()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarBairroJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        BairroDTO bairroExistente = new BairroDTO();
        try {
            bairroExistente = obterBairroValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter bairro da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairroExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse bairro"));
    }

    @Test
    public void aoAlterarBairro_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        BairroDTO bairro = obterBairroValido();

        // Adiciona o bairro para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(bairro)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        bairro = gson.fromJson(responsePost.getBody().asString(), BairroDTO.class);

        // Altera o bairro
        bairro.setNome(bairro.getNome() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairro)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(bairro.getCodigo()))
                .body("nome", Matchers.is(bairro.getNome()));
    }

    @Test
    public void aoAlterarBairroInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        BairroDTO bairroInexistente = obterBairroValido();
        bairroInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(bairroInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse bairro"));
    }

    @Test
    public void aoExcluirBairro_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        BairroDTO bairro = obterBairroValido();

        // Adiciona o bairro para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(bairro)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        bairro = gson.fromJson(responsePost.getBody().asString(), BairroDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", bairro.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirBairroInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse bairro"));
    }

    @Test
    public void aoBuscarBairroPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        BairroDTO bairroExistente = new BairroDTO();
        try {
            bairroExistente = obterBairroValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter bairro da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", bairroExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(bairroExistente.getCodigo()))
                .body("nome", Matchers.is(bairroExistente.getNome()))
                .body("custoEntrega", Matchers.equalTo((float) bairroExistente.getCustoEntrega()))
                .body("link", Matchers.is(bairroExistente.getLink()));
    }

    @Test
    public void aoBuscarBairroPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        BairroDTO bairroExistente = new BairroDTO();
        try {
            bairroExistente = obterBairroValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter bairro válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", bairroExistente.getNome())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/bairro");

        Assertions.assertEquals(200, response.getStatusCode());
        BairroDTO bairro = gson.fromJson(response.getBody().asString(), BairroDTO.class);

        Assertions.assertEquals(bairroExistente.getCodigo(), bairro.getCodigo());
        Assertions.assertEquals(bairroExistente.getNome(), bairro.getNome());
    }

    public static BairroDTO obterBairroValido() {
        BairroDTO bairro = new BairroDTO();
        bairro.setNome("Bairro" + Instant.now());
        bairro.setCustoEntrega(15.5f);
        bairro.setLink("http://exemplo.com/bairro");
        return bairro;
    }

    public static BairroDTO obterBairroValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/bairro/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter bairro válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), BairroDTO.class);
        }
    }
}
