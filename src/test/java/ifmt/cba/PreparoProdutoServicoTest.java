package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.dto.ProdutoDTO;
import ifmt.cba.dto.TipoPreparoDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class PreparoProdutoServicoTest {
    
    @BeforeAll
    public static void setup() {
        // Configuração básica para o Rest Assured
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/preparo-produto";
    }

    @Test
    public void aoAdicionarPreparoProduto_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PreparoProdutoDTO preparoProduto = new PreparoProdutoDTO();
        try {
            preparoProduto = obterPreparoProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido");
        }
        
        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProduto)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(preparoProduto.getNome()))
                .body("tempoPreparo", Matchers.is(preparoProduto.getTempoPreparo()))
                .body("valorPreparo", Matchers.is(preparoProduto.getValorPreparo()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarPreparoProdutoJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        PreparoProdutoDTO preparoProdutoExistente = new PreparoProdutoDTO();
        try {
            preparoProdutoExistente = obterPreparoProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse preparo de produto"));
    }

    @Test
    public void aoAlterarPreparoProduto_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        PreparoProdutoDTO preparoProduto = new PreparoProdutoDTO();
        try {
            preparoProduto = obterPreparoProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido");
        }

        // Adiciona o preparo produto para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(preparoProduto)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        preparoProduto = gson.fromJson(responsePost.getBody().asString(), PreparoProdutoDTO.class);

        // Altera o preparo produto
        preparoProduto.setNome(preparoProduto.getNome() + "-Alt");
        preparoProduto.setTempoPreparo(preparoProduto.getTempoPreparo() + 10);
        preparoProduto.setValorPreparo(preparoProduto.getValorPreparo() + 5.0f);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProduto)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(preparoProduto.getCodigo()))
                .body("nome", Matchers.is(preparoProduto.getNome()))
                .body("tempoPreparo", Matchers.is(preparoProduto.getTempoPreparo()))
                .body("valorPreparo", Matchers.is(preparoProduto.getValorPreparo()));
    }

    @Test
    public void aoAlterarPreparoProdutoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        PreparoProdutoDTO preparoProdutoInexistente = new PreparoProdutoDTO();
        try {
            preparoProdutoInexistente = obterPreparoProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido");
        }

        // Define um código inexistente
        preparoProdutoInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(preparoProdutoInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse preparo de produto"));
    }

    @Test
    public void aoExcluirPreparoProduto_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        PreparoProdutoDTO preparoProduto = new PreparoProdutoDTO();
        try {
            preparoProduto = obterPreparoProdutoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido");
        }

        // Adiciona o preparo produto para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(preparoProduto)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        preparoProduto = gson.fromJson(responsePost.getBody().asString(), PreparoProdutoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", preparoProduto.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirPreparoProdutoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse preparo de produto"));
    }

    @Test
    public void aoBuscarPreparoProdutoPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PreparoProdutoDTO preparoProdutoExistente = new PreparoProdutoDTO();
        try {
            preparoProdutoExistente = obterPreparoProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", preparoProdutoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(preparoProdutoExistente.getCodigo()))
                .body("nome", Matchers.is(preparoProdutoExistente.getNome()))
                .body("tempoPreparo", Matchers.is(preparoProdutoExistente.getTempoPreparo()))
                .body("valorPreparo", Matchers.is(preparoProdutoExistente.getValorPreparo()));
    }

    @Test
    public void aoBuscarPreparoProdutoPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        PreparoProdutoDTO preparoProdutoExistente = new PreparoProdutoDTO();
        try {
            preparoProdutoExistente = obterPreparoProdutoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo produto válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", preparoProdutoExistente.getNome())
            .when()
                .log().all()
                .get();

        Assertions.assertEquals(200, response.getStatusCode());
        PreparoProdutoDTO[] preparos = gson.fromJson(response.getBody().asString(), PreparoProdutoDTO[].class);

        Assertions.assertEquals(preparoProdutoExistente.getCodigo(), preparos[0].getCodigo());
        Assertions.assertEquals(preparoProdutoExistente.getNome(), preparos[0].getNome());
    }

    @Test
    public void AoBuscarPreparoProdutoPorProduto_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        List<ProdutoDTO> listaProdutoExistentes = new ArrayList<>();
        try {
            listaProdutoExistentes.add(ProdutoServicoTest.obterProdutoValidoDaApi(1));
            listaProdutoExistentes.add(ProdutoServicoTest.obterProdutoValidoDaApi(2));
            listaProdutoExistentes.add(ProdutoServicoTest.obterProdutoValidoDaApi(3));
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparos produto válidos da API");
        }

        for (ProdutoDTO produtoExistente : listaProdutoExistentes) {
            Response response = RestAssured
                .given()
                    .log().all()
                    .contentType("application/json")
                    .pathParam("codigo", produtoExistente.getCodigo())
                .when()
                    .log().all()
                    .get(ApiUtils.urlBase + RestAssured.basePath + "/produto/{codigo}");


            Assertions.assertEquals(200, response.getStatusCode());
            PreparoProdutoDTO[] preparosObtidos = gson.fromJson(response.getBody().asString(), PreparoProdutoDTO[].class);
            for (PreparoProdutoDTO preparoObtido : preparosObtidos) {
                Assertions.assertEquals(produtoExistente.getCodigo(), preparoObtido.getProduto().getCodigo());
                Assertions.assertEquals(produtoExistente.getNome(), preparoObtido.getProduto().getNome());
            }
        }
    }

    public static PreparoProdutoDTO obterPreparoProdutoValido() throws Exception {
        PreparoProdutoDTO preparoProduto = new PreparoProdutoDTO();
        preparoProduto.setNome("Prp" + Instant.now());
        preparoProduto.setTempoPreparo(30);
        preparoProduto.setValorPreparo(20.0f);

        preparoProduto.setProduto(ProdutoServicoTest.obterProdutoValidoDaApi(1));
        preparoProduto.setTipoPreparo(CadastrarTipoPreparo());

        return preparoProduto;
    }

    public static PreparoProdutoDTO obterPreparoProdutoValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/preparo-produto/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter preparo produto válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), PreparoProdutoDTO.class);
        }
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
