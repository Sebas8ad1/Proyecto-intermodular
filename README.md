# EstudiaFP — primera versión funcional

Aplicación de escritorio Java para gestionar asignaturas, temas y sesiones de estudio.

## Requisitos
- JDK 25
- IntelliJ IDEA
- Maven (IntelliJ puede usar el Maven integrado)

## Ejecutar desde IntelliJ
1. Abre la carpeta `Proyectointermodular`.
2. Espera a que Maven descargue las dependencias.
3. Abre `src/main/java/es/glaux/MainApp.java`.
4. Ejecuta el método `main` o la clase `MainApp`.

También puedes ejecutar desde la terminal, situada en la carpeta del proyecto:

```bash
mvn clean javafx:run
```

## Datos
La base de datos SQLite se crea automáticamente en `~/.estudiafp/estudiafp.db` (en Windows, dentro de la carpeta de usuario). Los datos permanecen al cerrar la aplicación.

## Funciones incluidas
- Crear y eliminar asignaturas.
- Crear y eliminar temas asociados a una asignatura.
- Registrar y eliminar sesiones de estudio con fecha, duración y notas.
- Validación básica de campos y persistencia local.

Esta es una primera versión. La edición de registros, las estadísticas de dominio, las evaluaciones y los informes quedan para siguientes iteraciones.
