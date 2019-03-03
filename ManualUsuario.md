# ChatRabbitMQ

## Interface do Chat em Linha de Comando

Ao ser executado, o chat pergunta o nome do usuário do mesmo. Exemplo:

```
User: tarcisiorocha
```

Com o nome do usuário, o chat cria a fila do usuário no RabbitMQ e exibe um prompt para que o usuário inicie a comunicação.

```
>> 
```

### Envio de mensagens 

No prompt, se o usuário (tarcisiorocha) quer enviar mensagem para um outro usuário do chat, ele deve digitar "@" seguido do
nome do usuário com o qual ele quer conversar. Exemplo:

```
>> @joaosantos
```

Com isso, o prompt é alterado automaticamente para exibir o nome do outro usuário para o qual se quer enviar mensagem. Exemplo:

```
@joaosantos>> 
```

Nesse exemplo, toda nova mensagem digitada no prompt é enviada para "joaosantos" até que o usuário mude para para um novo
destinatário. Exemplo:

```
@joaosantos>> Olá, João!!!
@joaosantos>> Vamos adiantar o trabalho de SD?
```
Se o usuário quiser enviar mensagens para outro usuário diferente de "joaosantos", ele deve informar o nome do outro usuário
para o qual ele quer enviar mensagem:

```
@joaosantos>> @marciocosta
```

O comando acima faz o prompt ser "chaveado" para "marciocosta". Com isso, as próximas mensagens serão enviadas para o usuário
"marciocosta":

```
@marciocosta>> Oi, Marcio!!
@marciocosta>> Vamos sair hoje?
@marciocosta>> Já estou em casa!
@marciocosta>>
```

### Recebimento de Mensagens

A qualquer momento, o usuário (exemplo: tarcisiorocha) pode receber mensagem de qualquer outro usuário (marciocosta,
joaosantos...). Nesse caso, a mensagem é impressa na tela da seguinte forma:

```
(21/09/2016 às 20:53) marciocosta diz: E aí, Tarcisio! Vamos sim!
```

Depois de impressa a mensagem, o prompt volta para o estado anterior:

```
@marciocosta>> 
```

Agora segue um exemplo de três mensagens recebidas de joaosantos:

```
(21/09/2016 às 20:55) joaosantos diz: Opa!
@marciocosta>> 
(21/09/2016 às 20:55) joaosantos diz: vamos!!!
@marciocosta>> 
(21/09/2016 às 20:56) joaosantos diz: estou indo para a sua casa
@marciocosta>> 
```

## Grupos

Os comandos para tratamento de grupos são precedidos do simbolo `!`.

### Criação de Grupos

Para criar um novo grupo, o usuario pode utilizar o comando `addGroup` seguido do nome do grupo que se deseja criar.
Exemplo de criação de um grupo chamado "amigos":

```
@marciocosta>> !addGroup amigos
@marciocosta>>
```

### Inclusão de usuários em um grupo

Para incluir um usuário em um grupo deve-se usar o comando `addUser` seguido dos parametros nome do usuario e nome do grupo.
Exemplo onde se adiciona os usuários "marciocosta" e "joaosantos" ao grupo amigos:

```
@marciocosta>> !addUser joaosantos amigos
@marciocosta>> !addUser marciocosta amigos
@marciocosta>>
```

O usuário que pede para ciar um grupo é adicionado automaticamente ao mesmo grupo. Por exemplo, se considerarmos que foi o usuário "tarcisiorocha" que criou o grupo "amigos", "tarcisiorocha" é adicionado  automaticamente ao grupo amigos (com isso, se tarcisiorocha criou o grupo amigos e adicionou marciocosta e jaosantos, esse grupo fica com tres membros: tarcisiorocha, marciocosta e joaosantos).

### Envio de mensagem para um grupo

No prompt, se o usuário (tarcisiorocha) quer enviar mensagem para um grupo, ele deve digitar "#" seguido do nome do grupo
para o qual ele quer enviar mensagens. Depois que o usuário pressiona a tecla <ENTER>, o prompt é alterado para exibir o
nome do grupo correspondente. Exemplo:

```
@marciocosta>> #amigos
#amigos>>  
```

A partir disso, o usuário pode digitar as mensagens para o respectivo grupo:

```
#amigos>> Olá, pessoal!
#amigos>> Alguém vai ao show de Djavan?
#amigos>>
```

### Recebimento de mensagens de grupo

Mensagens recebidas dentro do contexto de um grupo são exibidas de forma semelhante àquelas recebidas de um usuário
individualmente com exceção de que se acrescenta do nome do grupo logo após ao nome do usuário que a postou. Exemplo:

```
(21/09/2018 às 21:50) joaosantos#amigos diz: Olá, amigos!!!
```

### Exclusão usuário de um grupo

Para remover um usuário de um determinado grupo, deve-se utilizar o comando "delFromGroup" seguido do nome do usuário
e do nome do grupo. Exemplo:

```
@marciocosta>> !delFromGroup joaosantos amigos
@marciocosta>>
```

Neste último exemplo, joaosantos é removido do grupo amigos.

Para excluir um grupo, deve-se utilizar o comando "removeGroup" seguido do nome do grupo a ser removido. Exemplo:

```
@marciocosta>> !removeGroup amigos
@marciocosta>>
```

## Envio de arquivos

O chat disponibiliza o comando "upload" precedido do simbolo `!` para permitir que um usuário envie arquivos
(de qualquer tipo) para um usuário ou grupo corrente.

Exemplo do envio do arquivo "aula1.pdf" para o usuário "marciocosta":

```
@marciocosta>> !upload /home/tarcisio/aula1.pdf
```

O envio de arquivos para um grupo é semelhante:

```
#ufs>> !upload /home/tarcisio/aula1.pdf
```

Logo depois de chamado o comando "upload", deve ser exibida a mensagem (não bloqueante)
```Enviando "<nome-do-arquivo>" para <destinatário>```. Exemplo:

```
@marciocosta>> !upload /home/tarcisio/aula1.pdf
Enviando "/home/tarcisio/aula1.pdf" para @marciocosta.
@marciocosta>>
```

Observe também que no exemplo acima, logo depois de exibida a mensagem ```Enviando "/home/tarcisio/aula1.pdf" para @marciocosta```
o chat volta instantaneamente para o prompt (ex: "@marciocosta>> "), ou seja, o processo de envio de arquivos com o comando
"upload" é feito em background (sem bloquear o chat).

Depois que o arquivo é transferido do chat emissor para o servidor do RabbitMQ, é exibida a mensagem 
```Arquivo "<nome-do-arquivo>" foi enviado para @<id-do-receptor>``` Exemplo:

```
Arquivo "/home/tarcisio/aula1.pdf" foi enviado para @marciocosta!
```

O lado receptor do chat, recebe o arquivo também em background sem bloqueios. É realizado automaticamente o download dos
arquivos a serem recebidos em uma pasta default (ex: /home/ubuntu/downloads). Quando um download é completado, é exibida
a mensagem ```(<data> às <hora>) Arquivo <nome-do-arquivo> recebido de @<id-do-emissor>!``` no lado receptor. Exemplo: 

```
(21/09/2016 às 20:55) Arquivo "aula1.pdf" recebido de @tarcisio!
```
## Listagem de usuários e grupos

O chat disponibiliza o comandos para listar todos os usuários de um dado grupo do chat e listar todos os grupos dos quais
o usuário corrente faz parte.

### Listar todos os usuários de um grupo

Pelo comando "listUsers" precedido de `!` e seguido pelo nome do grupo. Ex:

```
@marciocosta>> !listUsers ufs
tarcisio, marciocosta, faviosantos, monicaferraz
@marciocosta>> 
```

### Listar todos os grupos

Pelo comando "listGroups" precedido de `!`. Ex:

```
@marciocosta>> !listGroups
ufs, amigos, familia
@marciocosta>> 
```
