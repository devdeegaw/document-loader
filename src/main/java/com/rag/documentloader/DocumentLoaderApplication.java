package com.rag.documentloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.supplier.file.FileSupplierConfiguration;

@SpringBootApplication(exclude = FileSupplierConfiguration.class)
public class DocumentLoaderApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocumentLoaderApplication.class, args);
  }

}
