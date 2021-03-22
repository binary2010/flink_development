# flink_development
learn flink process


# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.3/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.3/maven-plugin/reference/html/#build-image)
* [Spring Data Elasticsearch (Access+Driver)](https://docs.spring.io/spring-boot/docs/2.4.3/reference/htmlsingle/#boot-features-elasticsearch)
* [Java Mail Sender](https://docs.spring.io/spring-boot/docs/2.4.3/reference/htmlsingle/#boot-features-email)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.4.3/reference/htmlsingle/#boot-features-kafka)


# kafka消息实时处理 
source kafka 10.2.200.69 集群
sink   redis 10.2.200.5 单机
处理his消息顺序、消息缺失


## kafka
10.2.200.69 kafka1
10.2.200.69 kafka2
10.2.200.69 kafka3
10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094
## redis
10.2.200.5:16379
20211223fdfsdfsdfdf@$%ssfpoooiSEEWWEE


## flink kafka connector
https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kafka.html

Properties for the Kafka consumer. The following properties are required:
“bootstrap.servers” (comma separated list of Kafka brokers)
“group.id” the id of the consumer group