# Validação da entrega com H2

Executada em 10/09/2026 após substituir o banco externo por H2 em memória:

- Maven 3.9.11: `verify` concluído com três testes de integração, zero falhas e sem serviço de banco externo. Compilação com target Java 21 usando o JDK 25 instalado.
- Flyway criou o schema no H2 e Hibernate validou o mapeamento.
- CRUD completo, campos inválidos e limites de paginação verificados.
- `actionlint` 1.7.7: os três workflows passaram na análise estática.
- kubectl 1.34.1: overlays local/UAT/produção renderizados após remover o banco externo e configurar uma réplica com estratégia Recreate.

A imagem Docker não foi reconstruída nesta alteração. GitHub Actions, autenticação OIDC, dry-run no servidor e deploy EKS dependem da conta e do repositório configurados. Não foi feita publicação na nuvem.
