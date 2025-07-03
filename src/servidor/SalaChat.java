package servidor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*A classe SalaChat é responsável pelo funcionamento e estrutura de cada sala de chat presente no servidor */
public class SalaChat {
    private String nomeSala; //Nome da sala de chat
    private ArrayList<ClienteHandler> usuarios = new ArrayList<>(); //Lista dos membros de uma sala
    private ClienteHandler administrador; // administrador da sala

    //Método construtor
    public SalaChat( String nomeSala){
        this.nomeSala = nomeSala;
    }

    //método para adicionar usuário na sala
    public void adicionarUsuario(ClienteHandler cliente) {
        usuarios.add(cliente);

        // Enviar notificação para os outros usuários da sala
        for (ClienteHandler c : usuarios) {
            if (c != cliente) {
                c.enviarMensagem("[INFO] " + cliente.getNomeUsuario() + " entrou na sala.");
            }
        }
        cliente.enviarMensagem("[INFO] Você entrou na sala: " + nomeSala);

        // Definir administrador como sendo o primeiro membro da sala, garantindo que todos os outros não sejam ADM
        if (usuarios.size() == 1) {
            administrador = cliente;
            cliente.setEhAdmin(true);
            cliente.enviarMensagem("[INFO] Você é o administrador da sala.");
        } else {
            cliente.setEhAdmin(false);
        }
    }
    
    //método para remover usuário da sala
    public void removerUsuario(ClienteHandler cliente){
        usuarios.remove(cliente);
        mensagem("[INFO] " + cliente.getNomeUsuario() + " saiu na sala.");
        // Se o administrador sair, ele perde o status
        if (cliente == administrador) {
            cliente.setEhAdmin(false);
            administrador = null;

            // Se ainda houver usuários na sala, o primeiro usuario da lista vira o novo admin
            if (!usuarios.isEmpty()) {
                ClienteHandler novoAdmin = usuarios.get(0);
                administrador = novoAdmin;
                novoAdmin.setEhAdmin(true);
                novoAdmin.enviarMensagem("Você foi promovido a administrador da sala: " + nomeSala);
            }
        }
    }

    //método para enviar mensagem na sala
    public void mensagem(String msg){
        for (ClienteHandler cliente: usuarios){
            cliente.enviarMensagem(msg);
        }
    }
    
    //método para expulsar usuário 
    public void expulsarUsuario(String nomeUsuario, ClienteHandler quemChamou) {
        if (!quemChamou.equals(administrador) ) { //Se quem chamou o método não é ADM, não há permissão para expulsar
            quemChamou.enviarMensagem("Você não tem permissão para expulsar usuários.");
            return;
        }
        if(quemChamou.equals(administrador) && !quemChamou.getNomeUsuario().equals(nomeUsuario)){ //Garante que o administrador não pode se expulsar
            Iterator<ClienteHandler> iterator = usuarios.iterator();
            while (iterator.hasNext()) {
                ClienteHandler cliente = iterator.next();
                if (cliente.getNomeUsuario().equals(nomeUsuario)) {
                    iterator.remove();
                    cliente.setEhAdmin(false); // Caso ele fosse admin por algum erro
                    cliente.enviarMensagem("Você foi expulso da sala '" + nomeSala + "' pelo administrador.");
                    quemChamou.enviarMensagem("Usuário '" + nomeUsuario + "' foi expulso com sucesso.");

                    return;
                }
            } quemChamou.enviarMensagem("Usuário '" + nomeUsuario + "' não encontrado na sala.");

        } else
            quemChamou.enviarMensagem("Você nao pode expulsar a si mesmo");
    }

    //Método para listar todos os usuários da sala
    public List<String> listarNomesUsuarios() {
        List<String> nomes = new ArrayList<>();
        for (ClienteHandler cliente : usuarios) {
            String nome = cliente.getNomeUsuario();
            if (cliente == administrador) {
                nome += " *";
            }
            nomes.add(nome);
        }
        return nomes;
    }

    // promove um usuário para administrador, apenas um ADM pode promover outro usuário
    public void promoverAdministrador(String nomeUsuario, ClienteHandler quemChamou) {
        if (!quemChamou.equals(administrador)) {
            quemChamou.enviarMensagem("Apenas o administrador pode promover outro usuário.");
            return;
        }

        for (ClienteHandler cliente : usuarios) {
            if (cliente.getNomeUsuario().equals(nomeUsuario)) {
                administrador = cliente;
                cliente.enviarMensagem("Você foi promovido a administrador da sala '" + nomeSala + "'.");
                quemChamou.enviarMensagem("Usuário '" + nomeUsuario + "' agora é o administrador da sala.");
                return;
            }
        }

        quemChamou.enviarMensagem("Usuário '" + nomeUsuario + "' não encontrado na sala.");
    }

    //Getters e Setters importantes

    public String getNomeSala(){
        return nomeSala;
    }

    public ArrayList getUsuarios() {
        return usuarios;
    }
    public ClienteHandler getAdministrador() {
        return administrador;
    }
    public void setAdministrador(ClienteHandler novoAdmin) {
        this.administrador = novoAdmin;
    }

}
