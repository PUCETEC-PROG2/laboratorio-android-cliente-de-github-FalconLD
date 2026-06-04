# Laboratorio Android. Cliente de GitHub

## Datos del estudiante
- Leonardo Falconi
- Examen Parcial 1 — API REST con Retrofit

## Configuración local

Añade tu token de GitHub en `local.properties` (no se sube al repositorio):

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
GITHUB_TOKEN=ghp_tu_token_aqui
```

El token debe tener permisos `repo` para crear, editar y eliminar repositorios.

## Funcionalidades implementadas

- Lista de repositorios propios, colaborativos y de organizaciones (`affiliation=owner,collaborator,organization_member`)
- Editar y eliminar solo en repos con permiso `push` o `admin` (botones deshabilitados en solo lectura)
- Lista de repositorios con `RecyclerView` y diseño XML personalizado por ítem
- `RepoListFragment` y `RepoFormFragment`
- FAB para abrir el formulario de creación
- Botones vectoriales de edición y eliminación en cada ítem
- Formulario con `EditText` (nombre bloqueado en modo edición)
- Retrofit: GET, POST, PATCH y DELETE contra la API de GitHub

## Tecnologías

- Kotlin, Android SDK, XML, View Binding
- RecyclerView, Fragments, Material Design
- Retrofit, Gson, OkHttp, Glide
