package com.luyublog.aidemo.vecstore.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

/**
 * neo4j配置
 * <p>
 * note ollma的dimension是4096，而neo4j初始化代码中限制最大值为2048，暂时用不了
 *
 * <p>
 * &#064;author: east
 * <p>
 * &#064;date: 2024/4/19 21:54
 */
@Configuration
//@ConditionalOnProperty()
public class MyNeo4jVectorStoreConf {

    @Value(value = "${spring.neo4j.uri}")
    String uri;

    @Value(value = "${spring.neo4j.authentication.username}")
    String username;

    @Value(value = "${spring.neo4j.authentication.password}")
    String password;

    @Value(value = "${spring.ai.vectorstore.neo4j.embedding-dimension}")
    Integer dimension;

    @Value(value = "${spring.ai.vectorstore.neo4j.label}")
    String label;

    @Value(value = "${spring.ai.vectorstore.neo4j.index-name}")
    String indexName;

    @Autowired
    OllamaEmbeddingClient ollamaEmbeddingClient;

    public Driver driver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    @Bean
    public Neo4jVectorStore neo4jVectorStore() {
        Neo4jVectorStore.Neo4jVectorStoreConfig neo4jVectorStoreConfig = Neo4jVectorStore.
                Neo4jVectorStoreConfig.builder().
//                withEmbeddingDimension(dimension).
        withIndexName(indexName).
                withLabel(label).build();

        // dimension被限制小于2048，这里用反射跳过限制
        try {
            Class<? extends Neo4jVectorStore.Neo4jVectorStoreConfig> aClass = neo4jVectorStoreConfig.getClass();
            Field embeddingDimension = aClass.getDeclaredField("embeddingDimension");
            embeddingDimension.setAccessible(true);
            embeddingDimension.setInt(neo4jVectorStoreConfig, dimension);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new Neo4jVectorStore(driver(), ollamaEmbeddingClient, neo4jVectorStoreConfig);
    }

}
