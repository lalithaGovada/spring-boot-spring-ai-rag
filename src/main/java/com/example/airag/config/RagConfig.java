package com.example.airag.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

// RAG setup for POST /api/faq.
// On startup the policy files are embedded and stored in memory.
// Each question searches that store before the model answers.
@Configuration
public class RagConfig {

    // In-memory store of document vectors. Fine for this demo. A real app would use pgvector or Redis.
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // This ChatClient always searches the policy files first, then asks the model to answer only from them.
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultSystem("""
                        You answer questions about company policy.
                        Use only the policy text included in the question context.
                        If the policy text does not contain the answer, say you do not know.
                        Do not invent numbers, dates, or rules.
                        """)
                // topK(3) means "keep the 3 closest chunks". These files are short, so all 3 often match.
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(3).build())
                        .build())
                .build();
    }

    // Runs once at startup: read the files, embed them, and put them in the vector store.
    @Bean
    ApplicationRunner indexDocuments(VectorStore vectorStore, ResourcePatternResolver resources) {
        return args -> vectorStore.add(loadDocuments(resources));
    }

    private List<Document> loadDocuments(ResourcePatternResolver resources) throws IOException {
        Resource[] files = resources.getResources("classpath:docs/*.txt");
        // Split a long file into smaller pieces so a search can return one rule, not the whole file.
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> documents = new ArrayList<>();
        for (Resource file : files) {
            TextReader reader = new TextReader(file);
            // Remember the file name so the API can return it in "sources".
            reader.getCustomMetadata().put("source", file.getFilename());
            documents.addAll(splitter.apply(reader.get()));
        }
        return documents;
    }
}
