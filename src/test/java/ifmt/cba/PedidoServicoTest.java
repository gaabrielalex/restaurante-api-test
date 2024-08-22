package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ifmt.cba.dto.EstadoPedidoDTO;
import ifmt.cba.dto.ItemPedidoDTO;
import ifmt.cba.dto.PedidoDTO;
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
                .statusCode(200);

                //Não há nenhuma maneira de obter o pedidoDTO após a inserção, então não é possível testar os dados de resposta
                // .body("codigo", Matchers.notNullValue())
                // .body("codigo", Matchers.greaterThan(0))
                // .body("cliente.codigo", Matchers.is(pedido.getCliente().getCodigo()))
                // .body("dataPedido", Matchers.is(pedido.getDataPedido().toString()))
                // .body("horaPedido", Matchers.is(pedido.getHoraPedido().toString()))
                // .body("horaProducao", Matchers.is(pedido.getHoraProducao().toString()))
                // .body("estado", Matchers.is(pedido.getEstado()))
                // .body("entregador.codigo", Matchers.is(pedido.getEntregador().getCodigo()))
                // .body("listaItens", Matchers.hasSize(pedido.getListaItens().size()))
                // .body("link", Matchers.notNullValue());
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

        // Altera o pedido
        pedido.setHoraEntrega(pedido.getHoraEntrega().plusMinutes(10));

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(pedido)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(pedido.getCodigo()))
                .body("horaEntrega", Matchers.is(pedido.getHoraEntrega().toString()));
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

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(pedidoInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse pedido"));
    }

    @Test
    public void aoExcluirPedido_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        PedidoDTO pedido = new PedidoDTO();
        try {
            pedido = obterPedidoValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido valido");
        }

        // Adiciona o pedido para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(pedido)
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
            pedidoExistente = obterPedidoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", pedidoExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(pedidoExistente.getCodigo()))
                .body("cliente.codigo", Matchers.is(pedidoExistente.getCliente().getCodigo()))
                .body("dataPedido", Matchers.is(pedidoExistente.getDataPedido().toString()))
                .body("horaPedido", Matchers.is(pedidoExistente.getHoraPedido().toString()))
                .body("horaProducao", Matchers.is(pedidoExistente.getHoraProducao().toString()));
    }

    @Test
    public void aoBuscarPedidoPorNomeCliente_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        PedidoDTO pedidoExistente = new PedidoDTO();
        try {
            pedidoExistente = obterPedidoValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter pedido válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nomeCliente", pedidoExistente.getCliente().getNome())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/pedido");

        Assertions.assertEquals(200, response.getStatusCode());
        PedidoDTO pedido = gson.fromJson(response.getBody().asString(), PedidoDTO.class);

        Assertions.assertEquals(pedidoExistente.getCodigo(), pedido.getCodigo());
        Assertions.assertEquals(pedidoExistente.getCliente().getNome(), pedido.getCliente().getNome());
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
