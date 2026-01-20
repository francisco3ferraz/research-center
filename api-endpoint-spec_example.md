Especificação API REST

DAE-2025-26-API-Projeto

Estudantes envolvidos:

Francisco Ferraz - 2023122475

Ivan Lourenço - 2023122718

Luis Oneto - 2022110707

Utilizadores

EP01. Listar Utilizadores

Um utilizador autenticado como administrador, consulta a lista de utilizadores utilizando o

protocolo HTTP, método GET, para o endpoint:

/api/users

A resposta devolvida por este recurso segue o formato JSON:

[

{

"id": 1 ,

"username": "joao.silva",

"name": "João Silva",

"email": "joao.silva@centro.pt",

"role": "COLABORADOR",

"active": true,

"createdAt": "2025-01-15T10:30:00Z"

},

{

"id": 2 ,

"username": "maria.santos",

"name": "Maria Santos",

"email": "maria.santos@centro.pt",

"role": "RESPONSAVEL",

"active": true,

"createdAt": "2025-01-10T14:20:00Z"

}

]

EP02. Consultar Utilizador

Um utilizador autenticado consulta detalhes de um utilizador utilizando o protocolo HTTP, método

GET, para o endpoint:

/api/users/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"username": "joao.silva",

"name": "João Silva",

"email": "joao.silva@centro.pt",

"role": "COLABORADOR",

"active": true,

"createdAt": "2025-01-15T10:30:00Z",

"lastLogin": "2025-10-20T09:15:00Z"

}

EP03. Criar Utilizador

Um utilizador autenticado como administrador cria um novo utilizador utilizando o protocolo HTTP,

método POST, para o endpoint:

/api/users

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"username": "pedro.costa",

"name": "Pedro Costa",

"email": "pedro.costa@centro.pt",

"password": "senha123",

"role": "COLABORADOR"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 3 ,

"username": "pedro.costa",

"name": "Pedro Costa",

"email": "pedro.costa@centro.pt",

"role": "COLABORADOR",

"active": true,

"createdAt": "2025-10-27T14:30:00Z"

}

EP04. Editar Utilizador

Um utilizador autenticado como administrador edita um utilizador existente utilizando o protocolo

HTTP, método PUT, para o endpoint:

/api/users/{id}

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "Pedro Costa Silva",

"email": "pedro.costa@centro.pt",

"role": "RESPONSAVEL"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 3 ,

"username": "pedro.costa",

"name": "Pedro Costa Silva",

"email": "pedro.costa@centro.pt",

"role": "RESPONSAVEL",

"active": true,

"createdAt": "2025-10-27T14:30:00Z"

}

EP05. Ativar/Desativar Utilizador

Um utilizador autenticado como administrador ativa ou desativa um utilizador utilizando o

protocolo HTTP, método PATCH, para o endpoint:

/api/users/{id}/status

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"active": false

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 3 ,

"username": "pedro.costa",

"name": "Pedro Costa Silva",

"email": "pedro.costa@centro.pt",

"role": "RESPONSAVEL",

"active": false,

"createdAt": "2025-10-27T14:30:00Z"

}

EP06. Remover Utilizador

Um utilizador autenticado como administrador remove um utilizador utilizando o protocolo HTTP,

método DELETE, para o endpoint:

/api/users/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Utilizador removido com sucesso"

}

EP07. Editar Perfil Próprio

Um utilizador autenticado edita o seu próprio perfil utilizando o protocolo HTTP, método PUT, para

o endpoint:

/api/users/profile

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "João Silva Santos",

"email": "joao.silva.novo@centro.pt"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"username": "joao.silva",

"name": "João Silva Santos",

"email": "joao.silva.novo@centro.pt",

"role": "COLABORADOR",

"active": true

}

EP08. Consultar histórico de atividade

Um utilizador autenticado consulta o histórico de atividade próprio ou de outro utilizador (se

administrador) utilizando o protocolo HTTP, método GET, para o endpoint:

/api/users/{id}/activity

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 1 ,

"userId": 1 ,

"action": "UPLOAD_PUBLICATION",

"entityType": "PUBLICATION",

"entityId": 15 ,

"description": "Upload da publicação 'Machine Learning em Ciência de Dados'",

"timestamp": "2025-10-27T10:30:00Z" },

