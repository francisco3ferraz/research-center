Especificação API REST

DAE-2025-26-API-Projeto

Estudantes envolvidos:
Francisco Ferraz - 2023122475
Ivan Lourenço - 2023122718
Luis Oneto - 2022110707

--------------------------------------------------------------------------------

AUTENTICAÇÃO

EP01. Login
Um utilizador autentica-se no sistema utilizando o protocolo HTTP, método POST, para o endpoint:
/api/auth/login

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:
{
  "username": "joao.silva",
  "password": "password123"
}

A resposta devolvida por este recurso segue o seguinte formato JSON:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

EP02. Obter Utilizador Atual
Um utilizador autenticado obtém os seus dados utilizando o protocolo HTTP, método GET, para o endpoint:
/api/auth/user

A resposta devolvida por este recurso segue o seguinte formato JSON:
{
  "id": 1,
  "username": "joao.silva",
  "name": "João Silva",
  "email": "joao.silva@centro.pt",
  "role": "COLABORADOR",
  "active": true
}

EP03. Alterar Password
Um utilizador autenticado altera a sua password utilizando o protocolo HTTP, método POST ou PATCH, para o endpoint:
/api/auth/set-password

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:
{
  "oldPassword": "oldPassword123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}

--------------------------------------------------------------------------------

RECUPERAÇÃO DE PASSWORD

EP04. Pedir Reset de Password
Um utilizador solicita a recuperação de password via email utilizando o protocolo HTTP, método POST, para o endpoint:
/api/auth/request-reset

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:
{
  "email": "joao.silva@centro.pt"
}

EP05. Validar Token de Reset
O sistema valida se um token de reset é válido utilizando o protocolo HTTP, método GET, para o endpoint:
/api/auth/validate-token?token={token}

EP06. Redefinir Password
Um utilizador define uma nova password utilizando um token válido, via protocolo HTTP, método POST, para o endpoint:
/api/auth/reset-password

O corpo do pedido recebido por este recurso segue o seguinte formato JSON:
{
  "token": "reset-token-guid",
  "newPassword": "newPass123",
  "confirmPassword": "newPass123"
}

--------------------------------------------------------------------------------

UTILIZADORES

EP07. Listar Utilizadores
Um administrador lista todos os utilizadores utilizando o protocolo HTTP, método GET, para o endpoint:
/api/users

EP08. Pesquisar Utilizadores
Um administrador pesquisa utilizadores por nome utilizando o protocolo HTTP, método GET, para o endpoint:
/api/users/lookup?q={nome}

EP09. Consultar Utilizador
Um utilizador consulta detalhes de um utilizador específico (ou a si mesmo) utilizando o protocolo HTTP, método GET, para o endpoint:
/api/users/{id}

EP10. Criar Utilizador
Um administrador cria um novo utilizador utilizando o protocolo HTTP, método POST, para o endpoint:
/api/users

O corpo do pedido segue o formato JSON:
{
  "username": "novo.user",
  "password": "senha123",
  "name": "Novo User",
  "email": "novo@centro.pt",
  "role": "COLABORADOR"
}

EP11. Editar Utilizador
Um administrador edita um utilizador existente utilizando o protocolo HTTP, método PUT, para o endpoint:
/api/users/{id}

EP12. Alterar Estado (Ativar/Desativar)
Um administrador altera o estado de um utilizador utilizando o protocolo HTTP, método PATCH, para o endpoint:
/api/users/{id}/status

{ "active": false }

EP13. Editar Perfil Próprio
Um utilizador autenticado atualiza o seu próprio perfil utilizando o protocolo HTTP, método PUT, para o endpoint:
/api/users/profile

EP14. Remover Utilizador
Um administrador remove um utilizador utilizando o protocolo HTTP, método DELETE, para o endpoint:
/api/users/{id}

EP15. Consultar Histórico de Atividade (Admin/Próprio)
Um utilizador consulta o seu histórico (ou admin consulta de outrem) utilizando o protocolo HTTP, método GET, para o endpoint:
/api/users/{id}/activity?page=0&size=20

EP16. Consultar Minha Atividade
Um utilizador consulta o seu próprio histórico de atividade utilizando o protocolo HTTP, método GET, para o endpoint:
/api/activity-logs/my?page=0&size=20

--------------------------------------------------------------------------------

PUBLICAÇÕES

EP17. Listar Publicações
Qualquer utilizador (autenticado ou convidado) lista publicações públicas utilizando o protocolo HTTP, método GET, para o endpoint:
/api/publications

Filtros: search, areaScientific, tag, dateFrom, dateTo, sortBy, page, size.

EP18. Consultar Publicação
Um utilizador consulta os detalhes de uma publicação utilizando o protocolo HTTP, método GET, para o endpoint:
/api/publications/{id}

EP19. Criar Publicação (Simples)
Um colaborador cria uma publicação (metadados apenas) utilizando o protocolo HTTP, método POST, para o endpoint:
/api/publications

EP20. Criar Publicação (Multipart/Upload)
Um colaborador cria uma publicação + ficheiro utilizando o protocolo HTTP, método POST, para o endpoint:
/api/publications (Content-Type: multipart/form-data)

EP21. Editar Publicação
O autor (ou responsável/admin) edita uma publicação utilizando o protocolo HTTP, método PUT, para o endpoint:
/api/publications/{id}

EP22. Remover Publicação
Um administrador remove uma publicação utilizando o protocolo HTTP, método DELETE, para o endpoint:
/api/publications/{id}

EP23. Download de Ficheiro
Um utilizador faz download do ficheiro associado a uma publicação utilizando o protocolo HTTP, método GET, para o endpoint:
/api/publications/{id}/file

EP24. Upload de Ficheiro (Posterior)
O autor faz upload/substituição do ficheiro de uma publicação existente utilizando o protocolo HTTP, método POST, para o endpoint:
/api/publications/{id}/file (Multipart)

EP25. Gerir Tags de Publicação
- Adicionar: POST /api/publications/{id}/tags/{tagId}
- Remover: DELETE /api/publications/{id}/tags/{tagId}

EP26. Exportar Publicações
Um utilizador exporta a lista de publicações utilizando o protocolo HTTP, método GET, para o endpoint:
/api/publications/export?format=csv (ou json)

EP27. Pesquisa Avançada
Um utilizador realiza uma pesquisa complexa utilizando o protocolo HTTP, método POST, para o endpoint:
/api/publications/advanced-search

EP28. Listar Tipos de Publicação
Qualquer utilizador lista os tipos de publicação disponíveis utilizando o protocolo HTTP, método GET, para o endpoint:
/api/publication-types

--------------------------------------------------------------------------------

ÁREAS CIENTÍFICAS

EP29. Listar Áreas Científicas
Qualquer utilizador lista as áreas científicas utilizando o protocolo HTTP, método GET, para o endpoint:
/api/scientific-areas

EP30. Criar Área Científica
Um administrador cria, endpoint POST: /api/scientific-areas

EP31. Editar Área Científica
Um administrador edita, endpoint PUT: /api/scientific-areas/{id}

EP32. Remover Área Científica
Um administrador remove, endpoint DELETE: /api/scientific-areas/{id}

--------------------------------------------------------------------------------

ADMINISTRAÇÃO (PAINEL OCULTO & REPORTS)

EP33. Listar Publicações Ocultas
Um Responsável/Admin lista publicações ocultas, endpoint GET:
/api/admin/hidden-publications

EP34. Listar Comentários Ocultos
Um Responsável/Admin lista comentários ocultos, endpoint GET:
/api/admin/hidden-comments

EP35. Listar Tags Ocultas
Um Responsável/Admin lista tags ocultas, endpoint GET:
/api/admin/hidden-tags

EP36. Exportar Relatório de Atividade
Um admin exporta o relatório de atividades de utilizadores para CSV, JSON ou PDF, endpoint GET:
/api/reports/activity?userIds=1,2&dateFrom=...&format=pdf

--------------------------------------------------------------------------------

ESTATÍSTICAS

EP37. Estatísticas Gerais (Overview)
Um utilizador autenticado consulta estatísticas gerais do sistema, endpoint GET:
/api/statistics/overview

EP38. Estatísticas Pessoais
Um utilizador autenticado consulta as suas estatísticas pessoais, endpoint GET:
/api/statistics/personal

EP39. Publicações Populares (Top)
Um utilizador autenticado consulta as publicações mais populares, endpoint GET:
/api/statistics/top-publications?criteria=rating&limit=10

--------------------------------------------------------------------------------

TAGS (ETIQUETAS)

EP40. Listar Tags
GET /api/tags

EP41. Criar Tag (Admin/Resp)
POST /api/tags

EP42. Editar Tag (Admin/Resp)
PUT /api/tags/{id}

EP43. Remover Tag (Admin/Resp)
DELETE /api/tags/{id}

--------------------------------------------------------------------------------

COMENTÁRIOS

EP44. Editar Comentário
PUT /api/comments/{id}

EP45. Remover Comentário
DELETE /api/comments/{id}

EP46. Moderação (Visibilidade)
PATCH /api/comments/{id}/visibility

--------------------------------------------------------------------------------

INTELIGÊNCIA ARTIFICIAL (IA)

EP47. Gerar Resumo
POST /api/ai/generate-summary

EP48. Estado do Serviço
GET /api/ai/status

--------------------------------------------------------------------------------

NOTIFICAÇÕES

EP49. Listar Notificações
GET /api/notifications

EP50. Marcar como Lida
PATCH /api/notifications/{id}/read

EP51. Marcar Todas como Lidas
PATCH /api/notifications/read-all

EP52. Remover Notificação
DELETE /api/notifications/{id}

--------------------------------------------------------------------------------

SUBSCRIÇÕES

EP53. Subscrever Tag
POST /api/subscriptions/tags

EP54. Remover Subscrição
DELETE /api/subscriptions/tags/{tagId}

EP55. Listar Subscrições
GET /api/subscriptions/tags
