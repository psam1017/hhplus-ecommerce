# 서비스 확장 설계 방안

이 문서에서는 현재 서비스 코드 구조에서 트랜잭션 범위를 파악하고, 이로부터 발생하는 한계와 이를 극복하는 방안에 대해 기술합니다.

1. [프로젝트 구조 분석](#프로젝트-구조-분석)
2. [트랜잭션 범위(과거)](#트랜잭션-범위과거)
3. [트랜잭션 범위(현재)](#트랜잭션-범위현재)
4. [구조적 문제점 분석](#구조적-문제점-분석)
5. [한계 극복 : 이벤트 주도 아키텍처와 SAGA 패턴](#한계-극복--이벤트-주도-아키텍처와-saga-패턴)
6. [트랜잭션 범위(미래)](#트랜잭션-범위미래)

## 프로젝트 구조 분석

<details>
  <summary>프로젝트 구조 분석</summary>

```
[interfaces] -> [application] -> [domain(service -> repository)] <|- [infrastructure]
```

현재 이커머스 프로젝트는 클린 레이어드 아키텍처로 구현되어 있어 도메인을 중심으로 일관되게 접근하고 있습니다.

**interfaces 계층**에 있는 Controller 는 application 계층에 있는 Facade 를 호출합니다.

**application 계층**에 있는 Facade 는 퍼사드 패턴으로 Service 들을 조합하고 유스케이스를 만족/완성하는 코드입니다.

**domain 계층**에 있는 Service 는 Domain 의 행위를 보장하고, Repository 를 사용해서 영속화된 Domain 정보에 접근합니다.

이 Repository 에서는 DIP 를 적용하여 구체적인 기술에 의존하지 않도록 설계했으며, 이 Repository 는 **infrastructure 계층**에서 구현하고 이에 대한 의존성을 Spring Framework 가 주입하고 있습니다.

이와 관련해서 제 코드는 분산락을 만나면서 트랜잭션 범위가 과거에 한 번 바뀌었고, 이번 주차를 거치면서 한 번 더 바뀔 예정입니다. 이에 각 코드를 과거, 현재, 미래로 구분해봤습니다.

</details>

## 트랜잭션 범위(과거)

<details>
  <summary>트랜잭션 범위(과거)</summary>

분산락 이전에 제 코드는 **Facade 에 @Transactional** 이 적용되어 있었습니다. 불필요한 부분을 생략하고 보면 아래와 같습니다.

```
@Transactional
public Long createOrder(OrderCommand command) {

    itemService.deductStocks(...);

    pointService.usePoint(...);
    
    Order order = orderService.createOrderAndItems(...);
    
    cartService.deleteCartItems(...);
    
    orderDataPlatform.saveOrderData(...);

    return order.getId();
}
```

Facade 에서부터 트랜잭션이 적용되어 상품 차감, 포인트 차감, 주문 생성, 장바구니 삭제, 이후 외부 플랫폼에 데이터 전송하는 흐름으로 되어 있었고, 어느 지점에서 예외가 발생하면 @Transactional AOP 에 의해 자동으로 롤백되는 구조였습니다.

</details>

## 트랜잭션 범위(현재)

<details>
  <summary>트랜잭션 범위(현재)</summary>

Facade 에 트랜잭션이 적용된 상태에서 분산락을 도입했을 때 트랜잭션 및 분산락 간의 경합이 발생하여 교착상태에 빠지게 되었습니다. 교착상태가 발생한 원인 및 극복과정은 [이커머스 동시성 이슈 분석 보고서](https://github.com/psam1017/hhplus-ecommerce/blob/main/docs/concurrency/note.md)에서 확인할 수 있습니다.

이에 Facade 에서 적용한 @Transactional 을 제거하고 **각 서비스에서만 @Transactional** 을 적용하게 되었으며, 전체의 논리적 트랜잭션과 서비스 단위의 트랜잭션간의 원자성을 지키기 위해 **각 작업마다 보상 트랜잭션**을 스택 구조로 쌓고 예외 발생 시 이 보상 트랜잭션을 실행시켰습니다.

```
public Long createOrder(OrderCommand.CreateOrder command) {

    Deque<Runnable> compensationActions = new ArrayDeque<>();

    try {
        return processOrder(command, compensationActions);
    } catch (Exception e) {
        while (!compensationActions.isEmpty()) {
            try {
                compensationActions.pop().run();
            } catch (Exception e2) {
                // 로그 기록, 추가 보상 작업, 알림 전송, 모니터링 시스템 연동 등
            }
        }
        throw e;
    }
}

private Long processOrder(OrderCommand.CreateOrder command, Deque<Runnable> compensationActions) {


    for (Long itemId : itemIdStockAmountMap.keySet()) {
        itemService.deductStocks(...);
        compensationActions.push(() -> itemService.restoreStock(...));
    }

    pointService.usePoint(...);
    compensationActions.push(() -> pointService.chargePoint(...));

    Order order = orderService.createOrderAndItems(...);
    compensationActions.push(() -> orderService.cancelOrder(...));

    cartService.deleteCartItems(...);
    orderDataPlatform.saveOrderData(...);

    return order.getId();
}
```

위와 같이 코드를 변경함으로써 예외가 발생한 경우 마지막에 수행하고 커밋했던 트랜잭션 작업들을 되돌리고, 분산락까지 적용할 수 있었습니다.

</details>

## 구조적 문제점 분석

<details>
  <summary>구조적 문제점 분석</summary>

분산락 적용까지는 되었으나 정말로 분산환경에 적합한 서비스 구조라고는 할 수 없습니다.

Facade 코드만 보더라도 뭔가 굉장히 복잡해졌습니다. 이는 하나의 트랜잭션에서 모든 작업을 보장하려고 하기 때문입니다.

개발 초기 단계에서라면 관리가 가능한 규모의 한 프로젝트 및 트랜잭션으로 하는 게 유리할 수 있지만, 서비스 규모가 확장되면 될수록 생기는 문제가 생깁니다.

- **유지보수의 어려움** : 위와 같이 하나의 메서드에서 필요한 모든 작업과, 심지어 보상트랜잭션 작업까지 해야 한다면 서비스가 복잡해지고 확장될 수록 하나의 전체 트랜잭션에서 작업해야 할 코드가 커지게 됩니다.
- **도메인 로직과 부가적인 로직의 결합** : 도메인 서비스는 도메인에서 일어나는 일에만 관심이 있습니다. 그런데 부가적인 로직도 알아야 하고, 추가로 부가적인 로직, 예를 들어 위 코드의 `OrderDataPlatform` 에서 예외가 발생한다면 원래는 성공해야 할 도메인 로직까지 전체 롤백될 수 있습니다.
- **긴 시간 동안 수행되는 작업에 의한 지연 및 타임아웃** : 도메인 로직과 부가적인 로직 모두 합쳐서 주어진 커넥션(TCP/IP, DB, 분산락, 외부 플랫폼, ...) 시간 안으로 작업을 마쳐야 하는데 서비스 규모가 커지고 작업량이 많아지면 이 시간 안에 작업을 성공하지 못 할 수도 있습니다.

</details>

## 한계 극복 : 이벤트 주도 아키텍처와 SAGA 패턴

<details>
  <summary>한계 극복 : 이벤트 주도 아키텍처와 SAGA 패턴</summary>

목표는 분산환경에서의 대용량 처리입니다.

그 전에 한 프로젝트 안에서라도 트랜잭션의 범위를 나누는 작업부터 해보겠습니다.

저의 결론을 말하자면, 한 도메인이 관심을 가지는 로직은 한 트랜잭션 안에서 수행하기로 했습니다. 위 코드에서처럼 주문 상황을 가정하면, 주문 서비스는 주문 저장만 수행하면 됩니다.

그러고 남은 포인트, 재고, 외부 플랫폼 등의 모든 로직은 각자가 수행하도록 주문의 발생을 전파하고, 각기 분산된 작업들의 원자성만 지켜주면 됩니다.

이렇게 주문의 발생을 전파하는 것을 이벤트 발행이라고 합니다. 이벤트는 시스템 내 특정 사건이나 상태의 변화를 의미하는데, 이를 발행함으로써 이 이벤트에 관심을 가지는 구독자, 이 경우에는 각 도메인들과 데이터 플랫폼이 비동기적으로 자신만의 로직을 처리하게 하면 하나의 전체 작업을 작은 여러 개의 작업으로 분리할 수 있습니다.

결과적으로 HTTP 등의 외부 요청에는 빠르게 응답하면서 위에서 언급했던 유지보수, 로직간의 결합, 타임아웃 문제들을 해결할 수 있게 됩니다.

이러한 접근 방식을 이벤트 주도 아키텍처(Event Driven Architecture, EDA)라고 합니다. 시스템 안에서 발생하는 이벤트를 감지하고 이에 반응하여 필요한 작업을 수행하는 방식으로 구성됩니다. 특히 현재 상황과 같이 비동기적이고 분산된 시스템에서 유용하게 사용할 수 있습니다.

이벤트에 의한 작업들을 동기적으로 수행하면 각 작업들이 서로 직접적으로 호출하게 되어 위에서 언급한 여러 문제들이 다시 그대로 나타나게 됩니다. 비동기적인 작업들의 원자성을 보장하려면 동기화가 아니라 논리적으로 이를 보장하는 방법들이 필요하고, 이로 인해 SAGA 패턴 등이 생겨나게 됩니다.

SAGA 패턴은 분산된 트랜잭션 중 어느 하나의 로컬 트랜잭션이 실패할 경우 이전에 완료된 트랜잭션을 되돌리는 보상 트랜잭션을 실행하여 원자성을 지키는 패턴을 의미합니다.

SAGA 패턴 중 Orchestration 과 Choreography 2가지 유형이 있습니다.

- Orchestration
    - 특징 : 중앙 관리자인 오케스트레이터가 각 로컬 트랜잭션 순서, 성공, 실패를 관리합니다.
    - 장점 : 많은 참가자가 관여하거나 추가되는 복잡한 경우에 효과적이며, 각 참가자끼리 순환 종속성을 도입하지 않는다는 장점이 있습니다. 결과적으로 비즈니스 논리는 간소화될 것입니다.
    - 단점 : 오케스트레이터가 전체를 관리하므로 추가 실패 지점 혹은 단일 실패 지점이 되어 이벤트를 구독하는 시스템 전체가 영향을 받을 수 있습니다.
- Choreography
    - 특징 : 중앙 관리자 없이 이벤트를 교환하며 서로 조율합니다. 각 로컬 트랜잭션은 Choreography, 즉 연출을 하면서 다른 서비스의 로컬 트랜잭션을 트리거합니다.
    - 장점 : 참가자가 적거나 논리 조정이 필요하지 않은 경우에 효과적이며, 각 참가자에게 책임을 분산하여 단일 실패 지점을 도입하지 않습니다.
    - 단점 : 프로세스가 혼동되지 않도록 이벤트 관리의 복잡도가 올라가며 각 참가자 간에 순환 종속성이 발생할 위험이 생깁니다. 또한 전체 트랜잭션을 시뮬레이션하기 위한 통합 테스트가 어렵다는 점도 있습니다.

> 참고 링크 : [Saga 분산 트랜잭션 패턴](https://learn.microsoft.com/ko-kr/azure/architecture/reference-architectures/saga/saga#orchestration)

> 저는 단일 실패 지점을 도입하지 않는 것이 서비스 운영에 더 중요하다고 보고 Choreography SAGA 패턴을 적용할 예정입니다.

![scalability-3](https://github.com/user-attachments/assets/2d50ec25-ebec-4c7c-9830-036bbf36ac08)

(화이트보드는 언제나 최고입니다)

다시 원래의 프로젝트를 돌아보겠습니다. 하나의 프로젝트 안이지만, 각 도메인이 하나의 독립된 서비스라고 가정하고 서비스 흐름을 구상했을 때 위 사진과 같이 구성할 수 있습니다.

기존의 주문 메서드 안에서 작업하던 재고차감, 포인트차감, 주문저장, 데이터 전송 중에서 주문저장 이외의 모든 것들을 제거합니다.

이제 주문 메서드는 주문을 저장하고, 이를 이벤트로 발행하기만 하면 끝입니다.

이때 이 이벤트와 주문 도메인의 로컬 트랜잭션이 원자적으로 수행되도록 하기 위해 함께 수행해야 합니다. 이를 트랜잭셔널 메시징(Transactional Messaging)이라고 합니다.

이를 보장하기 위한 방법 중 트랜잭셔널 아웃박스 패턴 (Transactional Outbox Pattern) 은 별도의 아웃박스 테이블을 만들고 메시지 정보를 저장함으로써 트랜잭션 로직과 메시지 발행 로직을 하나로 묶는 방식입니다. 만약 이후에 메시지 발행에서만 실패하더라도 배치작업 등을 통해서 작업에 실패한 메시지를 재전송하게 되면 결국 언젠가 데이터 정합성이 맞춰지게 됩니다. 이를 Eventually Consistency 라고 표현합니다.

> 참고 링크 : [트랜잭셔널 아웃박스 패턴의 실제 구현 사례 (29CM)](https://medium.com/@greg.shiny82/%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%94%EB%84%90-%EC%95%84%EC%9B%83%EB%B0%95%EC%8A%A4-%ED%8C%A8%ED%84%B4%EC%9D%98-%EC%8B%A4%EC%A0%9C-%EA%B5%AC%ED%98%84-%EC%82%AC%EB%A1%80-29cm-0f822fc23edb)

이때 아웃박스 테이블에는 분산된 작업들의 원자성을 지키기 위해 트랜잭션키, 발행상태, 작업상태 등이 같이 저장되어야 합니다.

이후 트랜잭션 커밋을 하고 나면 각 포인트, 재고, 데이터 플랫폼 서비스들은 이 이벤트와 메시지를 전달 받고, 각자의 로직을 처리하게 됩니다.

만약 어떤 서비스가 로직 수행 중에 예외가 발생하면 그 로직은 즉시 롤백하고 작업 상태는 실패로 바꾸는 것과 같이 실패 이벤트를 발행합니다. 이후 실패 이벤트를 받게된 경우 각 서비스는 완료했던 로직을 되돌리는 보상 트랜잭션을 수행합니다.

이때 주문 생성 이벤트를 뒤늦게 전달 받은 서비스는 로직을 수행하기 전에 아웃박스의 작업상태를 같이 점검하여 로직 수행 여부를 결정할 수 있습니다.

이런 식으로 각각의 서비스를 분산하고 이벤트를 사용하여 각자의 작업을 비동기적으로 수행하고, 적절히 보상 트랜잭션도 갖추어 트랜잭션 범위를 낮추고 유지보수, 로직간의 결합, 타임아웃 문제들을 해결할 수 있습니다.

하지만 서비스가 확장되고 MSA 로 도메인 별로 서버를 각기 구축하게 된다면 이 이벤트의 메시지를 중개하는 브로커가 필요해집니다. 물론 이 브로커 없이 직접 전달하는 방법도 있겠지만 그렇게 하면 데이터 파이프라인 관리 및 처리 복잡도가 증대할 수 있기에 중앙집중형 메시지 관리 서버가 효과적입니다.

![scalability-2](https://github.com/user-attachments/assets/6819d7de-ef22-424a-b9c2-70a563fa15ea)

이벤트 기반 아키텍처에서는 이벤트 메시지를 전달하는 브로커의 가용성과 성능은 물론이고, 이를 여러 컨슈머가 소비하는 환경을 제공하는 것이 중요합니다.

카프카는 대용량 처리에 최적화되어 있고, 스케일 아웃으로 처리량을 늘릴 수 있습니다. 또한 클러스터링 구축, 파티션 리밸런싱 및 디스크에 메시지를 저장하는 방식 덕분에 가용성과 회복력 또한 좋습니다. 같은 이유에서 여러 컨슈머 및 컨슈머 그룹들이 상호 간섭 없이 메시지를 처리할 수 있다는 장점도 있습니다.

따라서 위 사진과 같이 각 마이크로서비스가 성능, 가용성이 높은 카프카를 중심으로 메시지를 주고 받음으로써 EDA 및 MSA 를 구축하고, 목표였던 분산 환경에서의 대용량 처리가 가능합니다.

</details>

## 트랜잭션 범위(미래)

<details>
  <summary>트랜잭션 범위(미래)</summary>

```

```

</details>
