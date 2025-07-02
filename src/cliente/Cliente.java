package cliente;

import java.io.*;
import java.net.Socket;

public class Cliente {

    private Socket socket;
    private BufferedReader teclado;
    private BufferedReader entradaServidor;
    private PrintWriter saidaServidor;
    private String nomeUsuario;

    public void conectar(String endereco, int porta) {
        try {
            socket = new Socket(endereco, porta);
            teclado = new BufferedReader(new InputStreamReader(System.in));
            entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saidaServidor = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Digite seu nome de usuário: ");
            nomeUsuario = teclado.readLine();
            saidaServidor.println(nomeUsuario);

            // Thread para receber mensagens do servidor
            new Thread(this::receberMensagens).start();

            // Enviar comandos digitados pelo usuário
            String comando;
            while ((comando = teclado.readLine()) != null) {
                enviarComando(comando);
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    public void enviarComando(String comando) {
        saidaServidor.println(comando);
    }

    public void receberMensagens() {
        String mensagem;
        try {
            while ((mensagem = entradaServidor.readLine()) != null) {
                System.out.println(mensagem);
            }
        } catch (IOException e) {
            System.out.println("Conexão encerrada pelo servidor.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar o socket: " + e.getMessage());
            }
        }
    }

    // Método main para iniciar o cliente
    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.conectar("localhost", 12345); // Altere o IP/porta se necessário
    }
}