{

"id": 2 ,

"userId": 1 ,

"action": "ADD_COMMENT",

"entityType": "PUBLICATION",

"entityId": 12 ,

"description": "Comentário adicionado à publicação",

"timestamp": "2025-10-26T15:20:00Z"

}

]

Publicações

EP09. Listar Publicações

Um utilizador autenticado consulta a lista de publicações visíveis utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/publications

Parâmetros de query opcionais:

● search: termo de pesquisa (título, autor)

● areaScientific: filtro por área científica

● tag: filtro por tag

● dateFrom: data inicial (ISO 8601)

● dateTo: data final (ISO 8601)

● sortBy: ordenação (comments, rating, ratings_count, date)

● order: ordem (asc, desc)

● page: número da página

● size: tamanho da página

A resposta devolvida por este recurso segue o formato JSON:

{

“content": \[

{

"id": 1 ,

"title": "Deep Learning Applications in Material Science",

"authors": \["João Silva", "Maria Santos"],

"type": "ARTICLE",

"areaScientific": "Ciência dos Materiais",

"year": 2024 ,

"abstract": "Este artigo explora aplicações de deep learning...",

"visible": true,

"confidential": false,

"averageRating": 4.5,

"ratingsCount": 10 ,

"commentsCount": 5 ,

"uploadedBy": { "id": 1 , "name": "João Silva" },

"uploadedAt": "2025-01-15T10:30:00Z",

"tags": \[

{

"id": 1 ,

"name": "Projeto X"

},

{

"id": 2 ,

"name": "Machine Learning"

}

]

}],

"totalElements": 150 ,

"totalPages": 15 ,

"currentPage": 0 ,

"pageSize": 10

}

EP10. Consultar Publicação

Um utilizador autenticado consulta detalhes de uma publicação utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/publications/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"title": "Deep Learning Applications in Material Science",

"authors": \["João Silva", "Maria Santos"],

"type": "ARTICLE",

"areaScientific": "Ciência dos Materiais",

"year": 2024 ,

"publisher": "IEEE",

"doi": "10.1109/example.2024.123456",

"abstract": "Este artigo explora aplicações de deep learning...",

"aiGeneratedSummary": "Resumo gerado automaticamente pela IA...",

"visible": true,

"confidential": false,

"averageRating": 4.5,

"ratingsCount": 10 ,

"uploadedBy": {

"id": 1 ,

"name": "João Silva"

},

"uploadedAt": "2025-01-15T10:30:00Z",

"updatedAt": "2025-10-20T14:30:00Z",

"tags": \[

{"id": 1 , "name": "Projeto X"},

{"id": 2 , "name": "Machine Learning"}

],

"fileUrl": "/api/publications/1/file"

}

EP11. Criar publicação (Upload)

Um utilizador autenticado cria uma nova publicação fazendo upload de um ficheiro utilizando o

protocolo HTTP, método POST, para o endpoint:

/api/publications

O corpo do pedido é do tipo multipart/form-data contendo:

● file: ficheiro PDF ou ZIP

● metadata: dados em formato JSON:

{

"title": "New Techniques in Data Science",

"authors": \["Ana Costa"],

"type": "CONFERENCE",

"areaScientific": "Ciência de Dados",

"year": 2025 ,

"publisher": "ACM",

"doi": "10.1145/example.2025.789",

"abstract": "Esta comunicação apresenta novas técnicas...",

"confidential": false, "tags": \[ 1 , 3 ]

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 25 ,

"title": "New Techniques in Data Science",

"authors": \["Ana Costa"],

"type": "CONFERENCE",

"areaScientific": "Ciência de Dados",

"year": 2025 ,

"publisher": "ACM",

"doi": "10.1145/example.2025.789",

"abstract": "Esta comunicação apresenta novas técnicas...",

"aiGeneratedSummary": "Resumo gerado automaticamente...",

"visible": true,

"confidential": false,

"averageRating": 0 ,

"ratingsCount": 0 ,

"uploadedBy": {

"id": 2 ,

"name": "Ana Costa"

},

"uploadedAt": "2025-10-27T14:30:00Z",

"tags": \[

{"id": 1 , "name": "Projeto X"},

{"id": 3 , "name": "Data Mining"}

]

}

EP12. Editar Publicações

Um utilizador autenticado edita uma publicação (apenas o autor ou responsável/administrador)

utilizando o protocolo HTTP, método PUT, para o endpoint:

/api/publications/{id}

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"title": "New Techniques in Data Science - Revised",

