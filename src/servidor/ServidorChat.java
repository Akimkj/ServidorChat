package servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorChat {
    private ServerSocket servidor;
    private List<ClienteHandler> clientesConectados;
    private Map<String, SalaChat> salas;

    public ServidorChat(int porta) throws IOException {
        this.servidor = new ServerSocket(porta);
        //this.servidor = new ServerSocket(porta, 50, InetAddress.getByName("0.0.0.0"));
        this.clientesConectados = new ArrayList<>();
        this.salas = new HashMap<>();
        criarSala("Geral"); 
    }

    public void iniciarServidor() {
        System.out.println("Servidor iniciado na porta: " + servidor.getLocalPort());
        aceitarConexoes();
    }

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

    public void encaminharMensagem(String nomeSala, String mensagem) {
        SalaChat sala = salas.get(nomeSala);
        if(sala != null) {
            sala.mensagem(mensagem);
            System.out.println("Mensagem enviada pelo servidor para a sala '" + nomeSala + "': " + mensagem);
        } else {
            System.out.println("Sala '" + nomeSala + "' não encontrada.");
        }
    }

    public void removerCliente(ClienteHandler cliente) {
        clientesConectados.remove(cliente);
        if (cliente.getSalaAtual() != null) {
            cliente.getSalaAtual().removerUsuario(cliente);
        }
    }

    public void criarSala(String nomeSala) {
        if (!salas.containsKey(nomeSala)) {
            salas.put(nomeSala, new SalaChat(nomeSala));
            System.out.println("Sala criada: " + nomeSala);
        }
    }

    public void removerSala(String nomeSala) {
        if (salas.containsKey(nomeSala) && salas.get(nomeSala).getUsuarios().isEmpty()) {
            salas.remove(nomeSala);
            System.out.println("Sala removida: " + nomeSala);
        }
    }

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

    public static void main(String[] args) {
        try {
            ServidorChat servidor = new ServidorChat(12345);
            servidor.iniciarServidor();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}
