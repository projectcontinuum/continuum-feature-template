# Hello Java Node

Appends a greeting column to every row in the input table. This is the **Java** version of the Hello World example.

## What It Does

For each row, the node reads a value from a configurable **name column** and produces a greeting string like `Hello, Alice!`. The result is written to a new output column alongside all original columns.

If the name column is missing from a row, the greeting defaults to `Hello, World!`.

## Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **Name Column** | string | `name` | The column to read the name from |
| **Greeting Prefix** | string | `Hello` | The word before the comma |
| **Output Column Name** | string | `greeting` | Name of the new column added to each row |

## Example

**Input:**

| name | age |
|------|-----|
| Alice | 30 |
| Bob | 25 |

**Output (with defaults):**

| name | age | greeting |
|------|-----|----------|
| Alice | 30 | Hello, Alice! |
| Bob | 25 | Hello, Bob! |

## Notes

This node is identical in behavior to the Kotlin `HelloWorldNodeModel` — it demonstrates that Continuum nodes can be written in plain Java using the `org.projectcontinuum.feature-java` Gradle plugin.
