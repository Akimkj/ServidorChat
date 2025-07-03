<h1>Servidor de Chat Multiusuário com Salas de Conversa</h1>
Este projeto é um servidor de chat em Java que permite a múltiplos usuários se conectarem e se comunicarem através de salas de conversa dedicadas. Ele oferece uma plataforma simples e funcional para interações em tempo real entre os participantes.

<h2>Características</h2>
<p><Strong>Comunicação Multiusuário</Strong>: Suporta a conexão simultânea de vários clientes.</p>

<Strong>Salas de Chat:</strong> Os usuários podem entrar em diferentes salas de conversa, garantindo que as mensagens sejam entregues apenas aos participantes daquela sala específica.

<strong>Mensagens em Tempo Real:</strong> As mensagens são transmitidas instantaneamente para todos os membros da sala.

<strong>Listagem de Usuários Online:</strong> Permite verificar quais usuários estão conectados e em quais salas.

<strong>Comandos Básicos:</strong> Suporta comandos simples para interação com o servidor (ex: /entrar [nome_da_sala], /sair, /listar_salas).

<h2>Pré-requisitos</h2>
Para compilar e executar este projeto, você precisará ter o seguinte instalado em seu sistema:

<strong>Java Development Kit (JDK) 8 ou superior:</strong> Certifique-se de que o JDK esteja configurado corretamente e que as variáveis de ambiente JAVA_HOME e PATH estejam apontando para a sua instalação do Java. Você pode baixá-lo em Oracle JDK ou OpenJDK.

Como Usar
Siga os passos abaixo para configurar e executar o servidor e o cliente de chat.

1. Clonar o Repositório
Primeiro, clone o repositório para o seu ambiente local usando o Git:

Bash

git clone https://github.com/Akimkj/ServidorChat.git
cd ServidorChat
2. Compilar o Código
Navegue até a pasta raiz do projeto clonado e compile os arquivos Java.

Bash

javac src/servidor/*.java src/cliente/*.java
Observação: Dependendo da estrutura do seu ambiente e do projeto, você pode precisar ajustar o caminho para os arquivos .java ou usar uma IDE como IntelliJ IDEA ou Eclipse para compilar o projeto.

3. Executar o Servidor
Após a compilação, você pode iniciar o servidor a partir do diretório raiz do projeto:

Bash

java -cp src servidor.Servidor
O servidor será iniciado e aguardará por conexões de clientes.

4. Executar o Cliente
Abra um novo terminal ou prompt de comando (você pode abrir múltiplos para simular vários usuários) e execute o cliente:

Bash

java -cp src cliente.Cliente
Ao iniciar o cliente, você será solicitado a:

Digitar seu nome de usuário: Digite um nome e pressione Enter.

Começar a digitar comandos ou mensagens:

Para entrar em uma sala: Use o comando /entrar [nome_da_sala]. Por exemplo: /entrar geral ou /entrar desenvolvimento.

Para listar as salas disponíveis (se implementado no servidor): Use /listar_salas.

Para enviar uma mensagem para a sala atual: Basta digitar a mensagem e pressionar Enter. Todos os usuários na mesma sala receberão sua mensagem.

Para sair de uma sala (se implementado no servidor): Use /sair.

Para desconectar do servidor: Você pode digitar /sair (se implementado para isso) ou fechar a janela do terminal.

Exemplo de Interação do Cliente:

Digite seu nome de usuário: Alice
Você entrou na sala 'lobby'.
/entrar sala_1
Você entrou na sala 'sala_1'.
Olá a todos!
Estrutura do Projeto
O projeto é dividido em duas partes principais:

src/servidor: Contém o código-fonte da aplicação do servidor que gerencia as conexões, salas e roteamento de mensagens.

src/cliente: Contém o código-fonte da aplicação cliente que os usuários utilizam para se conectar e interagir com o servidor de chat.
<h2>Autores</h2>
<ul>
<li>Mika Marques</li>
<li>Marina Veiga</li>
<li>Diogo Costa</li>
<li>Roosevelt</li>
</ul> 
