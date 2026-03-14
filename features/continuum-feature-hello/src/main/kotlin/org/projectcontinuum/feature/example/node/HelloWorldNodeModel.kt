package org.projectcontinuum.feature.example.node

import org.projectcontinuum.core.commons.model.ContinuumWorkflowModel
import org.projectcontinuum.core.commons.node.ProcessNodeModel
import org.projectcontinuum.core.commons.protocol.progress.NodeProgressCallback
import org.projectcontinuum.core.commons.utils.NodeInputReader
import org.projectcontinuum.core.commons.utils.NodeOutputWriter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.stereotype.Component
import org.projectcontinuum.core.commons.annotation.ContinuumNode

/**
 * Hello World Node — a minimal example Continuum node.
 *
 * Reads every row from the input table and appends a new "greeting" column
 * whose value is "Hello, {name}!" where {name} comes from a configurable
 * source column (default: "name"). If the source column is missing the
 * greeting falls back to "Hello, World!".
 */
@ContinuumNode
class HelloWorldNodeModel : ProcessNodeModel() {

    private val logger = LoggerFactory.getLogger(HelloWorldNodeModel::class.java)
    private val objectMapper = ObjectMapper()

    // ── Ports ────────────────────────────────────────────────

    final override val inputPorts = mapOf(
        "input" to ContinuumWorkflowModel.NodePort(
            name = "Input Table",
            contentType = APPLICATION_OCTET_STREAM_VALUE
        )
    )

    final override val outputPorts = mapOf(
        "output" to ContinuumWorkflowModel.NodePort(
            name = "Output Table",
            contentType = APPLICATION_OCTET_STREAM_VALUE
        )
    )

    // ── Categories ───────────────────────────────────────────

    override val categories = listOf("Examples")

    // ── Properties JSON Schema ───────────────────────────────

    private val propertiesSchema: Map<String, Any> = objectMapper.readValue(
        """
        {
          "type": "object",
          "properties": {
            "nameColumn": {
              "type": "string",
              "title": "Name Column",
              "description": "Column to read the name from. Falls back to 'Hello, World!' if the column is missing.",
              "default": "name"
            },
            "greeting": {
              "type": "string",
              "title": "Greeting Prefix",
              "description": "The greeting word to use before the name.",
              "default": "Hello"
            },
            "outputColumn": {
              "type": "string",
              "title": "Output Column Name",
              "description": "Name of the new column that will contain the greeting.",
              "default": "greeting"
            }
          },
          "required": []
        }
        """.trimIndent(),
        object : TypeReference<Map<String, Any>>() {}
    )

    // ── Node Icon (SVG) ──────────────────────────────────────

    private val icon = """
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8h1a4 4 0 0 1 0 8h-1"/>
          <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/>
          <line x1="6" y1="1" x2="6" y2="4"/>
          <line x1="10" y1="1" x2="10" y2="4"/>
          <line x1="14" y1="1" x2="14" y2="4"/>
        </svg>
    """.trimIndent()

    // ── Metadata ─────────────────────────────────────────────

    override val metadata = ContinuumWorkflowModel.NodeData(
        title = "Hello World",
        description = "Appends a greeting column to each row. A minimal example node.",
        icon = icon,
        nodeModel = this::class.java.name,
        inputs = inputPorts,
        outputs = outputPorts,
        propertiesSchema = propertiesSchema,
        properties = mapOf(
            "nameColumn" to "name",
            "greeting" to "Hello",
            "outputColumn" to "greeting"
        )
    )

    // ── Execution ────────────────────────────────────────────

    override fun execute(
        properties: Map<String, Any>?,
        inputs: Map<String, NodeInputReader>,
        nodeOutputWriter: NodeOutputWriter,
        nodeProgressCallback: NodeProgressCallback
    ) {
        val nameColumn = properties?.get("nameColumn")?.toString() ?: "name"
        val greetingPrefix = properties?.get("greeting")?.toString() ?: "Hello"
        val outputColumn = properties?.get("outputColumn")?.toString() ?: "greeting"

        val inputReader = inputs["input"]
            ?: throw IllegalStateException("Input port 'input' is not connected")

        val outputPortWriter = nodeOutputWriter.createOutputPortWriter("output")

        logger.info("HelloWorldNode: nameColumn=$nameColumn, greeting=$greetingPrefix, outputColumn=$outputColumn")

        val totalRows = inputReader.getRowCount()
        var rowNumber = 0L

        inputReader.use { reader ->
            outputPortWriter.use { writer ->
                while (true) {
                    val row = reader.read() ?: break
                    rowNumber++

                    // Read the name value from the configured column, fall back to "World"
                    val nameValue = row[nameColumn]?.toString() ?: "World"
                    val greetingValue = "$greetingPrefix, $nameValue!"

                    // Write original columns plus the new greeting column
                    val outputRow = row.toMutableMap()
                    outputRow[outputColumn] = greetingValue
                    writer.write(rowNumber, outputRow)

                    // Report progress
                    if (totalRows > 0) {
                        nodeProgressCallback.report((rowNumber * 100 / totalRows).toInt())
                    }
                }
            }
        }

        nodeProgressCallback.report(100)
        logger.info("HelloWorldNode: processed $rowNumber rows")
    }
}
