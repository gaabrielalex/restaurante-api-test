# Restaurante API Test

Este projeto foi desenvolvido para **testar a aplicação Restaurante Base**, que pode ser encontrada no repositório [Restaurante Base](https://github.com/gaabrielalex/restaurante-base). Ele realiza testes end-to-end (E2E) para garantir que as funcionalidades principais da aplicação base estejam funcionando corretamente.

## 🎯 Objetivo

O objetivo deste projeto é validar o comportamento da aplicação Restaurante Base, cobrindo cenários reais de uso, como:

- Criação, atualização e exclusão de recursos (clientes, pedidos, produtos, etc.).
- Verificação de respostas das APIs RESTful.
- Garantia de integridade dos dados manipulados pela aplicação.
- Testes de fluxo completo, simulando interações do início ao fim.

## 🧪 Tipos de Testes

Os testes implementados neste projeto incluem:

1. **Testes End-to-End (E2E)**  
   Simulam o comportamento do sistema como um todo, verificando a interação entre os diferentes componentes da aplicação.

2. **Testes de Integração**  
   Validam a comunicação entre os serviços RESTful e o banco de dados.

3. **Testes de Validação de Dados**  
   Garantem que os dados enviados e recebidos pelas APIs estão corretos e seguem os padrões esperados.

## 🚀 Funcionalidades Testadas

Os testes cobrem as principais funcionalidades da aplicação Restaurante Base, incluindo:

- **Gestão de Cardápios**  
  Testes para criação, edição, exclusão e consulta de itens do cardápio.

- **Controle de Clientes**  
  Testes para cadastro, atualização e busca de clientes.

- **Gestão de Pedidos**  
  Testes para criação de pedidos, adição de itens e alteração de status.

- **Administração de Funcionários**  
  Testes para gerenciamento de usuários internos e permissões.

## 🛠️ Tecnologias Utilizadas

Este projeto utiliza as seguintes tecnologias e frameworks:

- **Java 11 ou superior**: Linguagem principal do projeto.
- **JUnit 5**: Framework para criação e execução de testes.
- **RestAssured**: Biblioteca para testes de APIs RESTful.
- **Maven**: Gerenciador de dependências e build.