"authors": \["Ana Costa", "Pedro Silva"],

"abstract": "Abstract corrigido...",

"aiGeneratedSummary": "Resumo corrigido manualmente...",

"year": 2025 ,

"publisher": "ACM",

"doi": "10.1145/example.2025.789"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 25 ,

"title": "New Techniques in Data Science - Revised",

"authors": \["Ana Costa", "Pedro Silva"],

"type": "CONFERENCE",

"areaScientific": "Ciência de Dados",

"year": 2025 ,

"abstract": "Abstract corrigido...",

"aiGeneratedSummary": "Resumo corrigido manualmente...",

"visible": true,

"confidential": false,

"uploadedAt": "2025-10-27T14:30:00Z",

"updatedAt": "2025-10-27T16:45:00Z"

}

EP13. Remover Publicação

Um utilizador autenticado remove uma publicação (apenas responsável ou administrador)

utilizando o protocolo HTTP, método DELETE, para o endpoint:

/api/publications/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Publicação removida com sucesso"

}

EP13. Ocultar/Mostrar Publicação

Um utilizador autenticado como responsável ou administrador oculta ou mostra uma publicação

utilizando o protocolo HTTP, método PATCH, para o endpoint:

/api/publications/{id}/visiblity

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"visible": false

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 25 ,

"title": "New Techniques in Data Science",

"visible": false,

"updatedAt": "2025-10-27T17:00:00Z"

}

EP14. Consultar Publicações Próprias

Um utilizador autenticado consulta as publicações que submeteu utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/publications/my-publications

A resposta devolvida por este recurso segue o formato JSON:

{

“content": \[

{

"id": 1 ,

"title": "Deep Learning Applications in Material Science",

"authors": \["João Silva", "Maria Santos"],

"type": "ARTICLE",

"areaScientific": "Ciência dos Materiais",

"year": 2024 ,

"abstract": "Este artigo explora aplicações de deep learning...",

"visible": true,

"confidential": false,

"averageRating": 4.5,

"ratingsCount": 10 ,

"commentsCount": 5 ,

"uploadedBy": { "id": 1 , "name": "João Silva" },

"uploadedAt": "2025-01-15T10:30:00Z",

"tags": \[

{

"id": 1 ,

"name": "Projeto X"

},

{

"id": 2 ,

"name": "Machine Learning"

}

]

}],

"totalElements": 150 ,

"totalPages": 15 ,

"currentPage": 0 ,

"pageSize": 10

}

EP15. Consultar Histórico de Edições de Publicação

Um utilizador autenticado consulta o histórico de edições de uma publicação utilizando o

protocolo HTTP, método GET, para o endpoint:

/api/publications/{id}/history

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 1 ,

"publicationId": 25 ,

"editedBy": {

"id": 2 ,

"name": "Ana Costa"

},

"action": "UPDATE",

"changedFields": \["title", "authors"],

"timestamp": "2025-10-27T16:45:00Z",

"description": "Título e autores atualizados"

},

{

"id": 2 ,

"publicationId": 25 ,

"editedBy": {

"id": 2 ,

"name": "Ana Costa"

},

"action": "CREATE",

"timestamp": "2025-10-27T14:30:00Z",

"description": "Publicação criada"

}

]

EP16. Download de Ficheiro de Publicação

Um utilizador autenticado faz download do ficheiro de uma publicação utilizando o protocolo

HTTP, método GET, para o endpoint:

/api/publications/{id}/file

A resposta devolvida é o ficheiro binário (PDF ou ZIP) com o content-type apropriado.

Ratings

EP17. Adicionar Rating a Publicação

Um utilizador autenticado adiciona ou atualiza o seu rating a uma publicação utilizando o

protocolo HTTP, método POST, para o endpoint:

/api/publications/{id}/ratings

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"value": 5

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"publicationId": 25 ,

"userId": 1 ,

"userName": "João Silva",

"value": 5 ,

"createdAt": "2025-10-27T17:30:00Z"

}

EP18. Consultar Ratings de Publicação

Um utilizador autenticado consulta o histórico de edições de uma publicação utilizando o

protocolo HTTP, método GET, para o endpoint:

/api/publications/{id}/ratings

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"averageRating": 4.5,

"totalRatings": 10 ,

"distribution": { "1": 0 , "2": 1 , "3": 2 , "4": 3 , "5": 4 },

