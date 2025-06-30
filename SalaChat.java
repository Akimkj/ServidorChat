import java.util.ArrayList;
import java.util.List;

public class SalaChat {
    private String nomeSala;
    private List <ClienteHandler> usuarios; 
    private ClienteHandler administrador;

    public SalaChat( String nomeSala){
        this.nomeSala = nomeSala;
        this.usuarios= new ArrayList<>();

    }

    public String getNomeSala(){
        return nomeSala;
    }

    //método para adicionar usuário na sala
    public void adicionarUsuario(ClienteHandler cliente){
        usuarios.add(cliente);
        System.out.println(cliente.getNomeUsuario() + "entrou na sala");
    }
    
    //método para remover usuário da sala
    public void removerUsuario(ClienteHandler cliente){
        usuarios.remove(cliente);
        System.out.println(cliente.getNomeUsuario() + "saiu da sala");
    }

    //método para enviar mensagem na sala
    public void mensagem(String msg){
        for (ClienteHandler cliente: usuarios){
            cliente.enviarMensagem(msg);
        }
    }
    
    //método para expulsar usuário 
    public void expulsarUsuario (String nomeUsuario, ClienteHandler quemChamou){
        if(!quemChamou.isEhAdmin()){
            System.out.println("Você não tem permissão para expulsar usuários.");
            return;
        }

        //loop para procurar o usuário pelo nome
        for (ClienteHandler cliente: usuarios){  
            if (cliente.getNomeUsuario().equals(nomeUsuario)){
                usuarios.remove(cliente);
                System.out.println(nomeUsuario + "foi expulso da sala.");
                return;
            }

        }
       
    }
}
