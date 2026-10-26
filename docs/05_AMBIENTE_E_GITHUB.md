# Configuração e GitHub

## Requisitos
- JDK 17 (`java -version`)
- Maven (`mvn -version`)
- MySQL 8+ e MySQL Workbench (opcional)
- IntelliJ IDEA ou VS Code com extensões Java
- Git e conta GitHub

## Criar repositório no GitHub
1. Abrir https://github.com/new e criar `biblioteca-poo2`, preferencialmente **Private**.
2. Não marcar README, `.gitignore` nem licença: já existem neste pacote.
3. Extrair o ZIP e abrir terminal dentro da pasta `biblioteca-poo2`.
4. Executar:

```bash
git init
git branch -M main
git add .
git commit -m "Estrutura inicial e documentação"
git remote add origin https://github.com/SEU_UTILIZADOR/biblioteca-poo2.git
git push -u origin main
```

5. Em **Settings > Collaborators**, convidar os outros cinco membros.

## Trabalho individual
```bash
git clone https://github.com/SEU_UTILIZADOR/biblioteca-poo2.git
cd biblioteca-poo2
git checkout -b feature/livros
# editar ficheiros
git add .
git commit -m "Implementa interface de livros"
git push -u origin feature/livros
```
Abrir Pull Request da branch para `main` no GitHub; outro membro revê antes de fazer merge. Substituir `feature/livros` pela branch atribuída.

## Preparar MySQL
Executar `sql/biblioteca.sql` no Workbench ou CLI. Criar utilizador MySQL próprio com permissões apenas sobre `biblioteca_poo2`, se possível.

## Configurar ligação
A aplicação lê as variáveis de ambiente:
- `BIBLIOTECA_DB_URL` — por exemplo `jdbc:mysql://localhost:3306/biblioteca_poo2`
- `BIBLIOTECA_DB_USER` — utilizador MySQL
- `BIBLIOTECA_DB_PASSWORD` — palavra-passe

Exemplo no PowerShell, apenas na sessão actual:
```powershell
$env:BIBLIOTECA_DB_URL="jdbc:mysql://localhost:3306/biblioteca_poo2"
$env:BIBLIOTECA_DB_USER="biblioteca_app"
$env:BIBLIOTECA_DB_PASSWORD="SUA_SENHA_LOCAL"
mvn clean compile
mvn exec:java
```

A senha acima é apenas um marcador: não publicar senhas reais. A janela inicial abre sem consultar a base de dados; os módulos JDBC serão implementados pela equipa.

## Organização
`src/main/java/biblioteca/` contém o código; `sql/` contém o script; `docs/` contém os documentos. Não criar projectos separados por membro.
