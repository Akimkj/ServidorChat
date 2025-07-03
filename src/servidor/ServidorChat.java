package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*A classe ServidorChat é responsável por receber as conexões dos clientes e o gerenciamento das salas de chat */
public class ServidorChat {
    private ServerSocket servidor; //responsável para inicializar o servidor
    private List<ClienteHandler> clientesConectados; //Lista com os clientes conectados
    private Map<String, SalaChat> salas; //HashMap com as salas presentes no servidor

    //metodo construtor que recebe a porta para iniciar o servidor
    public ServidorChat(int porta) throws IOException {
        this.servidor = new ServerSocket(porta);
        //this.servidor = new ServerSocket(porta, 50, InetAddress.getByName("0.0.0.0"));
        this.clientesConectados = new ArrayList<>();
        this.salas = new HashMap<>();
        criarSala("Geral"); 
    }

    //método que informa que o servidor foi iniciado e chama o método aceitarConexoes
    public void iniciarServidor() {
        System.out.println("Servidor iniciado na porta: " + servidor.getLocalPort());
        aceitarConexoes();
    }
    //Método que contém o loop principal para aceitar novas conexões de múltiplos clientes
    private void aceitarConexoes() {
        while (!servidor.isClosed()) {
            try {
                Socket socket = servidor.accept();
                String ip = socket.getInetAddress().getHostAddress();

                if (isIpConectado(ip)) {
                    System.out.println("Conexão recusada: IP " + ip + " já está conectado.");
                    socket.close();
                } else {
                    ClienteHandler novoCliente = new ClienteHandler(socket, this);
                    clientesConectados.add(novoCliente);
                    new Thread(novoCliente).start();
                }

            } catch (IOException e) {
                System.err.println("Erro ao aceitar conexão: " + e.getMessage());
            }
        }
    }

    //Envia uma mensagem para uma sala específica, caso ela exista
    public void encaminharMensagem(String nomeSala, String mensagem) {
        SalaChat sala = salas.get(nomeSala);
        if(sala != null) {
            sala.mensagem(mensagem);
            System.out.println("Mensagem enviada pelo servidor para a sala '" + nomeSala + "': " + mensagem);
        } else {
            System.out.println("Sala '" + nomeSala + "' não encontrada.");
        }
    }

    //Método que remove o cliente da sala
    public void removerCliente(ClienteHandler cliente) {
        clientesConectados.remove(cliente);
        if (cliente.getSalaAtual() != null) {
            cliente.getSalaAtual().removerUsuario(cliente);
        }
    }

    //Método que adiciona uma nova sala ao servidor
    public void criarSala(String nomeSala) {
        if (!salas.containsKey(nomeSala)) {
            salas.put(nomeSala, new SalaChat(nomeSala));
            System.out.println("Sala criada: " + nomeSala);
        }
    }

    //Remove uma sala do servidor caso encontre o nome da sala
    public void removerSala(String nomeSala) {
        if (salas.containsKey(nomeSala) && salas.get(nomeSala).getUsuarios().isEmpty()) {
            salas.remove(nomeSala);
            System.out.println("Sala removida: " + nomeSala);
        }
    }

    //Verifica se certo ip está conectado ao servidor
    public boolean isIpConectado(String ip) {
        for (ClienteHandler cliente : clientesConectados) {
            String ipCliente = cliente.getSocket().getInetAddress().getHostAddress();
            if (ipCliente.equals(ip) && !cliente.getSocket().isClosed()) {
                return true;
            }
        }
        return false;
    }

    // Getters importantes
    public SalaChat getSala(String nomeSala) {
        return salas.get(nomeSala);
    }
    
    public List<String> getNomesSalas() {
        return new ArrayList<>(salas.keySet());
    }

    public List<ClienteHandler> getClientesConectados() {
        return clientesConectados;
    }

    //Método main para inicializar o servidor
    public static void main(String[] args) {
        try {
            ServidorChat servidor = new ServidorChat(12345);
            servidor.iniciarServidor();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}
