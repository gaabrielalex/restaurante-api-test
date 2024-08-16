package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.GrupoAlimentarDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.time.Instant;

public class GrupoAlimentarServicoTest {
    
   @BeforeAll
    public static void setup() {
        // Configuração básica para o Rest Assured
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/grupo-alimentar";
    }

    @Test
    public void aoAdicionarGrupoAlimentar_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos()
    {
        GrupoAlimentarDTO grupoAlimentar = obterGrupoAlimentarValido();
        
        RestAssured
            .given()
                .contentType("application/json")
                .body(grupoAlimentar)
            .when()
                .request(Method.POST)
            .then()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(grupoAlimentar.getNome()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarGrupoAlimentarJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        GrupoAlimentarDTO grupoAlimentarExistente = new GrupoAlimentarDTO();
        try {
            grupoAlimentarExistente = obterGrupoAlimentarValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(grupoAlimentarExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse grupo alimentar"));
    }

    @Test
    public void aoAlterarGrupoAlimentar_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        Gson gson = new Gson();
        GrupoAlimentarDTO grupoAlimentar = new GrupoAlimentarDTO();
        try {
            grupoAlimentar = obterGrupoAlimentarValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido");
        }

        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(grupoAlimentar)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        grupoAlimentar = gson.fromJson(responsePost.getBody().asString(), GrupoAlimentarDTO.class);

        grupoAlimentar.setNome(grupoAlimentar.getNome() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(grupoAlimentar)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(grupoAlimentar.getCodigo()))
                .body("nome", Matchers.is(grupoAlimentar.getNome()));
    }

    @Test
    public void aoAlterarGrupoAlimentarInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        GrupoAlimentarDTO grupoAlimentarInexistente = new GrupoAlimentarDTO();
        try {
            grupoAlimentarInexistente = obterGrupoAlimentarValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido");
        }

        grupoAlimentarInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(grupoAlimentarInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse grupo alimentar"));
    }

    @Test
    public void aoExcluirGrupoAlimentar_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        GrupoAlimentarDTO grupoAlimentar = new GrupoAlimentarDTO();
        try {
            grupoAlimentar = obterGrupoAlimentarValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido");
        }

        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(grupoAlimentar)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        grupoAlimentar = gson.fromJson(responsePost.getBody().asString(), GrupoAlimentarDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", grupoAlimentar.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test    public void aoExcluirGrupoAlimentarInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Esse GrupoAlimentar nao existe"));
    }

    @Test
    public void aoBuscarGrupoAlimentarPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        GrupoAlimentarDTO grupoAlimentarExistente = new GrupoAlimentarDTO();
        try {
            grupoAlimentarExistente = obterGrupoAlimentarValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", grupoAlimentarExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(grupoAlimentarExistente.getCodigo()))
                .body("nome", Matchers.is(grupoAlimentarExistente.getNome()));
    }

    @Test
    public void aoBuscarGrupoAlimentarPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRepostaCorretos() {
        Gson gson = new Gson();
        GrupoAlimentarDTO grupoAlimentarExistente = new GrupoAlimentarDTO();
        try {
            grupoAlimentarExistente = obterGrupoAlimentarValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter grupo alimentar válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", grupoAlimentarExistente.getNome())
            .when()
                .log().all()
                .get();

        Assertions.assertEquals(200, response.getStatusCode());
        GrupoAlimentarDTO[] grupos = gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO[].class);

        Assertions.assertEquals(grupoAlimentarExistente.getCodigo(), grupos[0].getCodigo());
        Assertions.assertEquals(grupoAlimentarExistente.getNome(), grupos[0].getNome());
    }
    
    public static GrupoAlimentarDTO obterGrupoAlimentarValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/grupo-alimentar/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter grupo alimentar válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), GrupoAlimentarDTO.class);
        }
    }

    public static GrupoAlimentarDTO obterGrupoAlimentarValido() {
        GrupoAlimentarDTO grupoAlimentar = new GrupoAlimentarDTO();
        grupoAlimentar.setNome("Graos" + Instant.now());

        return grupoAlimentar;
    }
}
