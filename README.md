# VamoJunto — console Java + MySQL (UPX 2 · Vision Hexa)

> "Você já vai? Vamos juntos."
> Sistema de caronas em tempo real: o motorista cadastra a rota que já vai fazer, o passageiro encontra uma viagem compatível, pede carona e o combustível é dividido de forma proporcional ao trecho.

## Conteúdo

```
vamojunto/
├── banco/vamojunto.sql        script completo do banco (DDL, DML, views, functions, procedures, triggers, usuários)
├── pom.xml                    projeto Maven (abre direto no NetBeans)
├── src/main/java/vamojunto/
│   ├── App.java               ponto de entrada (main)
│   ├── conexao/               Conexao (JDBC, atributos e métodos estáticos)
│   ├── modelo/                9 classes de domínio + 6 enums (diagrama de classes)
│   ├── dao/                   padrão DAO: todo o SQL do sistema fica aqui
│   ├── consulta/              Consulta (abstrata) → ConsultaProcedure / ConsultaView → OperacaoProcedure
│   ├── console/               Menu, OpcaoMenu, Entrada (console e JOptionPane), TabelaConsole, Sessao
│   ├── match/                 padrão Strategy para ordenar as caronas encontradas
│   ├── tela/                  telas: inicial, usuário, passageiro, motorista, conta, relatórios, administrador
│   └── util/                  Validador, Seguranca (hash), Formatador, ErroBanco, VamoJuntoException
└── src/test/java/             21 testes automatizados (JUnit 5)
```

## Como rodar

1. **Banco** — no MySQL Workbench: *File → Open SQL Script → `banco/vamojunto.sql` → executar tudo (raio)*.
   Pelo terminal: `mysql -u root -p < banco/vamojunto.sql`. O script apaga e recria o banco, e pode ser executado quantas vezes precisar.
2. **Java** — no NetBeans: *File → Open Project* → selecione a pasta `vamojunto` (o NetBeans reconhece o `pom.xml`). Na primeira vez ele baixa sozinho o driver `mysql-connector-j` e o JUnit (precisa de internet).
3. Rode com **F6** (classe principal `vamojunto.App`). Testes: botão direito no projeto → *Test* (ou `mvn test`).
4. Jar executável: *Clean and Build* gera `target/vamojunto.jar` já com o driver → `java -jar target/vamojunto.jar`.

**Projeto Ant (sem Maven):** crie um projeto *Java with Ant*, copie `src/main/java/vamojunto` para `src/` e adicione o jar do `mysql-connector-j` em *Libraries → Add JAR/Folder*.

**MySQL em outra porta ou outra senha:** em *Properties → Run → VM Options*:
`-Ddb.url=jdbc:mysql://localhost:3307/vamojunto?sslMode=DISABLED&allowPublicKeyRetrieval=true`
(também existem `-Ddb.usuario` e `-Ddb.senha`). O sistema conecta com o usuário restrito **vamojunto_app**, nunca com o root.

**Entrada por caixas de diálogo (JOptionPane):** VM Options `-Dentrada=dialogo`.

### Contas para testar

| Quem | E-mail | Senha | Situação |
|---|---|---|---|
| Ana Souza | ana.souza@uniso.br | vamo123 | motorista (viagens 1, 4 e 5) |
| Elisa Rocha | elisa.rocha@outlook.com | vamo123 | motorista (viagens 2 e 3) |
| Bruno Lima | bruno.lima@gmail.com | vamo123 | passageiro |
| Carla Mendes | carla@uniso.br | vamo123 | passageira |
| Diego Alves | diego.alves@ufscar.br | vamo123 | **não verificado** (bom para testar o administrador) |
| Felipe Duarte | felipe@fatec.sp.gov.br | vamo123 | passageiro |
| Administrador | — | admin2026 | menu "Área do administrador" |

