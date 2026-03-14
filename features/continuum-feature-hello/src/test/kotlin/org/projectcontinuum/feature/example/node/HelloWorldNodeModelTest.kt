package org.projectcontinuum.feature.example.node

import org.projectcontinuum.core.commons.protocol.progress.NodeProgressCallback
import org.projectcontinuum.core.commons.utils.NodeInputReader
import org.projectcontinuum.core.commons.utils.NodeOutputWriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HelloWorldNodeModelTest {

    private lateinit var node: HelloWorldNodeModel
    private lateinit var mockOutputWriter: NodeOutputWriter
    private lateinit var mockPortWriter: NodeOutputWriter.OutputPortWriter
    private lateinit var mockProgressCallback: NodeProgressCallback

    @BeforeEach
    fun setup() {
        node = HelloWorldNodeModel()
        mockOutputWriter = mock()
        mockPortWriter = mock()
        mockProgressCallback = mock()

        whenever(mockOutputWriter.createOutputPortWriter("output")).thenReturn(mockPortWriter)
    }

    @Test
    fun `metadata is correctly configured`() {
        assertEquals("Hello World", node.metadata.title)
        assertNotNull(node.metadata.icon)
        assertEquals(1, node.inputPorts.size)
        assertEquals(1, node.outputPorts.size)
        assertTrue(node.inputPorts.containsKey("input"))
        assertTrue(node.outputPorts.containsKey("output"))
    }

    @Test
    fun `categories contains Examples`() {
        assertTrue(node.categories.contains("Examples"))
    }

    @Test
    fun `execute appends greeting column with name`() {
        val rows = listOf(
            mapOf<String, Any>("name" to "Alice", "age" to 30),
            mapOf<String, Any>("name" to "Bob", "age" to 25)
        )
        val mockReader = createMockReader(rows)
        val inputs = mapOf("input" to mockReader)
        val properties = mapOf<String, Any>(
            "nameColumn" to "name",
            "greeting" to "Hello",
            "outputColumn" to "greeting"
        )

        node.execute(properties, inputs, mockOutputWriter, mockProgressCallback)

        val rowCaptor = argumentCaptor<Map<String, Any>>()
        val numCaptor = argumentCaptor<Long>()
        verify(mockPortWriter, times(2)).write(numCaptor.capture(), rowCaptor.capture())

        assertEquals("Hello, Alice!", rowCaptor.allValues[0]["greeting"])
        assertEquals("Hello, Bob!", rowCaptor.allValues[1]["greeting"])
        // Original columns preserved
        assertEquals("Alice", rowCaptor.allValues[0]["name"])
        assertEquals(30, rowCaptor.allValues[0]["age"])
    }

    @Test
    fun `execute falls back to World when name column is missing`() {
        val rows = listOf(
            mapOf<String, Any>("age" to 30)
        )
        val mockReader = createMockReader(rows)
        val inputs = mapOf("input" to mockReader)
        val properties = mapOf<String, Any>(
            "nameColumn" to "name",
            "greeting" to "Hello",
            "outputColumn" to "greeting"
        )

        node.execute(properties, inputs, mockOutputWriter, mockProgressCallback)

        val rowCaptor = argumentCaptor<Map<String, Any>>()
        verify(mockPortWriter, times(1)).write(any(), rowCaptor.capture())
        assertEquals("Hello, World!", rowCaptor.allValues[0]["greeting"])
    }

    @Test
    fun `execute uses custom greeting prefix`() {
        val rows = listOf(
            mapOf<String, Any>("name" to "Alice")
        )
        val mockReader = createMockReader(rows)
        val inputs = mapOf("input" to mockReader)
        val properties = mapOf<String, Any>(
            "nameColumn" to "name",
            "greeting" to "Welcome",
            "outputColumn" to "message"
        )

        node.execute(properties, inputs, mockOutputWriter, mockProgressCallback)

        val rowCaptor = argumentCaptor<Map<String, Any>>()
        verify(mockPortWriter, times(1)).write(any(), rowCaptor.capture())
        assertEquals("Welcome, Alice!", rowCaptor.allValues[0]["message"])
    }

    @Test
    fun `execute uses default properties when null`() {
        val rows = listOf(
            mapOf<String, Any>("name" to "Alice")
        )
        val mockReader = createMockReader(rows)
        val inputs = mapOf("input" to mockReader)

        node.execute(null, inputs, mockOutputWriter, mockProgressCallback)

        val rowCaptor = argumentCaptor<Map<String, Any>>()
        verify(mockPortWriter, times(1)).write(any(), rowCaptor.capture())
        assertEquals("Hello, Alice!", rowCaptor.allValues[0]["greeting"])
    }

    @Test
    fun `execute reports progress`() {
        val rows = listOf(
            mapOf<String, Any>("name" to "Alice"),
            mapOf<String, Any>("name" to "Bob")
        )
        val mockReader = createMockReader(rows)
        val inputs = mapOf("input" to mockReader)

        node.execute(null, inputs, mockOutputWriter, mockProgressCallback)

        // Should report progress during processing (2 rows) and 100 at the end
        verify(mockProgressCallback, times(3)).report(any<Int>())
    }

    private fun createMockReader(rows: List<Map<String, Any>>): NodeInputReader {
        val iterator = rows.iterator()
        val mockReader = mock<NodeInputReader>()
        whenever(mockReader.read()).thenAnswer {
            if (iterator.hasNext()) iterator.next() else null
        }
        whenever(mockReader.getRowCount()).thenReturn(rows.size.toLong())
        return mockReader
    }
}