"ratings": \[ {

"id": 1 ,

"userId": 1 ,

"userName": "João Silva",

"value": 5 ,

"createdAt": "2025-10-27T17:30:00Z"

}]

}

EP19. Remover Ratings de Publicação

Um utilizador autenticado remove o seu rating de uma publicação utilizando o protocolo HTTP,

método DELETE, para o endpoint:

/api/publications/{id}/ratings

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Rating removido com sucesso"

}

Comentários

EP20. Adicionar Comentário a uma Publicação

Um utilizador autenticado adiciona um comentário a uma publicação utilizando o protocolo HTTP,

método POST, para o endpoint:

/api/publications/{id}/comments

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

“content": "Comentário do projeto de DAE!"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"publicationId": 25 ,

"author": {

"id": 2 ,

"name": "Joana B"

},

"content": "Comentário do projeto de DAE!",

"visible": true,

"createdAt": "2025-10-27T18:00:00Z",

"updatedAt": "2025-10-27T18:45:00Z"

}

EP21. Listar Comentários de Publicação

Um utilizador autenticado consulta os comentários de uma publicação utilizando o protocolo

HTTP, método GET, para o endpoint:

/api/publications/{id}/comments

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 1 ,

"publicationId": 25 ,

"author": {

"id": 2 ,

"name": "Joana B"

},

"content": "Esta técnica pode revolucionar a nossa abordagem ao Projeto X!",

"visible": true,

"createdAt": "2025-10-27T18:00:00Z"

},

{

"id": 2 ,

"publicationId": 25 ,

"author": {

"id": 1 ,

"name": "João A"

},

"content": "Excelente descoberta!",

"visible": true,

"createdAt": "2025-10-27T18:30:00Z"

}

]

EP22. Editar Comentário

Um utilizador autenticado edita o seu próprio comentário utilizando o protocolo HTTP, método

PUT, para o endpoint:

/api/comments/{id}

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

“content": "Comentário editado do projeto de DAE!"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"publicationId": 25 ,

"author": {

"id": 2 ,

"name": "Joana B"

},

"content": “Comentário editado do projeto de DAE!",

"visible": true,

"createdAt": "2025-10-27T18:00:00Z",

"updatedAt": "2025-10-27T18:45:00Z"

}

EP23. Remover Comentário

Um utilizador autenticado remove o seu próprio comentário utilizando o protocolo HTTP, método

DELETE, para o endpoint:

/api/comments/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Comentário removido com sucesso"

}

EP24. Ocultar/Mostrar Comentário

Um utilizador autenticado como responsável ou administrador oculta ou mostra um comentário

utilizando o protocolo HTTP, método PATCH, para o endpoint:

/api/comments/{id}/visibility

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"visible": false

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"visible": false,

"updatedAt": "2025-10-27T19:00:00Z"

}

Tags

EP25. Listar Todas as Tags

Um utilizador autenticado consulta a lista de todas as tags utilizando o protocolo HTTP, método

GET, para o endpoint:

/api/tags

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 1 ,

"name": "Projeto X",

"description": "Publicações relacionadas com o Projeto X",

"visible": true,

"createdAt": "2025-01-10T10:00:00Z",

"publicationsCount": 45

},

{

"id": 2 ,

"name": "Machine Learning",

"description": "Técnicas e aplicações de ML",

"visible": true,

"createdAt": "2025-01-10T10:05:00Z",

"publicationsCount": 120

}

]

EP26. Criar Tag

Um utilizador autenticado como responsável ou administrador cria uma nova tag utilizando o

protocolo HTTP, método POST, para o endpoint:

/api/tags

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "Projeto Y",

"description": "Publicações relacionadas com o Projeto Y"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 5 ,

"name": "Projeto Y",

"description": "Publicações relacionadas com o Projeto Y",

"visible": true,

"createdAt": "2025-10-27T19:30:00Z",

"publicationsCount": 0

}

EP27. Editar Tag

Um utilizador autenticado como responsável ou administrador edita uma tag utilizando o protocolo

HTTP, método PUT, para o endpoint:

/api/tags/{id}

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "Projeto Y - Fase 2",

"description": "Publicações da segunda fase do Projeto Y"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 5 ,

"name": "Projeto Y - Fase 2",

"description": "Publicações da segunda fase do Projeto Y",

"visible": true,

"createdAt": "2025-10-27T19:30:00Z",

"updatedAt": "2025-10-27T20:00:00Z"

}

