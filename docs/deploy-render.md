# Deploy da demo no Render + MongoDB Atlas

Demo pública, gratuita e **somente leitura** da API. O Render roda o `Dockerfile` do
repositório com `SPRING_PROFILES_ACTIVE=demo`; o banco fica no MongoDB Atlas (M0).

Tempo estimado: 20 a 30 minutos. Custo: R$ 0.

## O que o perfil `demo` faz

- Mesma postura de `prod`: MongoDB real (`MONGODB_URI` obrigatória), `JWT_SECRET` obrigatório,
  `X-Forwarded-*` respeitado e health sem detalhes.
- Liga `DemoReadOnlyFilter`: `POST`/`PUT`/`PATCH`/`DELETE` em `/api/v1/**` recebem
  `403` com `code: READ_ONLY_DEMO`. Login, refresh, logout, Swagger e leituras funcionam.
- Na primeira subida com banco vazio, `DemoDataSeeder` cria 7 usuários de exemplo
  (6 ativos, 1 inativo) e o admin de demo.
- Não usa `APP_BOOTSTRAP_ADMIN_*`: a demo tem seeder próprio.

## 1. MongoDB Atlas

1. Crie uma conta em <https://www.mongodb.com/cloud/atlas/register> e um cluster
   **M0 (Free)**. Região mais próxima do Render escolhido (ex.: AWS `us-east-1`/Oregon).
2. **Database Access** → *Add New Database User*:
   - usuário: `api-usuarios-demo`
   - senha: gere uma forte e guarde (ela vai na URL)
   - privilégio: *Read and write to any database*
3. **Network Access** → *Add IP Address* → `0.0.0.0/0`.
   O Render free não tem IP fixo de saída; liberar tudo é o caminho simples. A proteção
   real fica no usuário/senha do banco e no fato de a API ser somente leitura.
4. **Connect** → *Drivers* → copie a string e **coloque o nome do banco**:
   ```text
   mongodb+srv://api-usuarios-demo:SENHA@cluster0.xxxxx.mongodb.net/api-usuarios-demo?retryWrites=true&w=majority
   ```
   Se a senha tiver caracteres especiais, use a versão URL-encoded.

## 2. Render

1. Crie a conta em <https://render.com> e conecte o GitHub (`EliasCamposDEV`).
2. **New** → **Blueprint** → selecione `api-usuarios`.
   O Render lê o `render.yaml` e propõe o serviço `api-usuarios-demo`.
3. Preencha a variável secreta:
   | Variável | Valor |
   |---|---|
   | `MONGODB_URI` | string do Atlas, com `/api-usuarios-demo` no caminho |

   `JWT_SECRET` é gerado pelo Render. `SPRING_PROFILES_ACTIVE=demo`, `DEMO_ADMIN_EMAIL`
   e `DEMO_ADMIN_PASSWORD` já vêm do blueprint (senha pública de propósito, porque a
   demo é somente leitura).
4. **Apply** e aguarde o build (primeiro build baixa as dependências Maven: 5–10 min).
5. A URL fica no topo do serviço, algo como
   `https://api-usuarios-demo.onrender.com`. Se o nome já estiver em uso, o Render
   adiciona um sufixo — ajuste os links do README nesse caso.

Alternativa manual (sem Blueprint): **New** → **Web Service** → repositório →
Runtime **Docker** → Plan **Free** → Health Check Path `/actuator/health` →
as mesmas variáveis acima.

## 3. Validar

```bash
BASE=https://api-usuarios-demo.onrender.com

curl -s $BASE/actuator/health
# {"status":"UP"}

curl -s -o /dev/null -w "%{http_code}\n" $BASE/swagger-ui.html
# 200

TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@eliascampos.dev","password":"SUA_SENHA"}' \
  | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -s $BASE/api/v1/users -H "Authorization: Bearer $TOKEN" | head -c 400
# {"content":[{"id":"...","name":"Ana Beatriz Lima",...

curl -s -X POST $BASE/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste","email":"teste@exemplo.com","password":"senha12345","role":"USER"}'
# 403 {"code":"READ_ONLY_DEMO", "title":"Demo somente leitura", ...}
```

No PowerShell, use `Invoke-RestMethod` ou importe a coleção do Postman em
[`postman/`](../postman) e troque `baseUrl` pela URL do Render.

## 4. Resetar os dados da demo

No Atlas: **Browse Collections** → banco `api-usuarios-demo` → coleção `usuarios` →
*Drop collection*. No próximo start (ou restart do serviço no Render) o seeder recria
os 7 usuários. Os dados também somem se você apagar o cluster.

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Build falha em `mvn package` | Java 21 é obrigatório; o `Dockerfile` já usa `eclipse-temurin`. Veja o log do Render. |
| App sobe e cai com erro de `MONGODB_URI` | Variável ausente ou string sem o nome do banco. |
| `401` no login da demo | Senha diferente da definida em `DEMO_ADMIN_PASSWORD` (o seeder só roda em banco vazio). |
| `503`/timeout no primeiro acesso | Free tier hiberna após ~15 min ociosos; o primeiro request leva 30–60 s. |
| `403 READ_ONLY_DEMO` em tudo | Esperado para escrita; leitura e login continuam funcionando. |
| `ReadOnlyDemo` mas quero testar escrita | Rode local com o perfil `dev` (Mongo embutido) — a demo pública é só leitura por decisão de segurança. |

## Segurança e limites

- Banco separado da produção e sem dados reais: os usuários são fictícios.
- Credenciais de demo são públicas de propósito; a única coisa que permitem é ler.
- `JWT_SECRET` gerado pelo Render; rotacione recriando a variável.
- Rate limit de login é em memória, por instância (5 tentativas/15 min).
- Free tier: 512 MB de RAM, hibernação por inatividade e sem SLA.