Um roteiro curto para a apresentação: entrar como **Bruno** → *Área do passageiro → Encontrar carona* (Sorocaba → Itu) → pedir a carona → sair → entrar como **Ana** → *Área do motorista → Pedidos recebidos* → aceitar → ver o rateio recalculado → *Gerenciar viagem* → iniciar → concluir → *Avaliar caronas*.

## Requisitos funcionais → onde estão no sistema

| RF | Requisito | Menu | Banco |
|---|---|---|---|
| RF01 | Cadastro e login | Inicial → Criar conta / Entrar | `trg_usuario_bi` (18+), UNIQUE de CPF e e-mail |
| RF02 | Perfil de motorista (só verificado e ativo) | Motorista → Quero ser motorista | `trg_motorista_bi` |
| RF03 | Cadastro de veículo | Motorista → Cadastrar veículo | CHECK de placa, ano e lugares |
| RF04 | Rota em tempo real (origem, paradas, destino) | Motorista → Oferecer viagem / Gerenciar → Adicionar parada | `trg_viagem_bi`, `trg_ponto_bi/_bu` |
| RF05 | Busca de rotas | Passageiro → Buscar viagens | `sp_buscar_viagens` |
| RF06 | Trajetos semelhantes (match) | Passageiro → Encontrar carona | `sp_match_rotas`, `fn_trecho_estimado` |
| RF07 | Solicitar carona escolhendo o embarque | Passageiro → Encontrar carona / Pedir pelo código | `trg_solicitacao_bi` |
| RF08 | Aceitar ou recusar | Motorista → Pedidos recebidos | `sp_responder_solicitacao` (transação) |
| RF09 | Custo e divisão do combustível | resumo da viagem e do pedido | `fn_calcular_custo`, `fn_calcular_rateio`, `trg_viagem_au` |
| RF10 | Registro da viagem e histórico | Gerenciar viagem (iniciar/concluir/cancelar) · Meu histórico | `sp_iniciar/concluir/cancelar_viagem`, `sp_historico_usuario` |
| RF11 | Avaliação mútua | Avaliar caronas · Meu perfil | `sp_avaliacoes_pendentes`, `trg_avaliacao_bi` |

Requisitos não funcionais: senha só como hash SHA-256 (`Seguranca`), CPF sempre mascarado (`fn_mascarar_cpf` / `Formatador`), 18+ anos (Java e trigger), integridade por chaves, CHECKs e transações, usuário de banco com o mínimo de privilégio.

## Cobertura dos planos de ensino

### Programação Orientada a Objetos

| Aula | Conteúdo | Onde aparece no código |
|---|---|---|
| 04/08 | Revisão de Java, repetição e condição | laços `while` dos menus (`Menu.executar`), `switch` em `Parametro.ler` e `StatusSolicitacao` |
| 11/08 | Classe, objeto e encapsulamento | `modelo/*`: atributos `private`, getters e setters que validam (ex.: `Usuario.setCpf`) |
| 25/08 | Praticando encapsulamento, JOptionPane e menu | `EntradaDialogo` (JOptionPane), `Menu` com opções numeradas |
| 01/09 e 05/09 | Construtor e sobrecarga | construtores sobrecarregados em `Usuario`, `Veiculo`, `Ponto`, `Consulta`, `ConsultaView`, `Parametro`; métodos sobrecarregados `calcularRateio`, `adicionarPonto`, `lerTexto`, `lerInteiroOpcional`, `Menu.adicionar` |
| 08/09 | Lista de objetos | `ArrayList<OpcaoMenu>` no `Menu`, `List<Ponto>` na `Viagem`, listas devolvidas pelos DAOs |
| 22/09 e 29/09 | Relacionamento e associação de classes | `Viagem` → `Motorista`, `Veiculo`, `Combustivel` (associação); `Viagem` ◆ `Ponto` (composição); `Usuario` ◇ `Instituicao` (agregação); `Solicitacao` → `Viagem`, `Usuario`, `Ponto` |
| 06/10 | Herança e sobrescrita | `Motorista extends Usuario` (sobrescreve `isMotorista`, `getPerfil`, `toString`); `Consulta → ConsultaProcedure → OperacaoProcedure` (2 níveis); `EntradaBase → EntradaConsole / EntradaDialogo`; exceções próprias |
| 20/10 | Polimorfismo, classe abstrata e interface | interfaces `OpcaoMenu`, `Entrada`, `Dao<T>`, `CriterioOrdenacao`; classes abstratas `Consulta` e `EntradaBase`; `Menu` chama `opcao.executar()` sem saber a classe; `UsuarioDAO` devolve `Usuario` ou `Motorista` |
| 03/11 | Acessando MySQL com JDBC | `Conexao` (`DriverManager`), `PreparedStatement`, `CallableStatement`, `ResultSet`, `ResultSetMetaData` (`TabelaConsole`), `getGeneratedKeys`, transação JDBC (`setAutoCommit(false)`, `commit`, `rollback`) em `ViagemDAO.inserir`, `adicionarParada` e `UsuarioDAO.alterarAtivo` |
| 10/11 | Atributo e método estático, Swing | `Conexao`, `Terminal`, `Sessao`, `Validador`, `Formatador` (estáticos); contador `Conexao.conexoesAbertas`; `JOptionPane`/`JPasswordField` em `EntradaDialogo` |
| 17/11 | Plugin | driver `mysql-connector-j` e JUnit adicionados como dependências no `pom.xml` |
| 24/11 | Interface | ver 20/10; também método `default` em `CriterioOrdenacao` |

