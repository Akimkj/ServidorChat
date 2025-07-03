package servidor;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
/*A classe ClienteHandler é responsável por ler o comando de cada usuário do servidor para exercutar alguma ação dentro do server ou das salas, além de permitir multiplas conexões ao servidor*/
public class ClienteHandler extends Thread {
	private Socket socket; //Estabelece a conexão com cada cliente ao servidor
	private BufferedReader entrada; 
	private PrintWriter saida;
	private String nomeUsuario; //Nome escolhido pelo usuario
	private boolean ehAdmin; //Flag para definir se o cliente é ADM ou não
	private SalaChat salaAtual; //indica a sala na qual o cliente está atualmente conectado
	private ServidorChat servidor; // para indicar o servidor em que o cliente está conectado
	
    //método construtor que recebe o socket e o servidor como parâmetros e tenta criar os streams para o usuário
	public ClienteHandler(Socket socket, ServidorChat servidor) {
		this.socket = socket;
		this.servidor = servidor;
		
		try {
			entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			saida = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Erro ao criar streams para o usuário: " + e.getMessage());
		}
	}

    //Método run da super classe Thread para executar multiplos clientes simultaneamente
	@Override
    public void run() {
        try {
            nomeUsuario = entrada.readLine();
            saida.println("Bem vindo, " + nomeUsuario + "!");
            saida.println("Digite </ajuda> para exibir comandos disponíveis.");
            servidor.getClientesConectados().add(this);
            System.out.println("Novo cliente conectado: " + nomeUsuario +" (IP: " + socket.getInetAddress().getHostAddress() + ")");
            //Loop principal que espera os comandos do usuário para processar
            String linha;
            while ((linha = entrada.readLine()) != null) {
                processarComando(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    //Método responsável por processar e executar o comando de acordo com a entrada do usuário
    private void processarComando(String linha) {
        // Se o usuário digitar /sair, ele sai da sala atual
        if (linha.startsWith("/sair")) {
            sairDaSala();
        } else if (linha.startsWith("/sala ")) { // Se o cliente digitar /sala + o nome da sala, ele procura se a sala existe para entrar na sala ou para criar a sala caso não exista
            String nomeSala = linha.substring(6).trim();
            servidor.criarSala(nomeSala);
            sairDaSala();
            salaAtual = servidor.getSala(nomeSala);
            salaAtual.adicionarUsuario(this);
        } else if (linha.startsWith("/expulsar ")) { //comando para expulsar um usuario da sala
            if (salaAtual != null) {
                String nomeExpulso = linha.substring(10).trim();
                salaAtual.expulsarUsuario(nomeExpulso, this);
            } else {
                enviarMensagem("Você não está em uma sala.");
            }
        } else if (linha.equals("/salas")) { //comando para listar as salas disponíveis
            List<String> nomesSalas = servidor.getNomesSalas();
            if (nomesSalas.isEmpty()) {
                enviarMensagem("Não há salas disponíveis no momento.");
            } else {
                enviarMensagem("Salas disponíveis:");
                for (String nome : nomesSalas) {
                    enviarMensagem("- " + nome);
                }
            }
        } else if (linha.equals("/ajuda")) {//comando para listar todos os comandos possíveis
            enviarMensagem("Comandos disponíveis:");
            enviarMensagem("/sala <nome>                 - Criar ou entrar em uma sala");
            enviarMensagem("/sair                        - Sair da sala atual");
            enviarMensagem("/salas                       - Listar todas as salas disponíveis");
            enviarMensagem("/integrantes                 - Listar todos os usuarios da sala");
            enviarMensagem("/expulsar <nome>             - Expulsar usuário da sala (admin)");
            enviarMensagem("/avisar <sala> <mensagem>    - Enviar mensagem para outra sala (admin)");
            enviarMensagem("/promover <nome>             - Transferir cargo de administrador (admin)");
            enviarMensagem("/removersala <nome>          - Apaga a sala atual (admin)");
            enviarMensagem("/ajuda                       - Mostrar esta lista de comandos");
        } else if (linha.equals("/integrantes")) { //comando para listar os integrantes da sala atual
            if (salaAtual != null) {
                List<String> nomes = salaAtual.listarNomesUsuarios();
                enviarMensagem("Usuários na sala '" + salaAtual.getNomeSala() + "':");
                for (String nome : nomes) {
                    enviarMensagem("- " + nome);
                }
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        } else if (linha.startsWith("/promover ")) { //comando para promover um cliente em uma sala em administrador
            if (salaAtual != null) {
                String nomeAlvo = linha.substring(10).trim();
                salaAtual.promoverAdministrador(nomeAlvo, this);
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        } else if (linha.startsWith("/avisar ")) { //enviar um aviso para uma sala
            String[] partes = linha.substring(8).split(" ", 2);

            if (partes.length < 2) {
                enviarMensagem("Uso: /avisar <nomeSala> <mensagem>");
                return;
            }

            String salaDestino = partes[0];
            String mensagem = partes[1];
            SalaChat salaOrigem = salaAtual;
            SalaChat salaAlvo = servidor.getSala(salaDestino);

            if (salaOrigem != null){
                if (salaOrigem.getAdministrador().equals(this)){
                    if (salaAlvo != null){
                        String msgFinal = "[AVISO de " + salaOrigem.getNomeSala() + "] " + mensagem;
                        servidor.encaminharMensagem(salaDestino, msgFinal);
                        enviarMensagem("Aviso enviado para a sala '" + salaDestino + "'.");
                    } else
                        enviarMensagem("Sala de destino '" + salaDestino + "' não encontrada.");
                } else
                    enviarMensagem("Apenas o administrador da sala atual pode enviar avisos.");
            } else
                enviarMensagem("Você precisa estar em uma sala para usar este comando.");



        } else if (linha.startsWith("/removersala ")) { //remove uma sala do servidor
            String nomeSala = linha.substring(13).trim();

            SalaChat sala = servidor.getSala(nomeSala);

            if (sala == null) {
                enviarMensagem("Sala '" + nomeSala + "' não encontrada.");
                return;
            }

            // Verifica se quem chamou o comando é o administrador da sala
            if (sala.getAdministrador() != this) {
                enviarMensagem("Apenas o administrador da sala pode removê-la.");
                return;
            }

            // Expulsa todos os usuários (inclusive o próprio admin)
            List<ClienteHandler> usuariosParaRemover = new ArrayList<>(sala.getUsuarios());

            for (ClienteHandler cliente : usuariosParaRemover) {
                cliente.enviarMensagem("[INFO] A sala '" + nomeSala + "' foi encerrada pelo administrador.");
                cliente.sairDaSala(); // Garante que o cliente seja removido corretamente
            }

            servidor.removerSala(nomeSala);
            enviarMensagem("Sala '" + nomeSala + "' foi removida com sucesso.");
        }else { //se nenhum comando foi acionado, apenas emitira a mensagem na sala
            if (salaAtual != null) {
                salaAtual.mensagem(nomeUsuario + ": " + linha);
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        }
    }

    public void enviarMensagem(String msg) { //envia mensagem para o terminal do cliente
        saida.println(msg);
    }

    public void sairDaSala() { //remove o usuario da sala
        if (salaAtual != null) {
            salaAtual.removerUsuario(this);
            salaAtual = null;
        }
    }

    public void desconectar() { //desconecta o usuario do servidor
        try {
            sairDaSala();
            servidor.removerCliente(this);
            socket.close();
            //servidor.getClientesConectados().remove(this);
            System.out.println("Cliente desconectado: " + nomeUsuario +
                    " (IP: " + socket.getInetAddress().getHostAddress() + ")");
        } catch (IOException e) {
            System.out.println("Erro ao desconectar cliente: " + e.getMessage());
        }
    }

    //Getters e Setters importantes
    
    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setEhAdmin(boolean ehAdmin) {
        this.ehAdmin = ehAdmin;
    }

    public SalaChat getSalaAtual() {
        return salaAtual;
    }

    public Socket getSocket() {
        return socket;
    }
}
