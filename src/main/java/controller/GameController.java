package controller;

import static spark.Spark.*;

import config.ConfiguracaoHtml;
import model.*;
import repositorio.AcoesDAO;
import repositorio.InventarioDAO;
import repositorio.ItemDAO;
import repositorio.SaveDAO;
import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameController {
    private final GameState gameState = new GameState();
    private final Gson gson = new Gson();
    private final Inventario inventario;

    public GameController() {
        this.inventario = new Inventario(gameState);
    }

    public static void main(String[] args) {
        staticFiles.location("/public");
        ThymeleafTemplateEngine thymeleaf = ConfiguracaoHtml.create();
        GameController controller = new GameController();
        controller.setupRoutes();
    }

    private void setupRoutes() {
        //Exibe a página inicial do jogo.

        get("/game", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("gameState", gameState);
            model.put("inventario", mostrarInventario());
            model.put("helpTexto", gameState.getHelp());
            model.put("local", gameState.getLocation());
            return new ModelAndView(model, "game");
        }, new ThymeleafTemplateEngine());

        // Processa todos os comando do usuario e vai retorna em json

        post("/game", (req, res) -> {
            res.type("application/json");
            String input = req.queryParams("input").toLowerCase();
            processCommand(input);
            Map<String, Object> model = new HashMap<>();
            model.put("message", gameState.getMessage());
            model.put("inventario", mostrarInventario());
            model.put("helpTexto", gameState.getHelp());
            model.put("local", gameState.getLocation());

            return gson.toJson(model);
        });
    }

    private void processCommand(String input) {
        try {
            switch (input) {
                case "restart" -> handleRestart(input);
                case "save" -> {
                    SaveDAO.setGameState(gameState);
                    SaveDAO.salvarJogo(gameState.getIdSave());
                    inventario.salvarInventario();
                    gameState.setMessage("Jogo salvo");
                }
                case "load" -> handleLoad(input);
                default -> {
                    if (gameState.getLocation().equals("start")) {
                        handleStart(input);
                    } else {
                        locationsProcess(input);
                    }
                }
            }
        } catch (SQLException e) {
            gameState.setMessage("Erro no jogo: " + e.getMessage());
        }
    }

    private void locationsProcess(String input) throws SQLException {
        switch (gameState.getLocation()) {
            case "casa":
                casaCena1(input);
                break;
            case "poraoCasa":
                casaCena1_1(input);
                break;
            case "casaSaida":
                casaCenaJardim(input);
                break;
            case "casaFundo":
                fundoCasa(input);
                break;
            case "quarto":
                quartoCena(input);
                break;
            default:
                gameState.setMessage("Local não reconhecido.");
        }
    }

    private List<Item> mostrarInventario() {
        return gameState.getInventario().listarItens();
    }
    public void atualizarInventario(List<Item> itens) {
        this.inventario.setItens(itens);
        System.out.println(itens);
    }

    private void handleStart(String input) {
        try {
            gameState.setIdSave(0);
            if (input.contains("start")) {
                if (gameState.getIdSave() == 0) { // se não tiver nenhum save criado, cria um,
                    // como no gameState está defininido como 0, sempre vai criar
                    gameState.setIdSave(SaveDAO.novoSave(1, "casa", false));
                }
                InventarioDAO.limparInventario(gameState.getIdSave());
                gameState.setLocation("casa");
                System.out.println("ID Save Criado:"+gameState.getIdSave()); // apenas para checar qual save criou quando iniciou
                gameState.carregarCena(1);
                atualizarInventario(inventario.getItens());
                gameState.setVisitouLocal(false);
            } else {
                gameState.setMessage("Tente start para iniciar o jogo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            gameState.setMessage("Erro ao iniciar: " + e.getMessage());
        }
    }

    private void handleLoad(String input) throws SQLException {
        if (input.equalsIgnoreCase("load")) {
            Save save = SaveDAO.carregarUltimoJogo();
            if (save != null) {
                gameState.setIdSave(save.getIdSave());
                inventario.carregarInventario(gameState.getIdSave());
                gameState.carregarCena(save.getCenaAtual().getIdCena());
                List<String> itens = inventario.listarApenasNomes();
                System.out.println(itens);
                mostrarInventario();
                gameState.setLocation(save.getLocalizacao());
                System.out.println("Load no ID: " + gameState.getIdSave()); // apenas para checar em qual save deu load
                gameState.setVisitouLocal(save.isVisitouLocal());
            } else {
                gameState.setMessage("Nenhum jogo salvo encontrado.");
            }
        }
    }

    private void handleRestart(String input) throws SQLException {
       gameState.carregarCena(18);
       gameState.setLocation("start");
       if (input.equalsIgnoreCase("start")) {
           InventarioDAO.limparInventario(gameState.getIdSave());
           atualizarInventario(gameState.getInventory());
           handleStart(input);
       }else if (input.equalsIgnoreCase("load")) {
           handleLoad(input);
       }
    }

    private void casaCena1(String input) throws SQLException {
        boolean visitouPorao = gameState.isVisitouLocal();
        if (input.equalsIgnoreCase("go to porao")) {
            gameState.setLocation("poraoCasa");
            gameState.carregarCena(2);
            gameState.setVisitouLocal(true);
        } else if (input.equalsIgnoreCase("go to jardim")) {
            if (visitouPorao) {
                gameState.setLocation("casaSaida");
                gameState.carregarCena(8);
            } else {
                gameState.setMessage("Acho melhor você visitar o porão primeiro");
            }
        }else{
            gameState.setMessage("Comando Invalido no local");
        }
    }
    private void casaCena1_1(String input) throws SQLException {
        Item lanterna = ItemDAO.findItemByID(3);
        Item pilha = ItemDAO.findItemByID(2);
        Item cartucho = ItemDAO.findItemByID(5);
        Item lanternaComPilhas = ItemDAO.findItemByID(4);
        switch (input) {
            case "get lanterna":
                if (gameState.getInventario().itemNoInv(lanterna) || gameState.getInventario().itemNoInv(lanternaComPilhas)) {
                    gameState.setMessage(lanterna.getItemNoInventario());
                } else {
                    gameState.getInventario().adicionarItem(lanterna);
                    gameState.carregarCena(4);
                }
                break;
            case "get pilhas":
                if (gameState.getInventario().itemNoInv(pilha) || gameState.getInventario().itemNoInv(lanternaComPilhas)) {
                    gameState.setMessage(pilha.getItemNoInventario());
                } else {
                    gameState.getInventario().adicionarItem(pilha);
                    gameState.carregarCena(3);
                }
                break;
            case "use pilha with lanterna":
                Acoes pilhaLanterna = AcoesDAO.findAcaoById(1);
                boolean tenhoPilha = gameState.getInventario().itemNoInv(pilha);
                boolean tenhoLanterna = gameState.getInventario().itemNoInv(lanterna);

                if (tenhoPilha && tenhoLanterna) {
                    gameState.setMessage(pilhaLanterna.getDescricaoComb());
                    gameState.getInventario().usarItem(lanterna, 1);
                    gameState.getInventario().usarItem(pilha, 1);
                    gameState.getInventario().adicionarItem(lanternaComPilhas);
                }else if (gameState.getInventario().itemNoInv(lanternaComPilhas)) {
                    gameState.setMessage(lanternaComPilhas.getItemNoInventario());
                }else{
                    gameState.setMessage(pilhaLanterna.getDescricao_negativa());
                }
                break;
            case "explorar":
                if(gameState.getInventario().itemNoInv(lanternaComPilhas)) {
                    gameState.carregarCena(6);
                }else {
                    gameState.setMessage(lanternaComPilhas.getResNegativo());
                }
                break;
            case "get cartucho":
                if (gameState.getInventario().itemNoInv(cartucho)) {
                    gameState.setMessage(cartucho.getItemNoInventario());
                } else {
                    gameState.getInventario().adicionarItem(cartucho);
                    gameState.carregarCena(7);
                }
                break;
            default:
                gameState.setMessage("Comando errado");
        }
        if (input.equalsIgnoreCase("voltar")) {
            gameState.carregarCena(1);
            gameState.setLocation("casa");
        }
    }

    private void casaCenaJardim(String input) throws SQLException {
        Item chave = ItemDAO.findItemByID(1);

        if (input.equalsIgnoreCase("check container")) {
            if (gameState.getInventario().itemNoInv(chave)) {
                gameState.setMessage(chave.getItemNoInventario());
            } else {
                gameState.getInventario().adicionarItem(chave);
                gameState.carregarCena(9);
            }
        } else if (input.equalsIgnoreCase("voltar")) {
            if (gameState.getInventario().itemNoInv(chave)) {
                gameState.carregarCena(10);
                gameState.setLocation("quarto");
            } else {
                gameState.setMessage(chave.getResNegativo());
            }
        }
    }

    private void quartoCena(String input) throws SQLException {
        Item revolver = ItemDAO.findItemByID(6);
        Item cartucho = ItemDAO.findItemByID(5);
        Item revolverCarregado = ItemDAO.findItemByID(7);
        Item chave = ItemDAO.findItemByID(1);
        boolean tenhoChave = gameState.getInventario().itemNoInv(chave);
        boolean tenhoCartucho = gameState.getInventario().itemNoInv(cartucho);
        boolean tenhoRevolver = gameState.getInventario().itemNoInv(revolver);
        boolean revolverCombala = gameState.getInventario().itemNoInv(revolverCarregado);
        switch (input) {
            case "use key":
                if (tenhoChave) {
                    gameState.carregarCena(11);
                    gameState.getInventario().usarItem(chave, 1);
                } else {
                    gameState.setMessage(chave.getDescricao());
                }
                break;
            case "get revolver":
                if(tenhoRevolver) {
                    gameState.setMessage(revolver.getItemNoInventario());
                }else {
                    gameState.getInventario().adicionarItem(revolver);
                    gameState.carregarCena(12);
                }
                break;
            case "use cartucho with revolver":
                Acoes equiparArma = AcoesDAO.findAcaoById(2);

                if (tenhoCartucho && tenhoRevolver) {
                    gameState.setMessage(equiparArma.getDescricao());
                    gameState.getInventario().usarItem(revolver,1);
                    gameState.getInventario().usarItem(cartucho, 1);
                    gameState.getInventario().adicionarItem(revolverCarregado);
                } else if(revolverCombala) {
                    gameState.setMessage(revolverCarregado.getItemNoInventario());
                } else {
                    Random aleatorio = new Random();
                    if(aleatorio.nextBoolean()){
                        gameState.setMessage(equiparArma.getDescricao_negativa());
                    }else {
                        gameState.setMessage(revolver.getResNegativo());
                    }
                }
                break;
            case "check janela":
                if(revolverCombala){
                    gameState.carregarCena(13);
                }else{
                    gameState.setMessage(revolverCarregado.getResNegativo());
                }
                break;
            case "correr":
                gameState.carregarCena(14);
                break;
            case "go to exit":
                gameState.carregarCena(15);
                gameState.setLocation("casaFundo");
        }
    }

    private void fundoCasa(String input) throws SQLException {
        Item cartucho = ItemDAO.findItemByID(5);
        Item revolverCarregado = ItemDAO.findItemByID(7);
        boolean tenhoRevolver = gameState.getInventario().itemNoInv(revolverCarregado);
        switch (input) {
            case "lutar":
                if (tenhoRevolver) {
                    gameState.carregarCena(17);
                    gameState.getInventario().usarItem(revolverCarregado, 1);
                }else{
                    gameState.setMessage(revolverCarregado.getResNegativo());
                }
                break;
            case "fugir":
                gameState.carregarCena(16);
                break;
            default:
                gameState.setMessage("Comando errado");
        }
    }
}
