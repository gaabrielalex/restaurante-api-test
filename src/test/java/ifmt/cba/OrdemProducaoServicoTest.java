package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import ifmt.cba.dto.EstadoOrdemProducaoDTO;
import ifmt.cba.dto.ItemOrdemProducaoDTO;
import ifmt.cba.dto.OrdemProducaoDTO;
import ifmt.cba.dto.QuantidadeProduzidasProdutosDTO;
import ifmt.cba.utils.ApiUtils;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;

public class OrdemProducaoServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/ordem-producao";
    }

    @Test
    public void aoAdicionarOrdemProducao_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonOrdemProducao)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("dataProducao", Matchers.is(ordemProducao.getDataProducao().toString()))
                .body("cardapio", Matchers.notNullValue())
                .body("cardapio.codigo", Matchers.is(ordemProducao.getCardapio().getCodigo()))
                .body("estado", Matchers.is(ordemProducao.getEstado().toString()))
                .body("listaItens", Matchers.notNullValue())
                .body("listaItens", Matchers.hasSize(ordemProducao.getListaItens().size()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAlterarOrdemProducao_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        // Adiciona a ordem de produção para depois alterá-la
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        ordemProducao = gson.fromJson(responsePost.getBody().asString(), OrdemProducaoDTO.class);

        // Altera a ordem de produção
        try {
            ordemProducao.setCardapio(CardapioServicoTest.obterCardapioValidoDaApi(5));
            ordemProducao.setDataProducao(LocalDate.now().minusDays(1));
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cardápio válido da API");
        }

        String jsonOrdemProducaoAlterada = gson.toJson(ordemProducao);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonOrdemProducaoAlterada)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("dataProducao", Matchers.is(ordemProducao.getDataProducao().toString()))
                .body("cardapio", Matchers.notNullValue())
                .body("cardapio.codigo", Matchers.is(ordemProducao.getCardapio().getCodigo()))
                .body("estado", Matchers.is(ordemProducao.getEstado().toString()))
                .body("listaItens", Matchers.notNullValue())
                .body("listaItens", Matchers.hasSize(ordemProducao.getListaItens().size()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAlterarOrdemProducaoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespondente() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
        .when()
            .put()
        .then()
            .log().all()
            .statusCode(400)
            .body("erro", Matchers.is("Nao existe essa ordem de producao"));
    }

    @Test
    public void aoExcluirOrdemProducao_DeveRetornarRespostaComStatus204() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        // Adiciona a ordem de produção para depois excluí-la
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        ordemProducao = gson.fromJson(responsePost.getBody().asString(), OrdemProducaoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", ordemProducao.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirOrdemProducaoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespondente() {
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
                .body("erro", Matchers.is("Nao existe essa ordem de producao"));
    }

    @Test
    public void aoBuscarOrdemProducaoPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        OrdemProducaoDTO ordemProducaoExistente = new OrdemProducaoDTO();
        try {
            ordemProducaoExistente = obterOrdemProducaoValidaDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", ordemProducaoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}");

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        OrdemProducaoDTO ordemProducao = gson.fromJson(response.getBody().asString(), OrdemProducaoDTO.class);
        Assertions.assertEquals(ordemProducaoExistente.getCodigo(), ordemProducao.getCodigo());
        Assertions.assertEquals(ordemProducaoExistente.getDataProducao(), ordemProducao.getDataProducao());
        Assertions.assertEquals(ordemProducaoExistente.getCardapio().getCodigo(), ordemProducao.getCardapio().getCodigo());
        Assertions.assertEquals(ordemProducaoExistente.getEstado(), ordemProducao.getEstado());
        Assertions.assertEquals(ordemProducaoExistente.getListaItens().size(), ordemProducao.getListaItens().size());
        Assertions.assertEquals(ordemProducaoExistente.getLink(), ordemProducao.getLink());
    }

    @Test
    public void aoAlterarItemOrdemProducao_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        // Adiciona a ordem de produção para depois alterar um item
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        ordemProducao = gson.fromJson(responsePost.getBody().asString(), OrdemProducaoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducaoAlterada = ordemProducao.getListaItens().get(0);
        itemOrdemProducaoAlterada.setQuantidadePorcao(10);

        try {
            itemOrdemProducaoAlterada.setPreparoProduto(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(2));
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo de produto válido da API");
        }

        String jsonItemOrdemProducaoAlterado = gson.toJson(itemOrdemProducaoAlterada);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonItemOrdemProducaoAlterado)
            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/item")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("quantidadePorcao", Matchers.is(itemOrdemProducaoAlterada.getQuantidadePorcao()))
                .body("preparoProduto", Matchers.notNullValue())
                .body("preparoProduto.codigo", Matchers.is(itemOrdemProducaoAlterada.getPreparoProduto().getCodigo()));
    }

    @Test
    public void aoAlterarItemOrdemProducaoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespondente() {
        ItemOrdemProducaoDTO itemOrdemProducao = new ItemOrdemProducaoDTO();
        try {
            itemOrdemProducao = obterItemOrdemProducaoValido(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter item de ordem de produção válido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonItemOrdemProducao = gson.toJson(itemOrdemProducao);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonItemOrdemProducao)
            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/item")
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse item de ordem de producao"));
    }

    @Test
    public void aoExcluirItemOrdemProducao_DeveRetornarRespostaComStatus204() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        // Adiciona a ordem de produção para depois excluir um item
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
            .when()
            .log().all()
            .post()
            .then()
                .log().all()
                .extract().response();

        Assertions.assertEquals(200, responsePost.getStatusCode());
        ordemProducao = gson.fromJson(responsePost.getBody().asString(), OrdemProducaoDTO.class);

        ItemOrdemProducaoDTO itemOrdemProducao = ordemProducao.getListaItens().get(0);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", itemOrdemProducao.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/item/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirItemOrdemProducaoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespondente() {
        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", -1)
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/item/{codigo}")
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse item de ordem de producao"));
    }


    @Test
    public void aoProcessarOrdemProducao_DeveRetornarRespostaComStatus200() {
        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        try {
            ordemProducao = obterOrdemProducaoValida();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter ordem de produção válida");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonOrdemProducao = gson.toJson(ordemProducao);

        // Adiciona a ordem de produção para depois processá-la
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonOrdemProducao)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        ordemProducao = gson.fromJson(responsePost.getBody().asString(), OrdemProducaoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", ordemProducao.getCodigo())
            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}/processar-ordem")
            .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void aoPesquisarProdutosMaisProduzidos_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
            .when()
                .log().all()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/relatorio/produtos-mais-produzidos");

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());

        Type listType = new TypeToken<List<QuantidadeProduzidasProdutosDTO>>(){}.getType();
        List<QuantidadeProduzidasProdutosDTO> listaQuantidadeProduzidasProdutos = gson.fromJson(response.getBody().asString(), listType);
        Assertions.assertTrue(listaQuantidadeProduzidasProdutos.size() > 0);
        
        // Verifica se a lista está ordenada do maior para o menor
        int quantidadeAnterior = Integer.MAX_VALUE;
        for (QuantidadeProduzidasProdutosDTO dto : listaQuantidadeProduzidasProdutos) {
            Assertions.assertTrue(dto.getQuantidadeProduzida() <= quantidadeAnterior);
            quantidadeAnterior = dto.getQuantidadeProduzida();
        }
    }

    @Test
    public void aoPesquisarProdutosMenosProduzidos_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/relatorio/produtos-menos-produzidos");

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());

        Type listType = new TypeToken<List<QuantidadeProduzidasProdutosDTO>>(){}.getType();
        List<QuantidadeProduzidasProdutosDTO> listaQuantidadeProduzidasProdutos = gson.fromJson(response.getBody().asString(), listType);
        Assertions.assertTrue(listaQuantidadeProduzidasProdutos.size() > 0);
        
        // Verifica se a lista está ordenada do menor para o maior
        int quantidadeAnterior = Integer.MIN_VALUE;
        for (QuantidadeProduzidasProdutosDTO dto : listaQuantidadeProduzidasProdutos) {
            Assertions.assertTrue(dto.getQuantidadeProduzida() >= quantidadeAnterior);
            quantidadeAnterior = dto.getQuantidadeProduzida();
        }
    }

    public static OrdemProducaoDTO obterOrdemProducaoValida() throws Exception {
        List<ItemOrdemProducaoDTO> listaItens = new ArrayList<>();
        listaItens.add(obterItemOrdemProducaoValido(1));
        listaItens.add(obterItemOrdemProducaoValido(2));
        listaItens.add(obterItemOrdemProducaoValido(3));

        OrdemProducaoDTO ordemProducao = new OrdemProducaoDTO();
        ordemProducao.setDataProducao(LocalDate.now());
        ordemProducao.setCardapio(CardapioServicoTest.obterCardapioValidoDaApi(1));
        ordemProducao.setEstado(EstadoOrdemProducaoDTO.REGISTRADA);
        ordemProducao.setListaItens(listaItens);
        return ordemProducao;
    }

    public static OrdemProducaoDTO obterOrdemProducaoValidaDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/ordem-producao/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter ordem de produção válida da API");
        } else {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

            return gson.fromJson(response.getBody().asString(), OrdemProducaoDTO.class);
        }
    }

    public static ItemOrdemProducaoDTO obterItemOrdemProducaoValido(int idPreparoProduto) throws Exception {
        ItemOrdemProducaoDTO itemOrdemProducao = new ItemOrdemProducaoDTO();
        itemOrdemProducao.setPreparoProduto(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(idPreparoProduto));
        itemOrdemProducao.setQuantidadePorcao(5);
        return itemOrdemProducao;
    }
}
