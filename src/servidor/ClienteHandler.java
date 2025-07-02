package servidor;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class ClienteHandler extends Thread {
	private Socket socket;
	private BufferedReader entrada;
	private PrintWriter saida;
	private String nomeUsuario;
	private boolean ehAdmin;
	private SalaChat salaAtual;
	private ServidorChat servidor;
	
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

	@Override
    public void run() {
        try {
            nomeUsuario = entrada.readLine();
            saida.println("Bem vindo, " + nomeUsuario + "!");
            saida.println("Digite </ajuda> para exibir comandos disponíveis.");
            servidor.getClientesConectados().add(this);
            System.out.println("Novo cliente conectado: " + nomeUsuario +" (IP: " + socket.getInetAddress().getHostAddress() + ")");
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

    private void processarComando(String linha) {
        if (linha.startsWith("/sair")) {
            sairDaSala();
        } else if (linha.startsWith("/sala ")) {
            String nomeSala = linha.substring(6).trim();
            servidor.criarSala(nomeSala);
            sairDaSala();
            salaAtual = servidor.getSala(nomeSala);
            salaAtual.adicionarUsuario(this);
        } else if (linha.startsWith("/expulsar ")) {
            if (salaAtual != null) {
                String nomeExpulso = linha.substring(10).trim();
                salaAtual.expulsarUsuario(nomeExpulso, this);
            } else {
                enviarMensagem("Você não está em uma sala.");
            }
        } else if (linha.equals("/salas")) {
            List<String> nomesSalas = servidor.getNomesSalas();
            if (nomesSalas.isEmpty()) {
                enviarMensagem("Não há salas disponíveis no momento.");
            } else {
                enviarMensagem("Salas disponíveis:");
                for (String nome : nomesSalas) {
                    enviarMensagem("- " + nome);
                }
            }
        } else if (linha.equals("/ajuda")) {
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
        } else if (linha.equals("/integrantes")) {
            if (salaAtual != null) {
                List<String> nomes = salaAtual.listarNomesUsuarios();
                enviarMensagem("Usuários na sala '" + salaAtual.getNomeSala() + "':");
                for (String nome : nomes) {
                    enviarMensagem("- " + nome);
                }
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        } else if (linha.startsWith("/promover ")) {
            if (salaAtual != null) {
                String nomeAlvo = linha.substring(10).trim();
                salaAtual.promoverAdministrador(nomeAlvo, this);
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        } else if (linha.startsWith("/avisar ")) {
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



        } else if (linha.startsWith("/removersala ")) {
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

            // Agora que está vazia, podemos removê-la
            servidor.removerSala(nomeSala);
            enviarMensagem("Sala '" + nomeSala + "' foi removida com sucesso.");
        }else {
            if (salaAtual != null) {
                salaAtual.mensagem(nomeUsuario + ": " + linha);
            } else {
                enviarMensagem("Você não está em nenhuma sala.");
            }
        }
    }

    public void enviarMensagem(String msg) {
        saida.println(msg);
    }

    public void sairDaSala() {
        if (salaAtual != null) {
            salaAtual.removerUsuario(this);
            salaAtual = null;
        }
    }

    public void desconectar() {
        try {
            sairDaSala();
            servidor.removerCliente(this);
            socket.close();
            //servidor.getClientesConectados().remove(this);
            System.out.println("cliente.Cliente desconectado: " + nomeUsuario +
                    " (IP: " + socket.getInetAddress().getHostAddress() + ")");
        } catch (IOException e) {
            System.out.println("Erro ao desconectar cliente: " + e.getMessage());
        }
    }

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
