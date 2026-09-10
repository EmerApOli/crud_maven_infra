# CRUD Maven · Kubernetes · GitHub Actions

Projeto Java 21 / Spring Boot 3.5 com CRUD de produtos, H2 em memória, validação e migrações Flyway. Pipeline de integração e pipeline de deploy independentes. AWS EKS é a implementação de referência; não há criação automática de recursos pagos.

## Estrutura

```text
.github/workflows/ci.yaml      # PR: verifica; main: verifica e publica no GHCR
.github/workflows/cd.yaml      # Entrada manual: UAT ou produção
.github/workflows/deploy.yaml  # Workflow reutilizável: autenticação e kubectl
infra/infra.yaml               # Entrada Kustomize (kubectl apply -k infra)
infra/base/infra.yaml          # Deployment e Service base
infra/overlays/local/          # Kubernetes local, sem banco externo
infra/overlays/uat/            # Homologação
infra/overlays/production/     # Produção
infra/bootstrap/rbac.yaml      # Permissões do deploy, aplicadas pelo administrador
compose.yaml                  # Execução local simplificada
src/                          # CRUD, testes de integração e migração SQL
```

São **dois ambientes de nuvem**: `uat` (homologação) e `production` (produção). Os arquivos YAML principais de automação ficam versionados em `.github/workflows`; o GitHub executa os comandos `kubectl` do workflow reutilizável. O arquivo `infra/infra.yaml` referencia os manifests base da aplicação no Kubernetes, não provisiona VPC, EKS ou banco. O modelo é deploy direto via Actions; não usa Argo CD/Flux.

## Executar localmente

Pré-requisito: Java 21 (Docker com Compose é opcional); o Maven Wrapper baixa Maven 3.9.11.

```bash
./mvnw -B verify
./mvnw spring-boot:run
```

Outra opção, após `./mvnw verify`: `docker compose up --build -d`. O banco H2 roda dentro da aplicação: os dados são perdidos a cada reinício. Não é necessário configurar senha, volume ou banco externo.

```bash
curl -i -X POST localhost:8080/api/products \
  -H 'Content-Type: application/json' -d '{"name":"Teclado","price":100.00}'
curl 'localhost:8080/api/products?page=0&size=20'
curl localhost:8080/api/products/1
curl -i -X PUT localhost:8080/api/products/1 \
  -H 'Content-Type: application/json' -d '{"name":"Mouse","price":50.00}'
curl -i -X DELETE localhost:8080/api/products/1
curl localhost:8080/actuator/health/readiness
```

Criação retorna 201 e `Location`; atualização/consulta 200; exclusão 204; ID inexistente 404; campos inválidos 400. A listagem limita o tamanho da página a 100. Testes `*IT` são executados com `verify` usando H2 em memória, sem serviços externos.

## Kubernetes local (Kind)

Instale Kind e kubectl. Empacote a aplicação com `./mvnw verify`.

```bash
kind create cluster --name crud
docker build -t crud-app:local .
kind load docker-image crud-app:local --name crud
kubectl --context kind-crud create namespace crud-local
kubectl --context kind-crud apply -k infra/overlays/local
kubectl --context kind-crud -n crud-local rollout status deployment/crud --timeout=300s
kubectl --context kind-crud -n crud-local port-forward service/crud 8080:80
```

Todos os ambientes usam uma réplica e estratégia `Recreate`, evitando instâncias com bancos em memória independentes. Há indisponibilidade durante o deploy e perda dos dados ao reiniciar. O ambiente chamado `production` serve, nesta etapa, para testar o pipeline com dados descartáveis. Ao reconstruir a tag local, carregue a imagem novamente e execute `kubectl --context kind-crud -n crud-local rollout restart deployment/crud`.

## Publicar e configurar CI/CD

Siga [o guia de implantação](docs/CI-CD.md), que cobre publicação no GitHub, OIDC, RBAC, banco, registry, aprovação, promoção e rollback.

O CRUD não implementa autenticação de usuários; o Service é interno (`ClusterIP`). Antes de disponibilizá-lo externamente, implemente autenticação/autorização e configure entrada HTTPS. O scaffold inclui controles de entrega, mas operar em produção também requer backups testados, monitoramento e a configuração real da infraestrutura.
# crud_maven_infra
