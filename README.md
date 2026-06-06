# Final Project Compiler — Base Project

Proyecto base para el examen final de Compiladores.

## Requisitos

- Java 8 o superior.
- `javac` y `java` disponibles en consola.
- No requiere Maven, Gradle ni dependencias externas.

## Objetivo del proyecto

El proyecto ya incluye la base mínima de un front-end de compilador SQL:

- Lexer / tokenización.
- Parser básico para `SELECT <columnas> FROM <tabla>`.
- AST mínimo.
- Validación semántica contra un schema fijo.
- Diagnósticos.
- Tests manuales.

La tarea del examen es completar el soporte para cláusulas `WHERE`.

## Validar que el proyecto base está listo

Desde la raíz del proyecto:

```bash
./run-tests.sh
```

Resultado esperado inicial:

```text
PASS valid SELECT without WHERE
PASS unknown projection column
FAIL TODO WHERE AST and trace -> WHERE válido no debe producir errores: SYNTACTIC_EXPECTED_WHERE_OPERAND|1:41|Soporte WHERE pendiente: implemente el AST de condiciones.

FAIL TODO WHERE unknown column diagnostic -> Debe reportar SEMANTIC_UNKNOWN_WHERE_COLUMN
FAIL TODO WHERE type mismatch diagnostic -> Debe reportar SEMANTIC_TYPE_MISMATCH
PASS TODO WHERE missing operand diagnostic
Passed: 3 Failed: 3
```

Ese resultado significa que el proyecto base **compila y está listo para usar**. Los 3 fallos son esperados porque corresponden al módulo `WHERE` que debés implementar.

## Restricciones

- Mantener Java 8.
- No agregar librerías externas.
- No modificar los tests para ocultar fallos.
- Implementar la solución dentro de `src/`.

## Evidencia

Link del Drive: https://drive.google.com/drive/folders/1uHiXbFD0gdx75C2WmTobh5hJ0nFB2rab?usp=sharing
