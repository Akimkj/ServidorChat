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
		// teste aq
		try {
			entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			saida = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Erro ao criar streams para o usuário: " + e.getMessage());
		}
	}
}