EP28. Remover Tag

Um utilizador autenticado como responsável ou administrador remove uma tag utilizando o

protocolo HTTP, método DELETE, para o endpoint:

/api/tags/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Tag removida com sucesso"

}

EP29. Adicionar Tag a Publicação

Um utilizador autenticado associa uma tag a uma publicação utilizando o protocolo HTTP, método

POST, para o endpoint:

/api/publications/{id}/tags

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"tagId": 5

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"publicationId": 25 ,

"tags": \[

{ "id": 1 , "name": "Projeto X" },

{ "id": 3 , "name": "Data Mining" },

{ "id": 5 , "name": "Projeto Y" }

]

}

EP30. Remover Tag de Publicação

Um utilizador autenticado (ou responsável/administrador) remove a associação de uma tag a uma

publicação utilizando o protocolo HTTP, método DELETE, para o endpoint:

/api/publications/{publicationId}/tags/{tagId}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Tag removida da publicação com sucesso"

}

Subscrições

EP31. Subscrever Tag

Um utilizador autenticado subscreve uma tag para receber notificações utilizando o protocolo

HTTP, método POST, para o endpoint:

/api/subscriptions/tags

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"tagId": 1

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"userId": 1 ,

"tag": {

"id": 1 , "name": "Projeto X"

},

"subscribedAt": "2025-10-27T20:30:00Z"

}

EP32. Cancelar Subscrição de Tag

Um utilizador autenticado cancela a subscrição de uma tag utilizando o protocolo HTTP, método

DELETE, para o endpoint:

/api/subscriptions/tags/{tagId}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Subscrição cancelada com sucesso"

}

EP33. Listar Subscrições Próprias

Um utilizador autenticado consulta as suas subscrições de tags utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/subscriptions/tags

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 1 ,

"tag": { "id": 1 , "name": "Projeto X", "publicationsCount": 45 },

"subscribedAt": "2025-10-27T20:30:00Z"

},

{

"id": 2 ,

"tag": { "id": 2 , "name": "Machine Learning", "publicationsCount": 120 },

"subscribedAt": "2025-09-15T11:00:00Z"

}

]

Notificações

EP34. Listar Notificações

Um utilizador autenticado consulta as suas notificações utilizando o protocolo HTTP, método GET,

para o endpoint:

/api/notifications

Parâmetros de query opcionais:

● unreadOnly: filtrar apenas não lidas (true/false)

● page: número da página

● size: tamanho da página

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"content": \[

{

"id": 1 ,

"userId": 1 ,

"type": "NEW_PUBLICATION_WITH_TAG",

"title": "Nova publicação na tag Projeto X",

"message": "Joana B adicionou uma nova publicação: 'Técnica Revolucionária'",

"relatedEntityType": "PUBLICATION",

"relatedEntityId": 25 ,

"read": false,

"createdAt": "2025-10-27T18:05:00Z"

},

{

"id": 2 ,

"userId": 1 ,

"type": "NEW_COMMENT_ON_TAG",

"title": "Novo comentário na tag Projeto X",

"message": "Manuel C comentou: 'ATENÇÃO: Erro grave detectado no artigo'",

"relatedEntityType": "COMMENT",

"relatedEntityId": 15 ,

"read": false,

"createdAt": "2025-10-27T16:00:00Z"

}

],

"totalElements": 25 ,

"unreadCount": 5

}

EP35.Marcar Notificação como Lida

Um utilizador autenticado marca uma notificação como lida utilizando o protocolo HTTP, método PATCH,

para o endpoint:

/api/notifications/{id}/read

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 1 ,

"read": true,

"readAt": "2025-10-27T21:00:00Z"

}

EP36. Marcar Todas as Notificações como Lidas

Um utilizador autenticado marca todas as suas notificações como lidas utilizando o protocolo

HTTP, método PATCH, para o endpoint:

/api/notifications/read-all

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Todas as notificações foram marcadas como lidas",

"count": 5

}

EP37.Remover Notificação

Um utilizador autenticado remove uma notificação utilizando o protocolo HTTP, método DELETE,

para o endpoint:

/api/notifications/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Notificação removida com sucesso"

}

Áreas Científicas

EP38. Listar Áreas Científicas

Um utilizador autenticado consulta a lista de áreas científicas utilizando o protocolo HTTP, método

GET, para o endpoint:

/api/scientific-areas

