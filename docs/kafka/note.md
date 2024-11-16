# Kafka 실행 및 테스트 보고서

1. [빠르게 카프카 테스트하기](#빠르게-카프카-테스트하기)
2. [카프카 UI 로 확인하기](#카프카-ui-로-확인하기)
3. [스프링 카프카 연동하기](#스프링-카프카-연동하기)
4. [카프카 학습 정리 문서](#카프카-학습-정리-문서)
5. [카프카 프로듀서 주요 옵션](#카프카-프로듀서-주요-옵션)
6. [카프카 컨슈머 주요 옵션](#카프카-컨슈머-주요-옵션)
7. [카프카 트랜잭션 프로듀서와 트랜잭션 컨슈머](#카프카-트랜잭션-프로듀서와-트랜잭션-컨슈머)

## 빠르게 카프카 테스트하기

<details>
  <summary>빠르게 카프카 테스트하기</summary>

카프카 컨테이너 설정은 프로젝트 최상단의 compose.yml 참고해주세요.

1. 애플리케이션 빌드하기
    ```
   [1번 터미널]$ ./gradlew clean build
   ```

2. 카프카를 포함한 컨테이너 실행하기

    ```
    [1번 터미널]$ docker compose up --build -d
    ```

3. 카프카 터미널 접속하기
    ```
    [2번 터미널]$ docker run --rm -it --network ecommerce-network confluentinc/cp-kafka:7.5.4 bash
    ```

4. 카프카 토픽 생성 및 확인하기
   ```
   [2번 터미널]$ kafka-topics --create --bootstrap-server kafka:9092 --topic hello-kafka
   [2번 터미널]$ kafka-topics --list --bootstrap-server kafka:9092
   ```

5. 메시지 생산 준비하기
   ```
   [2번 터미널]$ kafka-console-producer --broker-list kafka:9092 --topic hello-kafka
   ```

6. 메시지 소비 준비하기
   ```
   [3번 터미널]$ docker run --rm -it --network ecommerce-network confluentinc/cp-kafka:7.5.4 bash
   [3번 터미널]$ kafka-console-consumer --bootstrap-server kafka:9092 --topic hello-kafka --from-beginning
   ```

7. 메시지 출력 확인하기

   2번 터미널에서 메시지를 입력하면 3번 터미널에서 해당 메시지들이 순차적으로 출력되는 것을 확인할 수 있습니다.
   ```
   [2번 터미널]
   hello
   kafka
   [3번 터미널]
   hello
   kafka
   ```

</details>

## 카프카 UI 로 확인하기

<details>
  <summary>카프카 UI 로 확인하기</summary>

만약 compose.yml 설정대로 실행했다면 localhost:8090 에서 카프카에서 발행된 메시지를 UI 로 확인할 수도 있습니다.

![kafka-1](https://github.com/user-attachments/assets/3816f966-34b5-4146-b905-a13d04499c2c)

</details>

## 스프링 카프카 연동하기

<details>
  <summary>스프링 카프카 연동하기</summary>

카프카가 정상적으로 동작되는 것을 확인했으니 이번에는 스프링과 간단하게 연동해보겠습니다.

아래의 순서대로 의존성을 추가하고, 토픽, 프로듀서, 컨슈머를 생성하면 메시지의 발행 및 소비를 확인할 수 있습니다.

1. build.gradle 에서 의존성 추가
    ```
    dependencies {
        // kafka
        implementation 'org.springframework.kafka:spring-kafka'
    }
    ```

2. config 에서 토픽 설정
    ```
    package hhplus.ecommerce.server.config;

    import org.apache.kafka.clients.admin.NewTopic;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.kafka.config.TopicBuilder;
    
    @Configuration
    public class KafkaConfig {
    
        @Bean(name = "helloKafkaTopic")
        public NewTopic helloKafkaTopic() {
            return TopicBuilder
                    .name("hello-kafka")
                    .partitions(3)
                    .build();
        }
    }
   ```

3. producer 생성
    ```
    package hhplus.ecommerce.server.infrastructure.event;
    
    import jakarta.annotation.PostConstruct;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.kafka.core.KafkaTemplate;
    import org.springframework.stereotype.Service;
    
    // 테스트 용으로 생성
    @Slf4j
    @RequiredArgsConstructor
    @Service
    public class HelloKafkaProducer {
    
        private final KafkaTemplate<String, String> kafkaTemplate;
    
        public void sendMessage() {
            kafkaTemplate.send("hello-kafka", "kafka");
        }
    
        @PostConstruct
        public void init() {
            // 실행 이후 시험용으로 메시지를 발행
            sendMessage();
        }
    }
    ```

4. consumer 생성
    ```
    package hhplus.ecommerce.server.infrastructure.event;
    
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.kafka.annotation.KafkaListener;
    import org.springframework.stereotype.Service;
    
    // 테스트 용으로 생성
    @Slf4j
    @RequiredArgsConstructor
    @Service
    public class HelloKafkaConsumer {
    
        @KafkaListener(topics = "hello-kafka", groupId = "ecommerce")
        public void consume(String message) {
            log.info("Consumed message: {}", message);
        }
    }
    ```

이후 애플리케이션을 실행시켜보면 아래와 같이 프로듀서가 발행한 메시지를 컨슈머가 소비하여 콘솔로 출력하는 것을 확인할 수 있습니다.

![kafka-2](https://github.com/user-attachments/assets/11721e36-7bd4-4d84-a421-3cf524805b72)

</details>

## 카프카 학습 정리 문서

<details>
  <summary>카프카 학습 정리 문서</summary>

아래 링크로 가시면 별도로 정리한 노션 문서도 보실 수 있습니다:)

[카프카 정리한 노션 문서](https://married-jumper-f34.notion.site/Kafka-f0b2e058d8264dd2a5e223c33204fd45)

아래에서부터는 그 중에서 프로듀서, 컨슈머 참고 내용에 대해 기재합니다. 참고로 Apache Kafka 기준으로 기재합니다.

</details>

## 카프카 프로듀서 주요 옵션

<details>
  <summary>카프카 프로듀서 주요 옵션</summary>

### 필수옵션

- bootstrap.servers : {브로커 호스트}:{포트} 1개 이상 작성. 여러 개의 브로커 정보를 입력함으로 일부 브로커에 이슈가 발생하더라도 접속에 이슈가 없도록 설정한다.
- key.serializer : Record Message Key 를 직렬화하는 클래스를 지정한다.
- value.serializer : Record Message Value 를 직렬화하는 클래스를 지정한다.

### 선택옵션

- acks: 브로커로부터 어떤 종류의 응답을 기다릴지 설정. 기본값은 1(리더 파티션만 확인)
    - `ISR 과 acks 옵션`에서 상세 설명
- linger.ms : 배치를 전송하기 전까지 기다리는 최소 시간. 기본값은 0
- retries : 브로커로부터 에러를 받고 난 후 재전송을 시도하는 횟수. 기본값은 2,147,483,647
- max.in.flight.requests.per.connection : 한 번에 요청하는 최대 커넥션 개수. 설정된 값만큼 동시에 전달 요청을 수행한다. 기본값은 5
- partitioner.class : 레코드를 파티션오 전송할 때 적용하는 파티셔너 클래스. 기본값은 org.apache.kafka.clients.producer.internals.DefaultPartitioner(2.5.0 부터는 UniformStickyPertitioner)
- enable.idempotence : 멱등성 프로듀서로 동작할지 여부. 기본값은 false(3.0 부터는 true)
- transactional.id : 레코드를 트랜잭션 단위로 묶을지 여부. 기본값은 null

### ISR 과 acks 옵션

- ISR(In-Sync-Replicas) : 리더 파티션과 팔로워 파티션이 모두 싱크된 상태 또는 그들의 집합
    - 프로듀서가 리더 파티션에 먼저 데이터를 전송하고, 그 이후 리더 파티션으로부터 팔로워 파티션으로 데이터를 전송하는 데 시간이 걸리기 때문에 이러한 용어가 있다.
    - 예를 들어
        - 리더 파티션의 복제 개수를 2로 설정해서 리더 파티션 1개, 팔로워 파티션 1개가 있고, 리더 파티션의 레코드는 offset 0부터 12까지 존재한다.
        - 이때 팔로워 파티션도 offset 0부터 12까지 있다면 모든 파티션들이 ISR(상태)에 포함되어 있다.
        - 만약 그렇지 않다면 ISR 에는 리더 파티션만 포함되어 있다.
- acks
    - 설명
        - 브로커로부터 어떤 종류의 응답을 기다릴지 설정
        - 모든 파티션으로부터 응답을 받을 수 있다면 데이터 저장에 대한 신뢰성을 높일 수 있으나 그만큼 성능이 저하된다.
        - Trade Off 가 발생할 수 밖에 없기 때문에 데이터의 유실 여부가 얼마나 중요한지에 따라 결정해야 한다.
            - 예를 들어 GPS 정보는 일부 데이터의 유실 여부보다 속도가 더 정확하기에 0 으로 설정해도 된다.
    - 옵션값
        - 1 : 기본값. 리더 파티션 브로커의 응답을 기다림.
        - 0 : 브로커로부터 응답을 기다리지 않음.
        - -1 또는 all : 리더 파티션 및 모든 팔로워 파티션 브로커의 응답을 기다림.
            - 토픽 단위로 설정 가능한 min.insync.replicas 옵션값에 따라 데이터 안정성 및 성능이 달라진다. 예를 들어, 파티션이 100개라고 하더라도 min.insync.replicas 가 3이라면 리더 하나와 팔로워 2개만 확인하고 응답하게 된다.
                - min.insync.replicas=2 만 해도 충분하다.
                - min.insync.replicas=1 이라면 리더 파티션만 확인하고 응답하는 것과 같기에 -1 로 정하는 의미가 없다.

</details>

## 카프카 컨슈머 주요 옵션

<details>
  <summary>카프카 컨슈머 주요 옵션</summary>

### 필수옵션

- bootstrap.servers : {브로커 호스트}:{포트} 1개 이상 작성. 여러 개의 브로커 정보를 입력함으로 일부 브로커에 이슈가 발생하더라도 접속에 이슈가 없도록 설정한다.
- key.deserializer : Record Message Key 를 역직렬화하는 클래스를 지정한다.
- value.deserializer : Record Message Key 를 역직렬화하는 클래스를 지정한다.
    - 운영 상의 이점을 위해 가급적 String 으로 통일하는 게 좋다.

### 선택옵션

- group.id : 컨슈머 그룹 아이디를 지정한다. subscribe() 메소드로 토픽을 구독할 때는 필수로 지정해야 한다. 기본값은 null
- auto.offset.reset : 컨슈머 그룹이 특정 파티션을 읽을 때 저장된 오프셋이 없는 경우, 어느 오프셋부터 읽을지 선택. 컨슈머 오프셋이 있다면 무시된다. 기본값은 latest
    - latest : 가장 최근 오프셋부터 읽는다.
    - earliest : 가장 오래된 오프셋부터 읽는다.
    - none : 컨슈머 그룹의 커밋 기록을 찾아서 없으면 예외를 던지고, 있다면 그 값을 활용한다. 거의 사용하지 않는 듯하다.
- enable.auto.commit : 오토 커밋 여부를 선택한다. 기본값은 true
- auto.commit.interval.ms : 오토 커밋일 때 커밋 간격을 지정한다. 기본값은 5000(5초)
- max.poll.records : poll() 메소드를 통해 반환 받는 레코드 개수를 지정한다. 기본값은 500
- session.timeout.ms : 컨슈머의 응답을 기다리는 최대 시간을 지정한다.  기본값은 10000(10초)
- heartbeat.interval.ms : 하트비트를 전송하는 간격을 지정한다. 기본값은 3000(3초)
    - 해당 시간 동안 컨슈머가 하트비트를 보내지 않으면 해당 컨슈머를 Dead 상태로 간주하고 리밸런싱을 트리거한다.
    - 예를 들어, 컨슈머는 3초마다 하트비트를 보내고, 브로커는 마지막 하트비트로부터 10초까지 다음 하트비트를 기다린다. 3초마다 보내던 하트비트가 10초 동안 오지 않으면 리밸런싱을 트리거한다.
- max.poll.interval.ms : poll() 메소드를 호출하는 간격 최대 시간. 기본값은 300000(5분)
- isolation.level : 트랜잭션 프로듀서가 레코드를 트랜잭션 단위로 보낼 때 사용한다.
    - read_uncommitted
    - read_committed

</details>

## 카프카 트랜잭션 프로듀서와 트랜잭션 컨슈머

<details>
  <summary>카프카 트랜잭션 프로듀서와 트랜잭션 컨슈머</summary>

- 다수의 파티션에 데이터를 저장할 경우 모든 데이터에 대해 동일한 원자성을 만족시키기 위해 일련의 작업을 집합으로 묶는 것이다. 즉, 전체 데이터를 처리하거나, 처리하지 않도록 하려는 목적이다.
- 트랜잭션 프로듀서는 사용자가 보낸 데이터를 레코드로 파티션에 저장하는 것에 대허 트랜잭션의 시작과 끝을 표현하기 위해 트랜잭션 레코드(COMMIT)를 추가로 보낸다.
- 이렇게 COMMIT 한 레코드가 존재하게 되면, 트랜잭션 컨슈머는 트랜잭션 레코드로 커밋된 데이터를 파악하고 커밋된 데이터를 가져간다.

트랜잭션 프로듀서
```
Properties properties = new Properties();
properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID());

Producer<String, String> producer = new KafkaProducer<>(properties);

producer.initTransactions();

producer.beginTransaction();
producer.send(new ProducerRecord<>(TOPIC_NAME, "message value"));
producer.commitTransaction();

producer.close();
```

트랜잭션 컨슈머
```
...
Properties properties = new Properties();
properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
...
```

</details>
