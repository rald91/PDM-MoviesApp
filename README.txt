Instruções de Setup


1. Iniciar a API 
Iniciar o docker com os serviços da API (PostgreSQL, pgAdmin e Movies API):

    docker-compose up -d

A API estará disponível em: http://localhost:8080

Credenciais de administrador:
    - Username: admin
    - Password: admin
	
	
Credenciais de utilizador:
    - Username: user2
    - Password: user2


2. Criar Géneros na Base de Dados 
IMPORTANTE: Antes de executar a aplicação Android, é necessário criar os géneros na base de dados.

Os géneros devem ser criados através da API usando o ficheiro create_genres.json que se encontra na raiz do projeto.
 
POST para: http://localhost:8080/genres
    Basic Auth:
        - Username: admin
        - Password: admin

    Body JSON:
        Colar o conteúdo do ficheiro create_genres.json 
