package com.erprag.core.ingest;

import com.erprag.core.exception.IngestionException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Parses any supported file type (docx/pdf/ppt/pptx/txt via Apache Tika), splits it into
 * chunks, embeds them, and stores them in pgvector — every chunk tagged with the owning
 * entity so retrieval can never cross tenant boundaries.
 */
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentSplitter documentSplitter;

    public DocumentIngestionService(EmbeddingModel embeddingModel,
                                     EmbeddingStore<TextSegment> embeddingStore,
                                     int maxSegmentSizeInChars,
                                     int maxOverlapSizeInChars) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.documentSplitter = DocumentSplitters.recursive(maxSegmentSizeInChars, maxOverlapSizeInChars);
    }

    /**
     * @return the number of chunks that were embedded and stored
     */
    public int ingest(String entityCode, UUID documentId, String filename, InputStream content) {
        try {
            Document parsed = new ApacheTikaDocumentParser().parse(content);

            Metadata metadata = parsed.metadata().copy()
                    .put("entityCode", entityCode)
                    .put("documentId", documentId.toString())
                    .put("filename", filename);
            Document tagged = Document.from(parsed.text(), metadata);

            List<TextSegment> segments = documentSplitter.split(tagged);
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings, segments);

            return segments.size();
        } catch (Exception e) {
            throw new IngestionException("Failed to ingest document '" + filename + "'", e);
        }
    }
}
