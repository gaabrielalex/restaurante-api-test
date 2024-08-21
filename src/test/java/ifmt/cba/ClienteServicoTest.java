package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import ifmt.cba.dto.ClienteDTO;
import ifmt.cba.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.time.Instant;

public class ClienteServicoTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ApiUtils.urlBase;
        RestAssured.basePath = "/cliente";
    }

    @Test
    public void aoAdicionarCliente_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        ClienteDTO cliente = new ClienteDTO();
        try {
            cliente = obterClienteValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }
        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(cliente)
            .when()
                .log().all()
                .request(Method.POST)
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.notNullValue())
                .body("codigo", Matchers.greaterThan(0))
                .body("nome", Matchers.is(cliente.getNome()))
                .body("RG", Matchers.is(cliente.getRG()))
                .body("CPF", Matchers.is(cliente.getCPF()))
                .body("telefone", Matchers.is(cliente.getTelefone()))
                .body("logradouro", Matchers.is(cliente.getLogradouro()))
                .body("numero", Matchers.is(cliente.getNumero()))
                .body("bairro.codigo", Matchers.is(cliente.getBairro().getCodigo()))
                .body("bairro.nome", Matchers.is(cliente.getBairro().getNome()))
                .body("pontoReferencia", Matchers.is(cliente.getPontoReferencia()))
                .body("link", Matchers.notNullValue());
    }

    @Test
    public void aoAdicionarClienteJaExistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        ClienteDTO clienteExistente = new ClienteDTO();
        try {
            clienteExistente = obterClienteValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(clienteExistente)
            .when()
                .post()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Ja existe esse cliente"));
    }

    @Test
    public void aoAlterarCliente_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        ClienteDTO cliente = new ClienteDTO();
        try {
            cliente = obterClienteValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }

        // Adiciona o cliente para depois alterá-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(cliente)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        cliente = gson.fromJson(responsePost.getBody().asString(), ClienteDTO.class);

        // Altera o cliente
        cliente.setNome(cliente.getNome() + "-Alt");

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(cliente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(cliente.getCodigo()))
                .body("nome", Matchers.is(cliente.getNome()));
    }

    @Test
    public void aoAlterarClienteInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
        ClienteDTO clienteInexistente = new ClienteDTO();
        try {
            clienteInexistente = obterClienteValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }
        clienteInexistente.setCodigo(-1);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .body(clienteInexistente)
            .when()
                .put()
            .then()
                .log().all()
                .statusCode(400)
                .body("erro", Matchers.is("Nao existe esse cliente"));
    }

    @Test
    public void aoExcluirCliente_DeveRetornarRespostaComStatus204() {
        Gson gson = new Gson();
        ClienteDTO cliente = new ClienteDTO();
        try {
            cliente = obterClienteValido();
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }

        // Adiciona o cliente para depois excluí-lo
        Response responsePost = RestAssured.given()
            .log().all()
            .contentType("application/json")
            .body(cliente)
            .when()
            .post();
        Assertions.assertEquals(200, responsePost.getStatusCode());
        cliente = gson.fromJson(responsePost.getBody().asString(), ClienteDTO.class);

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", cliente.getCodigo())
            .when()
                .delete(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void aoExcluirClienteInexistente_DeveRetornarRespostaComStatus400EMensagemDeErroCorrespodente() {
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
                .body("erro", Matchers.is("Nao existe esse cliente"));
    }

    @Test
    public void aoBuscarClientePorCodigo_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        ClienteDTO clienteExistente = new ClienteDTO();
        try {
            clienteExistente = obterClienteValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente da API");
        }

        RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .pathParam("codigo", clienteExistente.getCodigo())
            .when()
                .get(ApiUtils.urlBase + RestAssured.basePath + "/{codigo}")
            .then()
                .log().all()
                .statusCode(200)
                .body("codigo", Matchers.is(clienteExistente.getCodigo()))
                .body("nome", Matchers.is(clienteExistente.getNome()))
                .body("RG", Matchers.is(clienteExistente.getRG()))
                .body("CPF", Matchers.is(clienteExistente.getCPF()))
                .body("logradouro", Matchers.is(clienteExistente.getLogradouro()))
                .body("numero", Matchers.is(clienteExistente.getNumero()))
                .body("bairro.codigo", Matchers.is(clienteExistente.getBairro().getCodigo()))
                .body("bairro.nome", Matchers.is(clienteExistente.getBairro().getNome()))
                .body("pontoReferencia", Matchers.is(clienteExistente.getPontoReferencia()))
                .body("telefone", Matchers.is(clienteExistente.getTelefone()))
                .body("link", Matchers.is(clienteExistente.getLink()));
    }

    @Test
    public void aoBuscarClientePorNome_DeveRetornarRespostaComStatus200ECComDadosDeRespostaCorretos() {
        Gson gson = new Gson();
        ClienteDTO clienteExistente = new ClienteDTO();
        try {
            clienteExistente = obterClienteValidoDaApi(1);
        } catch (Exception e) {
            Assertions.fail("Erro ao obter cliente válido da API");
        }

        Response response = RestAssured
            .given()
                .log().all()
                .contentType("application/json")
                .queryParam("nome", clienteExistente.getNome())
            .when()
                .log().all()
                .get(ApiUtils.urlBase + "/cliente");

        Assertions.assertEquals(200, response.getStatusCode());
        ClienteDTO[] clientes = gson.fromJson(response.getBody().asString(), ClienteDTO[].class);

        Assertions.assertTrue(clientes.length > 0, "Nenhum cliente encontrado com o nome especificado");
        Assertions.assertEquals(clienteExistente.getCodigo(), clientes[0].getCodigo());
        Assertions.assertEquals(clienteExistente.getNome(), clientes[0].getNome());
    }

    public static ClienteDTO obterClienteValido() throws Exception {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNome("Cli" + Instant.now());
        cliente.setRG("12345678");
        cliente.setCPF("CPF" + Instant.now());
        cliente.setTelefone("(65) 99999-9999");
        cliente.setLogradouro("Rua Exemplo");
        cliente.setNumero("100");
        cliente.setBairro(BairroServicoTest.obterBairroValidoDaApi(1));
        cliente.setPontoReferencia("Perto da padaria");
        cliente.setLink("http://exemplo.com/cliente");
        return cliente;
    }

    public static ClienteDTO obterClienteValidoDaApi(int codigo) throws Exception {
        Response response = RestAssured.request(Method.GET, ApiUtils.urlBase + "/cliente/" + codigo);
        if (response.getStatusCode() != 200) {
            throw new Exception("Erro ao obter cliente válido"); 
        } else {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody().asString(), ClienteDTO.class);
        }
    }
}