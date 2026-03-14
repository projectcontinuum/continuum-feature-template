package org.projectcontinuum.feature.example.java.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectcontinuum.core.commons.protocol.progress.NodeProgressCallback;
import org.projectcontinuum.core.commons.utils.NodeInputReader;
import org.projectcontinuum.core.commons.utils.NodeOutputWriter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelloJavaNodeModelTest {

    private HelloJavaNodeModel node;
    private NodeOutputWriter mockOutputWriter;
    private NodeOutputWriter.OutputPortWriter mockPortWriter;
    private NodeProgressCallback mockProgressCallback;

    @BeforeEach
    void setup() {
        node = new HelloJavaNodeModel();
        mockOutputWriter = mock(NodeOutputWriter.class);
        mockPortWriter = mock(NodeOutputWriter.OutputPortWriter.class);
        mockProgressCallback = mock(NodeProgressCallback.class);

        when(mockOutputWriter.createOutputPortWriter("output")).thenReturn(mockPortWriter);
    }

    @Test
    void metadataIsCorrectlyConfigured() {
        assertEquals("Hello Java", node.getMetadata().getTitle());
        assertNotNull(node.getMetadata().getIcon());
        assertEquals(1, node.getInputPorts().size());
        assertEquals(1, node.getOutputPorts().size());
        assertTrue(node.getInputPorts().containsKey("input"));
        assertTrue(node.getOutputPorts().containsKey("output"));
    }

    @Test
    void categoriesContainsExamples() {
        assertTrue(node.getCategories().contains("Examples"));
    }

    @Test
    void executeAppendsGreetingColumn() {
        NodeInputReader mockReader = createMockReader(List.of(
                Map.of("name", "Alice", "age", 30),
                Map.of("name", "Bob", "age", 25)
        ));

        Map<String, Object> properties = Map.of(
                "nameColumn", "name",
                "greeting", "Hello",
                "outputColumn", "greeting"
        );

        node.execute(properties, Map.of("input", mockReader), mockOutputWriter, mockProgressCallback);

        verify(mockPortWriter, times(2)).write(anyLong(), argThat(row ->
                row.containsKey("greeting") && row.get("greeting").toString().startsWith("Hello,")
        ));
    }

    @Test
    void executeFallsBackToWorldWhenColumnMissing() {
        NodeInputReader mockReader = createMockReader(List.of(
                Map.of("age", 30)
        ));

        Map<String, Object> properties = Map.of(
                "nameColumn", "name",
                "greeting", "Hello",
                "outputColumn", "greeting"
        );

        node.execute(properties, Map.of("input", mockReader), mockOutputWriter, mockProgressCallback);

        verify(mockPortWriter).write(eq(1L), argThat(row ->
                "Hello, World!".equals(row.get("greeting"))
        ));
    }

    @Test
    void executeUsesCustomGreetingPrefix() {
        NodeInputReader mockReader = createMockReader(List.of(
                Map.of("name", "Alice")
        ));

        Map<String, Object> properties = Map.of(
                "nameColumn", "name",
                "greeting", "Welcome",
                "outputColumn", "message"
        );

        node.execute(properties, Map.of("input", mockReader), mockOutputWriter, mockProgressCallback);

        verify(mockPortWriter).write(eq(1L), argThat(row ->
                "Welcome, Alice!".equals(row.get("message"))
        ));
    }

    @Test
    void executeUsesDefaultsWhenPropertiesNull() {
        NodeInputReader mockReader = createMockReader(List.of(
                Map.of("name", "Alice")
        ));

        node.execute(null, Map.of("input", mockReader), mockOutputWriter, mockProgressCallback);

        verify(mockPortWriter).write(eq(1L), argThat(row ->
                "Hello, Alice!".equals(row.get("greeting"))
        ));
    }

    @SuppressWarnings("unchecked")
    private NodeInputReader createMockReader(List<Map<String, Object>> rows) {
        NodeInputReader reader = mock(NodeInputReader.class);
        var iterator = rows.iterator();
        when(reader.read()).thenAnswer(inv -> iterator.hasNext() ? iterator.next() : null);
        when(reader.getRowCount()).thenReturn((long) rows.size());
        return reader;
    }
}