### Banco de Dados

| Aula | Conteúdo | Onde está |
|---|---|---|
| 1 a 4 | Conceitos, MER/DER, modelo lógico, normalização | modelagem já entregue (9 tabelas em 3FN); o script segue o MER |
| 5 | DDL | `CREATE DATABASE/TABLE/INDEX` com PK, FK (incluindo a FK composta), UNIQUE e 39 CHECKs |
| 6 | DML | dados de exemplo; `INSERT/UPDATE` feitos pelo Java (cadastros, viagem, pedidos, avaliações, preço, ativo/inativo) |
| 7 | SELECT, filtros, ordenação, funções | `sp_buscar_viagens`, `sp_buscar_usuarios` (LIKE, ORDER BY, DATE_FORMAT, IF, IFNULL) |
| 8 | JOINs e subconsultas | `sp_detalhes_viagem`, `sp_rota_viagem`, `sp_passageiros_viagem`, `sp_historico_usuario` (UNION ALL), `sp_usuarios_sem_carona` (NOT EXISTS), `sp_viagens_acima_media`, `sp_match_rotas`, `fn_trecho_estimado` |
| 9 | Agregação, GROUP BY, HAVING | `sp_ranking_motoristas`, `sp_relatorio_mensal`, `sp_resumo_por_instituicao`, `sp_resumo_passageiros`, `sp_estatisticas_gerais` |
| 10 | Views | 5 views; o Java usa `vw_detalhe_viagem`, `vw_caronas`, `vw_usuario_publico`, `vw_reputacao_motorista`, `vw_viagens_disponiveis` |
| 11 | Procedures, functions e triggers | 21 procedures, 11 functions, 11 triggers |
| 12 | Transações, concorrência e segurança | `START TRANSACTION/COMMIT/ROLLBACK` + `SELECT … FOR UPDATE` nas procedures de operação; transações JDBC no Java; usuários `vamojunto_app` e `vamojunto_leitura`; `PreparedStatement` contra SQL Injection |

### Engenharia de Software

