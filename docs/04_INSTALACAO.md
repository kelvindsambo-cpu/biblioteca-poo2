# Instalação e execução

## Requisitos
- JDK 17 ou mais recente (`java -version`)
- Maven (`mvn -version`), ou um IDE com suporte a Maven (IntelliJ IDEA, VS Code com as extensões Java)
- MySQL 8+ (o MySQL Workbench é opcional)

## Obter o projecto
```bash
git clone https://github.com/kelvindsambo-cpu/biblioteca-poo2.git
cd biblioteca-poo2
```

## Preparar o MySQL
No Windows, o script `sql/preparar-mysql.ps1` faz tudo de uma vez. Na raiz do projecto:

```powershell
powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1
```

Pede a password do `root` do MySQL e uma password nova para o utilizador da aplicação. Depois:
1. cria a base `biblioteca_poo2` (`sql/biblioteca.sql`) e, se for nova, insere os dados de exemplo (`sql/dados_teste.sql`);
2. cria o utilizador `biblioteca_app` com permissões apenas sobre `biblioteca_poo2`;
3. guarda as três variáveis da secção seguinte no utilizador do Windows.

Pode ser executado de novo sem duplicar dados, por exemplo para mudar a password. No fim, **fechar e voltar a abrir o terminal e o IDE**: as variáveis só são lidas por programas abertos depois.

Sem o script (Linux, macOS, ou sem PowerShell): executar `sql/biblioteca.sql` e `sql/dados_teste.sql` no Workbench ou na linha de comandos, criar um utilizador MySQL com `SELECT, INSERT, UPDATE, DELETE` apenas sobre `biblioteca_poo2` e configurar a ligação como abaixo.

### Base criada com a versão 1
Basta correr o script outra vez. Ele detecta a base antiga (sem a coluna `livro.capa`) e:
- aplica `sql/actualizar-v2.sql`, que acrescenta os prazos, as multas e as capas;
- cria as tabelas `reserva` e `funcionario`;
- cria o utilizador `admin` / `admin123`.

Os dados existentes mantêm-se e os empréstimos antigos ficam com prazo de 14 dias. Sem o script, executar no MySQL `sql/biblioteca.sql` e depois `sql/actualizar-v2.sql` (uma só vez).

Para recomeçar com os dados de exemplo (10 livros com capa, um atraso, uma multa e uma reserva), apagar a base com `DROP DATABASE biblioteca_poo2;` e correr o script de novo.

## Configurar a ligação
A aplicação lê as variáveis de ambiente:
- `BIBLIOTECA_DB_URL`, por exemplo `jdbc:mysql://localhost:3306/biblioteca_poo2`;
- `BIBLIOTECA_DB_USER`, o utilizador MySQL;
- `BIBLIOTECA_DB_PASSWORD`, a palavra-passe.

Exemplo no PowerShell, apenas para a sessão actual:
```powershell
$env:BIBLIOTECA_DB_URL="jdbc:mysql://localhost:3306/biblioteca_poo2"
$env:BIBLIOTECA_DB_USER="biblioteca_app"
$env:BIBLIOTECA_DB_PASSWORD="SUA_SENHA_LOCAL"
```

A senha acima é apenas um marcador: as credenciais nunca ficam no código nem no repositório.

## Executar
Na raiz do projecto:

| Comando | Para quê |
|---|---|
| `mvn clean compile` | Compilar. |
| `mvn exec:java` | Abrir a aplicação. |
| `mvn test` | Testes JUnit das regras, sem base de dados. |
| `mvn test-compile exec:java@smoke` | Testar os DAOs contra o MySQL (ver [05 — Testes](05_TESTES.md)). |

No IDE também se pode correr `biblioteca.Main` directamente. O IDE tem de ter sido aberto **depois** de configurar as variáveis.

Primeira entrada: utilizador `admin`, password `admin123`. Alterar a password logo a seguir, em **Alterar password**.

## Problemas comuns
| Sintoma | Causa provável | Solução |
|---|---|---|
| "Não foi possível ligar à base de dados" com "Configure BIBLIOTECA_DB_URL, ..." | Variáveis não definidas neste terminal ou IDE | Correr o script e reabrir o terminal e o IDE. Confirmar com `echo $env:BIBLIOTECA_DB_URL`. |
| `Access denied for user 'biblioteca_app'` | Password errada | Correr o script de novo e escolher outra password. |
| `Communications link failure` | O MySQL não está a correr | Abrir "Serviços" do Windows e iniciar o serviço do MySQL (ex.: `MySQL80`). |
| `Unknown database 'biblioteca_poo2'` | Base não criada | Correr o script ou `sql/biblioteca.sql`. |
| `mvn` ou `java` "não é reconhecido" | Não estão no `PATH` | Instalar e acrescentar a pasta `bin` ao `PATH`; reabrir o terminal. Em alternativa, correr pelo IDE. |
| Acentos estranhos no terminal (`indispon�vel`) | Codificação da consola | Só afecta o texto no terminal. No PowerShell, `chcp 65001` antes de correr resolve. |

## Organização do repositório
- `src/main/java/biblioteca/`: código da aplicação (`model`, `view`, `controller`, `dao`, `database`, `util`);
- `src/main/resources/capas/`: capas dos livros de exemplo;
- `src/test/java/`: testes JUnit e teste dos DAOs;
- `sql/`: scripts da base de dados;
- `docs/`: documentação e relatório.
