package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.EntregadorDTO;
import ifmt.cba.dto.EstadoPedidoDTO;
import ifmt.cba.dto.ItemPedidoDTO;
import ifmt.cba.dto.PedidoDTO;
import ifmt.cba.utils.ApiUtils;
import ifmt.cba.utils.LocalDateAdapter;
import ifmt.cba.utils.LocalTimeAdapter;
import ifmt.cba.utils.LocalTimeAdapter2;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class PedidoServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/pedido";
    }

    @Test
    public void aoAdicionarPedido_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonPedido)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("cliente.codigo", Matchers.is(pedido.getCliente().getCodigo()))
                .body("dataPedido", Matchers.is(pedido.getDataPedido().toString()))
                .body("horaPedido", Matchers.is(pedido.getHoraPedido().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaProducao", Matchers.is(pedido.getHoraProducao().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaPronto", Matchers.is(pedido.getHoraPronto().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaEntrega", Matchers.is(pedido.getHoraEntrega().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaFinalizado", Matchers.is(pedido.getHoraFinalizado().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("estado", Matchers.is(pedido.getEstado().toString()))
                .body("entregador.codigo", Matchers.is(pedido.getEntregador().getCodigo()))
                .body("listaItens", Matchers.hasSize(pedido.getListaItens().size()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAlterarPedido_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        // Adiciona o pedido para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonPedido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        pedido = gson.fromJson(responsePost.getBody().asString(), PedidoDTO.class);

        EntregadorDTO novoEntregador = new EntregadorDTO();
        try {
            novoEntregador = EntregadorServicoTest.obterEntregadorValidoDaApi(2);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter entregador válido da API");
        }

        // Altera o pedido
        pedido.setHoraPedido(pedido.getHoraPedido().plusMinutes(2));
        pedido.setHoraProducao(pedido.getHoraProducao().plusMinutes(2));
        pedido.setHoraPronto(pedido.getHoraPronto().plusMinutes(2));
        pedido.setHoraEntrega(pedido.getHoraEntrega().plusMinutes(2));
        pedido.setHoraFinalizado(pedido.getHoraFinalizado().plusMinutes(2));
        pedido.setEstado(EstadoPedidoDTO.PRONTO);
        pedido.setEntregador(novoEntregador);

        String jsonPedidoAlterado = gson.toJson(pedido);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonPedidoAlterado)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(pedido.getCodigo()))
                .body("cliente.codigo", Matchers.is(pedido.getCliente().getCodigo()))
                .body("dataPedido", Matchers.is(pedido.getDataPedido().toString()))
                .body("horaPedido", Matchers.is(pedido.getHoraPedido().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaProducao", Matchers.is(pedido.getHoraProducao().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaPronto", Matchers.is(pedido.getHoraPronto().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaEntrega", Matchers.is(pedido.getHoraEntrega().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("horaFinalizado", Matchers.is(pedido.getHoraFinalizado().truncatedTo(ChronoUnit.MILLIS).toString()))
                .body("estado", Matchers.is(pedido.getEstado().toString()))
                .body("entregador.codigo", Matchers.is(pedido.getEntregador().getCodigo()))
                .body("listaItens", Matchers.hasSize(pedido.getListaItens().size()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAlterarPedidoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        PedidoDTO pedidoInexistente = new PedidoDTO();
        try {
            pedidoInexistente = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        pedidoInexistente.setCodigo(-1);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedidoInexistente);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonPedido)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse pedido"));
    }

    @Test
    public void aoExcluirPedido_DeveRetornarRespostaComStatus204() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        // Adiciona o pedido para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonPedido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        pedido = gson.fromJson(responsePost.getBody().asString(), PedidoDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", pedido.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirPedidoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse pedido"));
    }

    @Test
    public void aoBuscarPedidoPorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PedidoDTO pedidoExistente = new PedidoDTO();
        try {
            pedidoExistente = obterPedidoValidoDaApi(12);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", pedidoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}");
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        PedidoDTO pedido = gson.fromJson(response.getBody().asString(), PedidoDTO.class);
        Assertions.assertEquals(pedidoExistente.getCodigo(), pedido.getCodigo());
        Assertions.assertEquals(pedidoExistente.getCliente().getCodigo(), pedido.getCliente().getCodigo());
        Assertions.assertEquals(pedidoExistente.getDataPedido(), pedido.getDataPedido());
        Assertions.assertEquals(pedidoExistente.getHoraPedido(), pedido.getHoraPedido());
        Assertions.assertEquals(pedidoExistente.getHoraProducao(), pedido.getHoraProducao());
        Assertions.assertEquals(pedidoExistente.getHoraPronto(), pedido.getHoraPronto());
        Assertions.assertEquals(pedidoExistente.getHoraEntrega(), pedido.getHoraEntrega());
        Assertions.assertEquals(pedidoExistente.getHoraFinalizado(), pedido.getHoraFinalizado());
        Assertions.assertEquals(pedidoExistente.getEstado(), pedido.getEstado());
        Assertions.assertEquals(pedidoExistente.getEntregador().getCodigo(), pedido.getEntregador().getCodigo());
        Assertions.assertEquals(pedidoExistente.getListaItens().size(), pedido.getListaItens().size());
    }

    @Test
    public void aoAlterarItemPedido_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        // Adiciona o pedido para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonPedido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        pedido = gson.fromJson(responsePost.getBody().asString(), PedidoDTO.class);

        ItemPedidoDTO itemPedidoAlterado = pedido.getListaItens().get(0);
        itemPedidoAlterado.setQuantidadePorcao(5);
        try {
            itemPedidoAlterado.setPreparoProduto(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(3));
        } catch (Exception e) {
            Assertions.fail("Erro ao obter preparo de produto valido da API");
        }
        
        String jsonItemPedidoAlterado = gson.toJson(itemPedidoAlterado);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonItemPedidoAlterado)
            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/item")
            .then()
                .log().all()
                .statusCode(200)
                .body("preparoProduto.codigo", Matchers.is(itemPedidoAlterado.getPreparoProduto().getCodigo()))
                .body("preparoProduto.nome", Matchers.is(itemPedidoAlterado.getPreparoProduto().getNome()))
                .body("quantidadePorcao", Matchers.is(itemPedidoAlterado.getQuantidadePorcao()));
    }

    @Test
    public void aoAlterarItemPedidoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        ItemPedidoDTO itemPedidoInexistente = new ItemPedidoDTO();
        try {
            itemPedidoInexistente = obterItemPedidoValido(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter item de pedido valido");
        }

        itemPedidoInexistente.setCodigo(-1);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonItemPedido = gson.toJson(itemPedidoInexistente);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(jsonItemPedido)
            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/item")
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse item de pedido"));
    }

    @Test
    public void aoExcluirItemPedido_DeveRetornarRespostaComStatus204() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        // Adiciona o pedido para depois excluir um item de pedido
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonPedido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        pedido = gson.fromJson(responsePost.getBody().asString(), PedidoDTO.class);

        ItemPedidoDTO itemPedido = pedido.getListaItens().get(0);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", itemPedido.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/item/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirItemPedidoInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse item de pedido"));
    }

    @Test
    public void aoAlterarEstadoDoPedidoParaTodosOsEstadosPossiveis_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        String jsonPedido = gson.toJson(pedido);

        // Adiciona o pedido para depois alterar o estado
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(jsonPedido)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        pedido = gson.fromJson(responsePost.getBody().asString(), PedidoDTO.class);

        for (EstadoPedidoDTO estado : EstadoPedidoDTO.values()) {
            if(estado == EstadoPedidoDTO.REGISTRADO) continue;

            String stringEstado = estado.toString().toLowerCase();

            RestAssured
                .given()
                    .log().all()
                    .contentType("application/json")

                .when()
                    .put(ApiUtils.urlBase + RestAssured.basePath + "/" + pedido.getCodigo() + "/alterar-estado/" + stringEstado)
                .then()
                    .log().all()
                    .statusCode(200)
                    .body("codigo", Matchers.is(pedido.getCodigo()))
                    .body("cliente.codigo", Matchers.is(pedido.getCliente().getCodigo()))
                    .body("dataPedido", Matchers.is(pedido.getDataPedido().toString()))
                    .body("horaPedido", Matchers.is(pedido.getHoraPedido().truncatedTo(ChronoUnit.MILLIS).toString()))
                    .body("horaProducao", Matchers.is(pedido.getHoraProducao().truncatedTo(ChronoUnit.MILLIS).toString()))
                    .body("horaPronto", Matchers.is(pedido.getHoraPronto().truncatedTo(ChronoUnit.MILLIS).toString()))
                    .body("horaEntrega", Matchers.is(pedido.getHoraEntrega().truncatedTo(ChronoUnit.MILLIS).toString()))
                    .body("horaFinalizado", Matchers.is(pedido.getHoraFinalizado().truncatedTo(ChronoUnit.MILLIS).toString()))
                    .body("estado", Matchers.is(estado.toString()))
                    .body("entregador.codigo", Matchers.is(pedido.getEntregador().getCodigo()))
                    .body("listaItens", Matchers.hasSize(pedido.getListaItens().size()))
                    .body("link", Matchers.notNullValue());
        }
    }

    @Test
    public void aoAlterarEstadoDoPedidoParaEstadoInvalido_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        
        RestAssured
            .given()
                .log().all()
                .contentType("application/json")

            .when()
                .put(ApiUtils.urlBase + RestAssured.basePath + "/1/alterar-estado/estadoInvalido")
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Estado invalido!"));
    }

    @Test
    public void aoPesquisarPedidosPelaDataAtualEPorEstadoRegistrado_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("data", LocalDate.now().toString())
                .queryParam("estado", EstadoPedidoDTO.REGISTRADO.toString().toLowerCase())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath)
            .then()
                .log().all()
                .extract().response();
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter2())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        PedidoDTO[] pedidos = gson.fromJson(response.getBody().asString(), PedidoDTO[].class);
        for (PedidoDTO pedido : pedidos) {
            Assertions.assertEquals(EstadoPedidoDTO.REGISTRADO, pedido.getEstado());
            Assertions.assertEquals(LocalDate.now(), pedido.getDataPedido());
        }

        //Verifica se está em ordem cronológica
        for (int i = 0; i < pedidos.length - 1; i++) {
            Assertions.assertTrue(pedidos[i].getHoraPedido().isBefore(pedidos[i + 1].getHoraPedido()), 
            "Pedido " + pedidos[i].getCodigo() + " não está em ordem cronológica. O horário do pedido é " + pedidos[i].getHoraPedido() + " e o horário do próximo pedido é " + pedidos[i + 1].getHoraPedido());
        }
    }

    @Test
    public void aoPesquisarPedidosPelaDataAtualEPorEstadoPronto_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("data", LocalDate.now().toString())
                .queryParam("estado", EstadoPedidoDTO.PRONTO.toString().toLowerCase())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath);
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter2())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        PedidoDTO[] pedidos = gson.fromJson(response.getBody().asString(), PedidoDTO[].class);
        for (PedidoDTO pedido : pedidos) {
            Assertions.assertEquals(EstadoPedidoDTO.PRONTO, pedido.getEstado());
            Assertions.assertEquals(LocalDate.now(), pedido.getDataPedido());
        }

        //Verifica se está em ordem cronológica
        for (int i = 0; i < pedidos.length - 1; i++) {
            Assertions.assertTrue(pedidos[i].getHoraPedido().isBefore(pedidos[i + 1].getHoraPedido()));
        }
    }

    @Test
    public void aoPesquisarTotalizacaoDePedidosProduzidosNoPeriodo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("dataInicial", LocalDate.now().minusDays(1).toString())
                .queryParam("dataFinal", LocalDate.now().toString())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/relatorio/totalizacao-pedido-produzidos-periodo");
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter2())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        double totalizacao = gson.fromJson(response.getBody().asString(), Double.class);
        Assertions.assertTrue(totalizacao > 0);
    }

    @Test
    public void aoPesquisarMediaTempoEntreProntoEConcluido_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        List<PedidoDTO> listaPedidoDTO = pesquisarPedidosPorEstado(EstadoPedidoDTO.CONCLUIDO);

        double mediaTempoCalculado = listaPedidoDTO.stream()
                .filter(pedido -> pedido.getHoraPronto() != null && pedido.getHoraFinalizado() != null)
                .mapToLong(
                        pedido -> Duration.between(pedido.getHoraPronto(), pedido.getHoraFinalizado()).toMinutes())
                .average()
                .orElse(0);

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/relatorio/media-tempo-pronto-concluido");
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter2())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        double mediaTempoObtido = gson.fromJson(response.getBody().asString(), Double.class);
        Assertions.assertTrue(mediaTempoObtido >= 0);
        Assertions.assertEquals(mediaTempoCalculado, mediaTempoObtido, 0.01);
    }

    @Test
    public void aoPesquisarMediaTempoRegistradoEPronto_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        List<PedidoDTO> listaPedidoDTO =  pesquisarPedidosPorEstado(EstadoPedidoDTO.PRONTO);
        listaPedidoDTO.addAll(pesquisarPedidosPorEstado(EstadoPedidoDTO.ENTREGA));
        listaPedidoDTO.addAll(pesquisarPedidosPorEstado(EstadoPedidoDTO.CONCLUIDO));

        double mediaTempoCalculado = listaPedidoDTO.stream()
                .filter(pedido -> pedido.getHoraPedido() != null && pedido.getHoraPronto() != null)
                .mapToLong(pedido -> Duration.between(pedido.getHoraPedido(), pedido.getHoraPronto()).toMinutes())
                .average()
                .orElse(0);

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/relatorio/media-tempo-registrado-pronto");
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();

        Assertions.assertEquals(200, response.getStatusCode());
        double mediaTempoObtido = gson.fromJson(response.getBody().asString(), Double.class);
        Assertions.assertTrue(mediaTempoObtido >= 0);
        Assertions.assertEquals(mediaTempoCalculado, mediaTempoObtido, 0.01);
    }

    public static List<PedidoDTO> pesquisarPedidosPorEstado(EstadoPedidoDTO estado) {
        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("estado", estado.toString().toLowerCase())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/buscar-por-estado/{estado}");
            
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
        .create();
    
        Assertions.assertEquals(200, response.getStatusCode());
        PedidoDTO[] pedidos = gson.fromJson(response.getBody().asString(), PedidoDTO[].class);
        return new ArrayList<>(Arrays.asList(pedidos));  // Garante uma lista mutável
    }    

    public static PedidoDTO obterPedidoValido() throws Exception {
        List<ItemPedidoDTO> listaItens = new ArrayList<>();
        listaItens.add(obterItemPedidoValido(1));
        listaItens.add(obterItemPedidoValido(2));
        listaItens.add(obterItemPedidoValido(3));

        PedidoDTO pedido = new PedidoDTO();
        pedido.setCliente(ClienteServicoTest.obterClienteValidoDaApi(1));
        pedido.setDataPedido(LocalDate.now());
        pedido.setHoraPedido(LocalTime.now());
        pedido.setHoraProducao(LocalTime.now().plusMinutes(5));
        pedido.setHoraPronto(LocalTime.now().plusMinutes(10));
        pedido.setHoraEntrega(LocalTime.now().plusMinutes(15));
        pedido.setHoraFinalizado(LocalTime.now().plusMinutes(20));
        pedido.setEstado(EstadoPedidoDTO.REGISTRADO);
        pedido.setEntregador(EntregadorServicoTest.obterEntregadorValidoDaApi(1));
        pedido.setListaItens(listaItens);
        return pedido;
    }

    public static PedidoDTO obterPedidoValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/pedido/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter pedido válido");
        } else {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

            return gson.fromJson(response.getBody().asString(), PedidoDTO.class);
        }
    }

    public static ItemPedidoDTO obterItemPedidoValido(int preparoProdutoId) throws Exception {
        ItemPedidoDTO itemPedido = new ItemPedidoDTO();
        itemPedido.setPreparoProduto(PreparoProdutoServicoTest.obterPreparoProdutoValidoDaApi(preparoProdutoId));
        itemPedido.setQuantidadePorcao(3);
        return itemPedido;
    }
}
