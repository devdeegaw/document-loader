package com.rag.documentloader.configuration;

import com.rag.documentloader.model.DocumentTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class DocumentLoaderPipeLine {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLoaderPipeLine.class);

  @Bean
  ApplicationRunner go(FunctionCatalog catalog) {
    Runnable composedFunction = catalog.lookup(null);
    return args -> {
      composedFunction.run();
    };
  }

  @Bean
  Function<Flux<byte[]>, Flux<Document>> documentReader() {
    return resourceFlux -> resourceFlux
        .map(fileBytes -> new TikaDocumentReader(
            new ByteArrayResource(fileBytes))
            .get()
            .getFirst())
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Bean
  Function<Flux<Document>, Flux<List<Document>>> splitter() {
    var splitter = new TokenTextSplitter();
    return documentFlux -> documentFlux
        .map(incoming -> splitter
            .apply(List.of(incoming)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Value("classpath:/templates/nameOfTheDocument.st")
  Resource nameOfTheDocumentTemplateResource;

  @Bean
  Function<Flux<List<Document>>, Flux<List<Document>>> titleDeterminer(ChatClient.Builder chatClientBuilder) {

    var chatClient = chatClientBuilder.build();

    return documentListFlux -> documentListFlux
        .map(documents -> {
          if (!documents.isEmpty()) {
            var firstDocument = documents.getFirst();

            var title = chatClient.prompt()
                .user(userSpec -> userSpec
                    .text(nameOfTheDocumentTemplateResource)
                    .param("document", firstDocument.getText()))
                .call()
                .entity(DocumentTitle.class);

            if (title == null) {
              LOGGER.warn("Unable to determine the name of a document adding to vector store without title.");
              documents = documents.stream().toList();
            } else {

              LOGGER.info("Determined document title to be {}", title.getTitle());
              documents = documents.stream().peek(document -> {
                document.getMetadata()
                    .put("title", title.getNormalizedTitle());
              }).toList();
            }
          }

          return documents;
        });
  }

  @Bean
  Consumer<Flux<List<Document>>> vectorStoreConsumer(VectorStore vectorStore) {
    return documentFlux -> documentFlux
        .doOnNext(documents -> {
          if (!documents.isEmpty()) {
            var docCount = documents.size();
            LOGGER.info("Writing {} documents to vector store.", docCount);
            vectorStore.accept(documents);
            LOGGER.info("{} documents have been written to vector store.", docCount);
          }
        })
        .doOnComplete(() -> {
          LOGGER.info("✅ Pipeline completed successfully. All documents processed.");
        })
        .doOnError(error -> {
          LOGGER.error("❌ Error in pipeline: ", error);

        })
        .subscribe();
  }

}
