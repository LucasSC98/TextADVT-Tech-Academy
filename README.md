# Projeto Text Adventure

![Casa Assombrada](https://i.imgur.com/RlyC7kW.png)



Este é um projeto de um jogo de aventura baseado em texto, desenvolvido em Java, TypeScript e JavaScript, utilizando o framework Maven para gerenciamento de dependências. O jogo permite que os jogadores interajam com o ambiente e tomem decisões que afetam o desenrolar da história.

## Tecnologias Utilizadas

- **Java**: Linguagem principal para a lógica do jogo.
- **JavaScript**: Utilizado para manipulação de elementos no frontend.
- **Maven**: Gerenciamento de dependências e build do projeto.
- **Spark Java**: Framework para criação de rotas e servidor web.
- **Gson**: Biblioteca para manipulação de JSON.

## Estrutura do Projeto

- `src/main/java/controller/GameController.java`: Controlador principal do jogo, responsável por gerenciar as rotas e a lógica do jogo.
- `src/main/resources/public/styles.css`: Arquivo de estilos CSS para a interface do jogo.
- `src/main/resources/templates/game.html`: Template HTML para a página do jogo.
- `src/main/java/model`: Pacote contendo as classes de modelo do jogo.
- `src/main/java/repositorio`: Pacote contendo as classes de acesso a dados (DAO).

## Funcionalidades

- **Iniciar Jogo**: O jogador pode iniciar um novo jogo.
- **Salvar Jogo**: O jogador pode salvar o estado atual do jogo.
- **Carregar Jogo**: O jogador pode carregar um jogo salvo anteriormente.
- **Interação com o Ambiente**: O jogador pode interagir com diferentes locais e itens no jogo.
- **Inventário**: O jogador pode gerenciar os itens coletados durante o jogo.
- **Ajuda**: O jogador pode obter ajuda sobre os comandos disponíveis.

## Comandos

- **start**: Inicia um novo jogo.
- **save**: Salva o estado atual do jogo.
- **load**: Carrega um jogo salvo anteriormente.
- **inventory**: Mostra os itens no inventário do jogador.
- **help**: Exibe a lista de comandos disponíveis.

## Como Executar

1. Clone o repositório:
    ```sh
    git clone https://github.com/LucasSC98/text-adventure.git
    ```

2. Navegue até o diretório do projeto:
    ```sh
    cd text-adventure
    ```

3. Compile o projeto utilizando Maven:
    ```sh
    mvn clean install
    ```

4. Execute o projeto:
    ```sh
    mvn exec:java -Dexec.mainClass="controller.GameController"
    ```

5. Abra o navegador e acesse:
    ```
    http://localhost:4567/game
    ```
