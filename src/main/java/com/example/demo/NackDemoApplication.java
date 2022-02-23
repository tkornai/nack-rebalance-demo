package com.example.demo;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@SpringBootApplication
public class NackDemoApplication {
  private final static double NACK_PERCENTAGE = 0.05;
  private final static long ONE_SECOND = 1000;
  private final static long MESSAGE_PER_SECOND = 5;
  private final static int NACK_SLEEP_MILLIS = 60_000;
  private final static String TOPIC = "test-topic";

  Logger logger = LoggerFactory.getLogger(NackDemoApplication.class);

  @Bean
  public NewTopic topic() {
    return TopicBuilder.name(TOPIC).partitions(4).build();
  }

  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx, KafkaTemplate<String, String> kafkaTemplate) {
    return args -> {
      for (int i = 0; ; i++) {
        kafkaTemplate.send(TOPIC, "Key " + i, "Message " + i);
        Thread.sleep(ONE_SECOND / MESSAGE_PER_SECOND);
      }
    };
  }

  @KafkaListener(topics = TOPIC)
  public void listenGroupFoo(String message, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
    logger.info("Partition " + partition + ", " + message);
    if (Math.random() < NACK_PERCENTAGE) {
      logger.warn("Unlucky! Calling nack().");
      ack.nack(NACK_SLEEP_MILLIS);
    } else {
      ack.acknowledge();
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(NackDemoApplication.class, args);
  }
}
