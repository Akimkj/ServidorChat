package cliente;

import java.io.*;
import java.net.Socket;
/*A classe Cliente é a responsável por estabelecer  a conexão com o servidor e o envio de mensagens*/
public class Cliente {
    //
    private Socket socket; // estabelece e mantem conexão com o servidor
    private BufferedReader teclado; //ler a entrada do usuariom pelo teclado
    private BufferedReader entradaServidor; //ler as entradas enviadas do servidor
    private PrintWriter saidaServidor; // para enviar mensagens e comandos ao servidor
    private String nomeUsuario; // Nome escolhido pelo Usuário para se conectar


    //Método construtor que recebe o endereço IP e a porta do servidor
    public void conectar(String endereco, int porta) {
        try { // tenta criar o fluxo de dados com o cliente
            socket = new Socket(endereco, porta);
            teclado = new BufferedReader(new InputStreamReader(System.in));
            entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saidaServidor = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Digite seu nome de usuário: ");
            nomeUsuario = teclado.readLine();
            saidaServidor.println(nomeUsuario);

            // Thread para receber e enviar mensagens para o  servidor
            new Thread(this::receberMensagens).start();

            // Loop principal para enviar comandos digitados pelo usuário.
            String comando;
            while ((comando = teclado.readLine()) != null) {
                enviarComando(comando);
            }

        } catch (IOException e) { //Se falhar a atribuição de streams, emite uma erro
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    // Envia um comando ou mensagem para o servidor.
    public void enviarComando(String comando) {
        saidaServidor.println(comando);
    }

    //escuta continuamente por mensagens recebidas do servidor e as imprime terminal.
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
        cliente.conectar("172.29.175.25", 12345); // Altere o IP/porta se necessário
    }
}
