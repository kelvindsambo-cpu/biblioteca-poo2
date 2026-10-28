# Configuração e GitHub

## Requisitos
- JDK 17 ou mais recente (`java -version`)
- Maven (`mvn -version`)
- MySQL 8+ e MySQL Workbench (opcional)
- IntelliJ IDEA ou VS Code com extensões Java
- Git e acesso ao repositório (pedir convite ao Kelvin, se ainda não tiver)

## Obter o projecto
```bash
git clone https://github.com/kelvindsambo-cpu/biblioteca-poo2.git
cd biblioteca-poo2
```

Quem já tem o projecto deve actualizar antes de começar:
```bash
git switch main
git pull origin main
```

## Trabalho individual
```bash
git switch -c feature/nome-da-tarefa
# editar ficheiros
mvn clean compile
git add .
git commit -m "Descreve o que mudou"
git push -u origin feature/nome-da-tarefa
```
Abrir Pull Request da branch para `main` no GitHub; outro membro revê antes de fazer merge. Não enviar código que não compila e não usar mensagens vagas ("done", "....").

## Preparar MySQL
No Windows, o script `sql/preparar-mysql.ps1` faz tudo de uma vez. Na raiz do projecto:

```powershell
powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1
```

Pede a password do `root` do MySQL e uma password nova para o utilizador da aplicação. Depois:
1. cria a base `biblioteca_poo2` (`sql/biblioteca.sql`) e, se for nova, insere os dados de exemplo (`sql/dados_teste.sql`);
2. cria o utilizador `biblioteca_app` com permissões apenas sobre `biblioteca_poo2`;
3. guarda as três variáveis da secção seguinte no utilizador do Windows.

Pode ser executado de novo sem duplicar dados, por exemplo para mudar a password. No fim, **fechar e voltar a abrir o terminal e o IDE**: as variáveis só são lidas por programas abertos depois.

Sem o script (Linux, macOS, ou sem PowerShell): executar `sql/biblioteca.sql` e `sql/dados_teste.sql` no Workbench ou CLI, criar um utilizador MySQL próprio com `SELECT, INSERT, UPDATE, DELETE` apenas sobre `biblioteca_poo2` e configurar a ligação como abaixo.

## Configurar ligação
A aplicação lê as variáveis de ambiente:
- `BIBLIOTECA_DB_URL` — por exemplo `jdbc:mysql://localhost:3306/biblioteca_poo2`
- `BIBLIOTECA_DB_USER` — utilizador MySQL
- `BIBLIOTECA_DB_PASSWORD` — palavra-passe

Exemplo no PowerShell, apenas na sessão actual (útil para testar sem o script):
```powershell
$env:BIBLIOTECA_DB_URL="jdbc:mysql://localhost:3306/biblioteca_poo2"
$env:BIBLIOTECA_DB_USER="biblioteca_app"
$env:BIBLIOTECA_DB_PASSWORD="SUA_SENHA_LOCAL"
```

A senha acima é apenas um marcador: não publicar senhas reais nem guardá-las em ficheiros do repositório.

## Executar
Na raiz do projecto:

| Comando | Para quê |
|---|---|
| `mvn clean compile` | Compilar. Fazer sempre antes de um commit. |
| `mvn exec:java` | Abrir a aplicação. |
| `mvn test-compile exec:java@smoke` | Testar os DAOs contra o MySQL (ver [06](06_TESTES_APRESENTACAO.md)). |

No IDE, também se pode correr `biblioteca.Main` directamente (botão ▶). O IDE tem de ter sido aberto **depois** de configurar as variáveis.

## Problemas comuns
| Sintoma | Causa provável | Solução |
|---|---|---|
| "Configure BIBLIOTECA_DB_URL, BIBLIOTECA_DB_USER e BIBLIOTECA_DB_PASSWORD." ou "Não foi possível carregar os livros." ao abrir | Variáveis não definidas neste terminal/IDE | Correr o script e reabrir terminal e IDE. Confirmar com `echo $env:BIBLIOTECA_DB_URL`. |
| `Access denied for user 'biblioteca_app'` | Password errada | Correr o script de novo e escolher outra password. |
| `Communications link failure` | O MySQL não está a correr | Abrir "Serviços" do Windows e iniciar o serviço do MySQL (ex.: `MySQL80`). |
| `Unknown database 'biblioteca_poo2'` | Base não criada | Correr o script ou `sql/biblioteca.sql`. |
| `mvn` ou `java` "não é reconhecido" | Não estão no `PATH` | Instalar e acrescentar a pasta `bin` ao `PATH`; reabrir o terminal. |
| Acentos estranhos no terminal (`indispon�vel`) | Codificação da consola | Só afecta o texto no terminal. No PowerShell, `chcp 65001` antes de correr resolve. |

## Organização
`src/main/java/biblioteca/` contém o código; `src/test/java/` o teste dos DAOs; `sql/` os scripts; `docs/` os documentos. Não criar projectos separados por membro.