A resposta devolvida por este recurso segue o formato JSON:

\[

{

"id": 1 ,

"name": "Ciência de Dados",

"description": "Área dedicada à análise e interpretação de dados",

"publicationsCount": 250

},

{

"id": 2 ,

"name": "Ciência dos Materiais",

"description": "Estudo e desenvolvimento de novos materiais",

"publicationsCount": 180

},

{

"id": 3 ,

"name": "Inteligência Artificial",

"description": "Desenvolvimento de sistemas inteligentes",

"publicationsCount": 320

}

]

EP39. Criar Área Científica

Um utilizador autenticado como administrador cria uma nova área científica utilizando o protocolo

HTTP, método POST, para o endpoint:

/api/scientific-areas

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "Robótica",

"description": "Desenvolvimento e aplicação de sistemas robóticos"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 4 ,

"name": "Robótica",

"description": "Desenvolvimento e aplicação de sistemas robóticos",

"publicationsCount": 0 ,

"createdAt": "2025-10-27T21:30:00Z"

}

EP40. Editar Área Científica

Um utilizador autenticado como administrador edita uma área científica utilizando o protocolo

HTTP, método PUT, para o endpoint:

/api/scientific-areas/{id}

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"name": "Robótica Avançada",

"description": "Desenvolvimento de sistemas robóticos autónomos e inteligentes"

}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"id": 4 ,

"name": "Robótica Avançada",

"description": "Desenvolvimento de sistemas robóticos autónomos e inteligentes",

"publicationsCount": 0 ,

"updatedAt": "2025-10-27T22:00:00Z"

}

EP41.Remover Área Científica

Um utilizador autenticado como administrador remove uma área científica utilizando o protocolo

HTTP, método DELETE, para o endpoint:

/api/scientific-areas/{id}

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"message": "Área científica removida com sucesso"

}

Tipos de Publicação

EP42. Listar Tipos de Publicação

Um utilizador autenticado consulta os tipos de publicação disponíveis utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/publication-types

A resposta devolvida por este recurso segue o formato JSON:

\[

{

"code": "ARTICLE",

"name": "Artigo Científico",

"description": "Artigo científico com revisão por pares"

},

{

"code": "CONFERENCE",

"name": "Comunicação em Conferência",

"description": "Apresentação ou paper de conferência"

},

{

"code": "BOOK_CHAPTER",

"name": "Capítulo de Livro",

"description": "Capítulo em livro científico"

},

{

"code": "BOOK",

"name": "Livro Científico",

"description": "Livro completo de caráter científico"

},

{

"code": "TECHNICAL_REPORT",

"name": "Relatório Técnico",

"description": "Relatório técnico ou white paper"

},

{

"code": "PATENT",

"name": "Patente",

"description": "Documento de patente"

},

{

"code": "DATASET",

"name": "Dataset",

"description": "Conjunto de dados científicos"

},

{

"code": "SOFTWARE",

"name": "Software",

"description": "Software em código aberto"

},

{

"code": "AI_MODEL",

"name": "Modelo de IA",

"description": "Modelo de inteligência artificial"

},

{

"code": "DATABASE",

"name": "Base de Dados",

"description": "Base de dados científica"

},

{

"code": "THESIS_MASTER",

"name": "Tese de Mestrado",

"description": "Dissertação de mestrado"

},

{

"code": "THESIS_PHD",

"name": "Tese de Doutoramento",

"description": "Tese de doutoramento"

},

{

"code": "OUTREACH",

"name": "Artigo de Divulgação",

"description": "Artigo de divulgação científica"

}

]

Estatísticas e Dashboard

EP43. Obter Estatísticas Gerais

Um utilizador autenticado consulta estatísticas gerais da plataforma utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/statistics/overview

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"totalPublications": 1250 ,

"totalUsers": 85 ,

"totalComments": 3420 ,

"totalRatings": 5680 ,

"totalTags": 45 ,

"publicationsByType": {

"ARTICLE": 450 ,

"CONFERENCE": 320 ,

"BOOK_CHAPTER": 150 ,

"THESIS_PHD": 80 ,

"DATASET": 120 ,

"SOFTWARE": 90 ,

"OTHER": 40

},

"publicationsByArea": {

"Ciência de Dados": 450 ,

"Ciência dos Materiais": 380 ,

"Inteligência Artificial": 420

},

