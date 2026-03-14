# Hello World Node

Appends a greeting column to every row in the input table.

## What It Does

For each row, the node reads a value from a configurable **name column** and produces a greeting string like `Hello, Alice!`. The result is written to a new output column alongside all original columns.

If the name column is missing from a row, the greeting defaults to `Hello, World!`.

## Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| **Name Column** | string | `name` | The column to read the name from |
| **Greeting Prefix** | string | `Hello` | The word before the comma (e.g. `Hi`, `Welcome`) |
| **Output Column Name** | string | `greeting` | Name of the new column added to each row |

## Ports

| Port | Direction | Content Type | Description |
|------|-----------|-------------|-------------|
| Input Table | Input | Parquet | Any tabular data |
| Output Table | Output | Parquet | Input data + greeting column |

## Example

**Input:**

| name | age |
|------|-----|
| Alice | 30 |
| Bob | 25 |

**Configuration:** `nameColumn=name`, `greeting=Hello`, `outputColumn=greeting`

**Output:**

| name | age | greeting |
|------|-----|----------|
| Alice | 30 | Hello, Alice! |
| Bob | 25 | Hello, Bob! |

## Tips

- Set **Greeting Prefix** to `Welcome` for onboarding workflows
- Use any column as the name source — it doesn't have to be called "name"
- Missing name values produce `Hello, World!` instead of errors
