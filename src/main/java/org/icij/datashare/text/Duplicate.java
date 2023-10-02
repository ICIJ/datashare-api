package org.icij.datashare.text;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.icij.datashare.Entity;
import org.icij.datashare.text.indexing.IndexType;

import java.nio.file.Path;

@IndexType("Duplicate")
public class Duplicate implements Entity {
    @JsonDeserialize(using = PathDeserializer.class)
    private final Path path;
    private final String documentId;
    private final String id;

    public Duplicate(final Path path, final String docId, Hasher hasher) {
        this.id = hasher.hash(path);
        this.path = path;
        this.documentId = docId;
    }

    public Duplicate(final Path path, final String docId) {
        this(path, docId, DEFAULT_DIGESTER);
    }

    @Override
    public String getId() { return id;}
}