| Tema | Onde aparece |
|---|---|
| Engenharia de requisitos e casos de uso | tabela RF01–RF11 acima; atores Visitante, Usuário (Passageiro/Motorista) e Administrador viraram menus |
| Análise orientada a objetos / UML | as classes do pacote `modelo` seguem o diagrama de classes (9 classes + 6 enums) |
| Padrões de projeto | **DAO** (`dao`), **Template Method** (`Consulta.executar`), **Strategy** (`match`), **Composite** (`Menu` dentro de `Menu`), **Command** (`OpcaoMenu`) |
| Construção e boas práticas | pacotes por camada (modelo, dao, tela), nomes claros, validação nas duas camadas, nenhum SQL concatenado com valor digitado, configuração por `-D` sem recompilar |
| UX e usabilidade | mensagens claras para cada erro (`ErroBanco`), o formulário pede de novo só o campo errado (`Terminal.tentar`), "Enter = todos", confirmação antes de gravar, resumo do custo antes de publicar |
| Qualidade e testes | 21 testes JUnit (`src/test`): custo e rateio com os números da documentação, regras de rota, 18 anos, hash, transições de status, validações e as estratégias de ordenação |
| Manutenção e gerência de configuração | projeto Maven com versões fixas, script do banco reexecutável, `.gitignore` |

## O que mudou no banco (versão 2)

O script parte do `vamojunto.sql` documentado e mantém todas as tabelas, regras e os dados de exemplo. Mudanças:

1. **Senhas de exemplo** com hash real: `SHA2('vamo123', 256)` no lugar de `'$2b$12$hash…'`, para o login do console funcionar.
2. **Correção em `sp_responder_solicitacao`**: agora só responde pedido **PENDENTE**. Antes, responder `ACEITA` num pedido já aceito passava sem erro (a trigger só confere quando o status muda) e alterava a `dt_resposta`.
3. **Objetos novos** (seção "EXTENSÕES" do script):
   - `fn_distancia_km` (Haversine), `fn_trecho_estimado` (km do ponto de embarque até o destino), `fn_media_avaliacao_passageiro`;
   - `sp_match_rotas` (RF06), `sp_avaliacoes_pendentes` (RF11), `sp_iniciar_viagem`, `sp_concluir_viagem`, `sp_cancelar_viagem` (com transação e conferindo se a viagem é do motorista).

Totais: 9 tabelas · 10 FKs · 39 CHECK · 11 triggers · **11 functions · 21 procedures** · 5 views · 2 usuários de banco.

## Testes realizados

- Script executado várias vezes seguidas, sem erros, no **MySQL 8.0.46** (com binlog ligado, como no Workbench; também sem avisos) e no **MariaDB 10.11.14**.
- **21/21** testes JUnit aprovados.
- Console testado de ponta a ponta nos dois servidores, conectado como `vamojunto_app`, com quatro roteiros: visitante, cadastro com entradas inválidas (nome curto, CPF errado, e-mail repetido, 31/02, menor de 18, senha curta e diferente), login, match e pedido de carona, aceite com recálculo do rateio, viagem lotada (o banco recusa e desfaz), pedido já respondido, oferta de viagem com parada, inclusão de parada depois, iniciar/concluir/cancelar viagem, avaliação, verificação de conta e desativação pelo administrador, atualização de preço (viagens antigas mantêm o preço congelado) e as 16 opções do menu de consultas, uma por uma.
- Privilégios conferidos: `vamojunto_app` não consegue `DELETE`; `vamojunto_leitura` só lê as views e não executa procedures.

Observação: nesta máquina de teste o Maven Central estava bloqueado, então o console foi executado com o driver MariaDB Connector/J (que aceita a URL `jdbc:mysql:`). O código usa apenas JDBC padrão; no NetBeans o Maven usa o `mysql-connector-j` 8.4.0 declarado no `pom.xml`.

## Pontos para atualizar na documentação

- Seção 8.11 / Tabela 5 e C4 níveis 2 e 3: o console agora tem login e menus por perfil (Visitante, Passageiro, Motorista, Administrador). O submenu "Consultas e relatórios" mantém as 16 opções documentadas.
- Contagem de objetos do banco: 11 functions e 21 procedures (eram 8 e 16).
- Seção 8.12 (testes e correções): incluir a correção do `sp_responder_solicitacao` e os testes JUnit.
