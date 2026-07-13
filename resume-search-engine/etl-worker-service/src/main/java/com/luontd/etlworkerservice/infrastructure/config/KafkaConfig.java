package com.luontd.etlworkerservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config =
                new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                config
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(
                producerFactory()
        );
    }

    // =========================
    // Consumer Configuration
    // =========================
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config =
                new HashMap<>();
        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "default-group"
        );
        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        config.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        config.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );
        config.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*"
        );
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(Object.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory()
        );

        return factory;
    }

    /**
     * Consumer factory riêng cho CvParsedEvent — deserialize thẳng thành kiểu typed.
     * Dùng cho ElasticSearchConsumer và WeaviateConsumer.
     */
    @Bean
    public ConsumerFactory<String, com.luontd.etlworkerservice.application.dto.event.CvParsedEvent> cvParsedEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.luontd.etlworkerservice.application.dto.event.CvParsedEvent");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(
                        com.luontd.etlworkerservice.application.dto.event.CvParsedEvent.class,
                        false
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, com.luontd.etlworkerservice.application.dto.event.CvParsedEvent>
    cvParsedEventListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<
                String,
                com.luontd.etlworkerservice.application.dto.event.CvParsedEvent>();
        factory.setConsumerFactory(cvParsedEventConsumerFactory());
        return factory;
    }


    // =========================
    // Topic Configuration
    // =========================
    @Bean
    public NewTopic userTopic() {
        return TopicBuilder
                .name("user-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Value("${kafka.topic.cv-parsed-mysql}")
    private String cvParsedMysqlTopic;

    @Value("${kafka.topic.cv-parsed-elasticsearch}")
    private String cvParsedElasticsearchTopic;

    @Value("${kafka.topic.cv-parsed-embedding}")
    private String cvParsedEmbeddingTopic;

    /** Topic downstream: ETL → MySQL Service lưu metadata candidate */
    @Bean
    public NewTopic cvParsedMysqlTopic() {
        return TopicBuilder
                .name(cvParsedMysqlTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /** Topic downstream: ETL → Elasticsearch index full-text/keyword */
    @Bean
    public NewTopic cvParsedElasticsearchTopic() {
        return TopicBuilder
                .name(cvParsedElasticsearchTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /** Topic downstream: ETL → Embedding Service → Weaviate vector store */
    @Bean
    public NewTopic cvParsedEmbeddingTopic() {
        return TopicBuilder
                .name(cvParsedEmbeddingTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
