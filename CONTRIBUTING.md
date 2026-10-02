# Contribuir

## Flujo recomendado

1. Crear una rama para el cambio:

```bash
git checkout -b feature/nombre-del-cambio
```

2. Realizar cambios pequeños y verificables.
3. Ejecutar:

```bash
mvn clean verify
```

4. Usar mensajes de commit descriptivos, por ejemplo:

```text
feat: agrega consulta de viajes
fix: corrige persistencia de asignaciones
test: agrega pruebas de viajes
docs: actualiza referencia de API
```

5. Abrir un Pull Request hacia `main`.

## Datos locales

No subir bases de datos, credenciales, archivos `.env`, JAR generados ni respaldos.
