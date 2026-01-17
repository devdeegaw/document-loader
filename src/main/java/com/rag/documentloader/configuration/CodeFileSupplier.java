package com.rag.documentloader.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component("fileSupplier")
public class CodeFileSupplier implements Supplier<Flux<byte[]>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeFileSupplier.class);

    @Value("${file.supplier.directory}")
    private String directory;

    @Value("${file.supplier.extensions:java,py,js,ts,cpp,c,go,rs,kt,scala,rb,php,cs,swift}")
    private String extensions;

    @Override
    public Flux<byte[]> get() {
        String[] exts = extensions.split(",");
        
        return Flux.defer(() -> {
            try {
                Stream<Path> paths = Files.walk(Paths.get(directory))
                        .filter(Files::isRegularFile)
                        .filter(path -> matchesExtension(path, exts));
                
                return Flux.fromStream(paths)
                        .flatMap(path -> {
                            try {
                                byte[] content = Files.readAllBytes(path);
                                LOGGER.info("Processing file: {}", path);
                                return Flux.just(content);
                            } catch (IOException e) {
                                LOGGER.error("Error reading file: {}", path, e);
                                return Flux.empty();
                            }
                        });
            } catch (IOException e) {
                LOGGER.error("Error walking directory: {}", directory, e);
                return Flux.empty();
            }
        });
    }

    private boolean matchesExtension(Path path, String[] extensions) {
        String fileName = path.getFileName().toString();
        for (String ext : extensions) {
            if (fileName.endsWith("." + ext.trim())) {
                return true;
            }
        }
        return false;
    }
}
