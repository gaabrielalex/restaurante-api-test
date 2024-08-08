package ifmt.cba;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

public class GrupoAlimentarServicoTest3 {
    
    @Test
    public void testInclusaoComDadosCorretos1(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{\"nome\":\"Inclusao pelo Teste\"}")
        .when()
            .post("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo",Matchers.is(Matchers.notNullValue()))
            .body("nome",Matchers.is("Inclusao pelo Teste"));
    }

    @Test
    public void testInclusaoComDadosIncorretos2(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Nome nao valido"));
    }

    @Test
    public void testAlteracaoComDadosCorretos(){
        
        RestAssured
        .given()
            .log().all()
            .contentType("application/json")
            .body("{\"codigo\": 1, \"nome\":\"Alteracao pelo Teste\"}")
        .when()
            .put("http://localhost:8080/grupoalimentar/")
        .then()
            .log().all()
            .statusCode(200)
            .body("codigo",Matchers.is(Matchers.notNullValue()))
            .body("nome",Matchers.is("Alteracao pelo Teste"));
    }

    @Test
    public void testExclusaoComDadosCorretos(){
        
        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/grupoalimentar/1")
        .then()
            .log().all()
            .statusCode(200);
    }

    @Test
    public void testExclusaoComDadosInCorretos(){
        
        RestAssured
        .given()
            .log().all()
        .when()
            .delete("http://localhost:8080/grupoalimentar/12345")
        .then()
            .log().all()
            .statusCode(400)
            .body("texto",Matchers.is("Esse GrupoAlimentar nao existe"));
    }
}
