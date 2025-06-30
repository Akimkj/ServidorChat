package ServerChat;
import java.io.*;
import java.net.*;
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
            saida.println("Bem-vindo! Digite seu nome:");
            nomeUsuario = entrada.readLine();
            servidor.getClientesConectados().add(this);

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
            String nomeSala = linha.substring(6);
            servidor.criarSala(nomeSala);
            sairDaSala();
            salaAtual = servidor.getSala(nomeSala);
            salaAtual.adicionarUsuario(this);
        } else {
            if (salaAtual != null) {
                salaAtual.transmitirMensagem(nomeUsuario + ": " + linha);
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
        } catch (IOException e) {
            System.out.println("Erro ao desconectar cliente: " + e.getMessage());
        }
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public boolean isEhAdmin() {
        return ehAdmin;
    }

    public void setEhAdmin(boolean ehAdmin) {
        this.ehAdmin = ehAdmin;
    }

    public SalaChat getSalaAtual() {
        return salaAtual;
    }
	
	
	
	
}