"recentActivity": {

"last7Days": {

"newPublications": 12 ,

"newComments": 45 ,

"newRatings": 78

},

"last30Days": {

"newPublications": 48 ,

"newComments": 180 ,

"newRatings": 320

}

}

}

EP44. Obter Estatísticas Pessoais

Um utilizador autenticado consulta as suas próprias estatísticas utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/statistics/personal

A resposta devolvida por este recurso segue o seguinte formato JSON:

{

"userId": 1 ,

"publicationsUploaded": 15 ,

"commentsCreated": 45 ,

"ratingsGiven": 120 ,

"tagsSubscribed": 5 ,

"averageRatingReceived": 4.3,

"totalRatingsReceived": 89 ,

"mostActiveTag": {

"id": 1 ,

"name": "Projeto X",

"activityCount": 25

},

"recentUploads": \[

{

"id": 25 ,

"title": "New Techniques in Data Science",

"uploadedAt": "2025-10-27T14:30:00Z"

}

]

}

EP45. Obter Publicações Mais Populares

Um utilizador autenticado consulta as publicações mais populares utilizando o protocolo HTTP,

método GET, para o endpoint:

/api/statistics/top-publications

Parâmetros de query opcionais:

● criteria: critério de popularidade (rating, comments, views)

● limit: número de resultados (default: 10)

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 5 ,

"title": "Deep Learning Revolution",

"averageRating": 4.9,

"ratingsCount": 150 ,

"commentsCount": 45 ,

"viewsCount": 1250 ,

"uploadedBy": {

"id": 3 ,

"name": "Carlos Mendes"

}

},

{

"id": 12 ,

"title": "Material Science Breakthrough",

"averageRating": 4.8,

"ratingsCount": 120 ,

"commentsCount": 38 ,

"viewsCount": 980

}

]

Pesquisa Avançada

EP46. Pesquisa Avançada de Publicações

Um utilizador autenticado realiza uma pesquisa avançada de publicações utilizando o protocolo

HTTP, método POST, para o endpoint:

/api/publications/advanced-search

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:

{

"keywords": \["machine learning", "neural networks"],

"authors": \["João Silva"],

"types": \["ARTICLE", "CONFERENCE"],

"scientificAreas": \[ 1 , 3 ],

"tags": \[ 1 , 2 ],

"yearFrom": 2020 ,

"yearTo": 2025 ,

"minRating": 4.0,

"hasComments": true,

"confidential": false,

"sortBy": "rating",

"order": "desc",

"page": 0 ,

"size": 20

}

Exportação de Dados

EP47. Exportar Publicações

Um utilizador autenticado exporta uma lista de publicações em formato CSV utilizando o protocolo

HTTP, método GET, para o endpoint:

/api/publications/export

Parâmetros de query opcionais (mesmos da listagem):

● format: formato de exportação (csv, json)

● Filtros de pesquisa

EP48. Exportar Relatório de Atividade

Um utilizador autenticado como administrador exporta um relatório de atividade utilizando o

protocolo HTTP, método GET, para o endpoint:

/api/reports/activity

Parâmetros de query:

● userIds: lista de IDs de utilizadores (opcional)

● dateFrom: data inicial

● dateTo: data final

● format: formato (csv, json, pdf)

Conteúdo Oculto

EP49. Listar Publicações Ocultas

Um utilizador autenticado como responsável ou administrador consulta publicações ocultas

utilizando o protocolo HTTP, método GET, para o endpoint:

/api/admin/hidden-publications

EP50. Listar Comentários Ocultos

Um utilizador autenticado como responsável ou administrador consulta comentários ocultos

utilizando o protocolo HTTP, método GET, para o endpoint:

/api/admin/hidden-comments

A resposta devolvida por este recurso segue o seguinte formato JSON:

\[

{

"id": 15 ,

"publicationId": 8 ,

"publicationTitle": "Artigo Controverso",

"author": {

"id": 5 ,

"name": "Pedro Alves"

},

"content": "Comentário inapropriado...",

"visible": false,

"hiddenBy": {

"id": 3 ,

"name": "Maria Santos"

},

"hiddenAt": "2025-10-25T10:00:00Z",

"createdAt": "2025-10-24T15:30:00Z"

}

]

EP51. Listar Tags Ocultas

Um utilizador autenticado como responsável ou administrador consulta tags ocultas utilizando o

protocolo HTTP, método GET, para o endpoint:

/api/admin/hidden-tags
