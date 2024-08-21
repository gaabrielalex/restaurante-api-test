package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.TipoPreparoDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.time.Instant;

public class TipoPreparoServicoTest {
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/tipo-preparo";
    }

    @Test
    public void aoAdicionarTipoPreparo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        TipoPreparoDTO tipoPreparo = obterTipoPreparoValido();

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparo)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("descricao", Matchers.is(tipoPreparo.getDescricao()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarTipoPreparoJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        TipoPreparoDTO tipoPreparoExistente = new TipoPreparoDTO();
        try {
            tipoPreparoExistente = obterTipoPreparoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter tipo de preparo da api");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparoExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse tipo de preparo"));
    }

    @Test
    public void aoAlterarTipoPreparo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        TipoPreparoDTO tipoPreparo = obterTipoPreparoValido();

        // Adiciona o tipo de preparo para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(tipoPreparo)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        tipoPreparo = gson.fromJson(responsePost.getBody().asString(), TipoPreparoDTO.class);

        // Altera o tipo de preparo
        tipoPreparo.setDescricao(tipoPreparo.getDescricao() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparo)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(tipoPreparo.getCodigo()))
                .body("descricao", Matchers.is(tipoPreparo.getDescricao()));
    }

    @Test
    public void aoAlterarTipoPreparoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        TipoPreparoDTO tipoPreparoInexistente = obterTipoPreparoValido();
        tipoPreparoInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(tipoPreparoInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse tipo de preparo"));
    }

    @Test
    public void aoExcluirTipoPreparo_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        TipoPreparoDTO tipoPreparo = obterTipoPreparoValido();

        // Adiciona o tipo de preparo para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(tipoPreparo)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        tipoPreparo = gson.fromJson(responsePost.getBody().asString(), TipoPreparoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", tipoPreparo.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirTipoPreparoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Tipo de Preparo nao existe"));
    }

    @Test
    public void aoBuscarTipoPreparoPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        TipoPreparoDTO tipoPreparoExistente = new TipoPreparoDTO();
        try {
            tipoPreparoExistente = obterTipoPreparoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter tipo de preparo da api");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", tipoPreparoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(tipoPreparoExistente.getCodigo()))
                .body("descricao", Matchers.is(tipoPreparoExistente.getDescricao()));
    }

    @Test
    public void aoBuscarTipoPreparoPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        TipoPreparoDTO tipoPreparoExistente = new TipoPreparoDTO();
        try {
            tipoPreparoExistente = obterTipoPreparoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter tipo preparo válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", tipoPreparoExistente.getDescricao())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/tipo-preparo");

        Assertions.assertEquals(200, response.getStatusCode());
        TipoPreparoDTO[] tiposPreparo = gson.fromJson(response.getBody().asString(), TipoPreparoDTO[].class);

        Assertions.assertTrue(tiposPreparo.length > 0, "Nenhum tipo de preparo encontrado com o nome especificado");
        Assertions.assertEquals(tipoPreparoExistente.getCodigo(), tiposPreparo[0].getCodigo());
        Assertions.assertEquals(tipoPreparoExistente.getDescricao(), tiposPreparo[0].getDescricao());
    }

    public static TipoPreparoDTO obterTipoPreparoValido() {
        TipoPreparoDTO tipoPreparo = new TipoPreparoDTO();
        tipoPreparo.setDescricao("TipoP" + Instant.now());
        return tipoPreparo;
    }

    public static TipoPreparoDTO obterTipoPreparoValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/tipo-preparo/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter produto válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), TipoPreparoDTO.class);
        }
    }

    public static TipoPreparoDTO CadastrarTipoPreparo() {
        Gson gson = new Gson();
        TipoPreparoDTO tipoPreparo = new TipoPreparoDTO();
        tipoPreparo.setDescricao("TipoP" + Instant.now());

        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(tipoPreparo)
            .when()
                .log().all()
            .post(ApiUtils.urlBase + "/tipo-preparo")       
            .then()
                .log().all()
                .extract().response();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        return gson.fromJson(responsePost.getBody().asString(), TipoPreparoDTO.class);
    }
}
