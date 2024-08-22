package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.CardapioDTO;
import ifmt.cba.dto.PreparoProdutoDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class CardapioServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/cardapio";
    }

    @Test
    public void aoAdicionarCardapio_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        CardapioDTO cardapio = new CardapioDTO();
        try {
            cardapio = obterCardapioValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da válido");
        }

        RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body(cardapio)
                .when()
                .log().all()
                .request(Method.POST)
                .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(cardapio.getNome()))
                .body("descricao", Matchers.is(cardapio.getDescricao()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarCardapioJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        CardapioDTO cardapioExistente = new CardapioDTO();
        try {
            cardapioExistente = obterCardapioValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da API");
        }

        RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body(cardapioExistente)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse cardapio"));
    }

    @Test
    public void aoAlterarCardapio_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        CardapioDTO cardapio = new CardapioDTO();
        try {
            cardapio = obterCardapioValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da válido");
        }

        // Adiciona o cardápio para depois alterá-lo
        Response responsePost = RestAssured.given()
                .log().all()
                .contentType("application/json")
                .body(cardapio)
                .when()
                .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        cardapio = gson.fromJson(responsePost.getBody().asString(), CardapioDTO.class);

        // Altera o cardápio
        cardapio.setNome(cardapio.getNome() + "-Alt");

        RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body(cardapio)
                .when()
                .put()
                .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(cardapio.getCodigo()))
                .body("nome", Matchers.is(cardapio.getNome()));
    }

    @Test
    public void aoAlterarCardapioInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        CardapioDTO cardapioInexistente = new CardapioDTO();
        try {
            cardapioInexistente = obterCardapioValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da válido");
        }
        cardapioInexistente.setCodigo(-1);

        RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body(cardapioInexistente)
                .when()
                .put()
                .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse cardapio"));
    }

    @Test
    public void aoExcluirCardapio_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        CardapioDTO cardapio = new CardapioDTO();
        try {
            cardapio = obterCardapioValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da válido");
        }

        // Adiciona o cardápio para depois excluí-lo
        Response responsePost = RestAssured.given()
                .log().all()
                .contentType("application/json")
                .body(cardapio)
                .when()
                .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        cardapio = gson.fromJson(responsePost.getBody().asString(), CardapioDTO.class);

        RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", cardapio.getCodigo())
                .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirCardapioInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse cardapio"));
    }

    @Test
    public void aoBuscarCardapioPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        CardapioDTO cardapioExistente = new CardapioDTO();
        try {
            cardapioExistente = obterCardapioValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio da API");
        }

        Response response = RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", cardapioExistente.getCodigo())
                .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}");

        Assertions.assertEquals(200, response.getStatusCode());
        CardapioDTO cardapio = gson.fromJson(response.getBody().asString(), CardapioDTO.class);

        Assertions.assertEquals(cardapioExistente.getCodigo(), cardapio.getCodigo());
        Assertions.assertEquals(cardapioExistente.getNome(), cardapio.getNome());
        Assertions.assertEquals(cardapioExistente.getDescricao(), cardapio.getDescricao());
        Assertions.assertEquals(cardapioExistente.getLink(), cardapio.getLink());
        Assertions.assertEquals(cardapioExistente.getListaPreparoProduto().size(),
                cardapio.getListaPreparoProduto().size());
    }

    @Test
    public void aoBuscarCardapioPorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        CardapioDTO cardapioExistente = new CardapioDTO();
        try {
            cardapioExistente = obterCardapioValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio válido da API");
        }

        Response response = RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", cardapioExistente.getNome())
                .when()
                .log().all()
                .get(ApiUtils.urlBase + "/cardapio");

        Assertions.assertEquals(200, response.getStatusCode());
        CardapioDTO[] cardapios = gson.fromJson(response.getBody().asString(), CardapioDTO[].class);
        CardapioDTO cardapio = cardapios[0];

        Assertions.assertEquals(cardapioExistente.getCodigo(), cardapio.getCodigo());
        Assertions.assertEquals(cardapioExistente.getNome(), cardapio.getNome());
        Assertions.assertEquals(cardapioExistente.getDescricao(), cardapio.getDescricao());
        Assertions.assertEquals(cardapioExistente.getLink(), cardapio.getLink());
        Assertions.assertEquals(cardapioExistente.getListaPreparoProduto().size(),
                cardapio.getListaPreparoProduto().size());
    }

    public static CardapioDTO obterCardapioValido() throws Exception {
        List<PreparoProdutoDTO> listaProdutoExistentes = new ArrayList<PreparoProdutoDTO>();
        listaProdutoExistentes.add(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(1));
        listaProdutoExistentes.add(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(1));
        listaProdutoExistentes.add(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(1));
        listaProdutoExistentes.add(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(1));

        CardapioDTO cardapio = new CardapioDTO();
        cardapio.setNome("Cardápio" + Instant.now());
        cardapio.setDescricao("Descrição do cardápio");
        cardapio.setListaPreparoProduto(listaProdutoExistentes);
        cardapio.setLink("http://exemplo.com/cardapio");
        return cardapio;
    }

    public static CardapioDTO obterCardapioValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/cardapio/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter cardápio válido");
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), CardapioDTO.class);
        }
    }
}
