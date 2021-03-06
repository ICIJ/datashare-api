package org.icij.datashare.batch;

import org.icij.datashare.json.JsonObjectMapper;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;


public class SearchResultTest {
    @Test
    public void test_json_serialize() throws Exception {
        assertThat(JsonObjectMapper.MAPPER.writeValueAsString(new SearchResult("q1", "docId1", "rootId1", Paths.get("/path/to/doc1"),
                new Date(), "content/type", 123L, 1))).contains("\"documentPath\":\"/path/to/doc1\"");
        assertThat(JsonObjectMapper.MAPPER.readValue(("{\"query\":\"q1\"," +
                        "\"documentId\":\"docId1\"," +
                        "\"documentPath\":\"/path/to/doc1\"," +
                        "\"creationDate\":\"1608049139794\"," +
                        "\"rootId\":\"rootId1\",\"documentNumber\":\"1\"," +
                        "\"contentType\":\"content/type\",\"contentLength\":123}").getBytes(),
                SearchResult.class).documentPath.toString()).isEqualTo("/path/to/doc1");

    }
}