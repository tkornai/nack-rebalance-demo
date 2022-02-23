# Nack-rebalance-demo

### Description

This application shows how calling Spring Kafka's `KafkaMessageListenerContainer.nack()` method delays the finishing of 
a subsequent consumer group rebalance.

### How to reproduce the issue

0. Start Zookeeper and Kafka docker containers: <br>
`docker-compose up`
1. Start one instance of the `NackDemoApplication` app
2. Monitor the consumer group state: <br>
`watch 'kafka-consumer-groups --bootstrap-server localhost:29092 --group foo --describe'`
3. After the first application calls `.nack()` and stops consuming, start a second instance of the application.
4. It can be observed that the CG rebalance does not finish until the `nack()` sleep time has passed.
