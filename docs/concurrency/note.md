# 이커머스 동시성 이슈 분석 보고서

## 목차
- [동시성 이슈란?](#동시성-이슈란)
- [이커머스 시나리오에서 발생할 수 있는 동시성 이슈](#이커머스-시나리오에서-발생할-수-있는-동시성-이슈)
- [동시성 제어를 위한 락](#동시성-제어를-위한-락)
- [데드락](#데드락)
- [동시성 제어 방식](#동시성-제어-방식)
- [락 시간 측정 테스트](#락-시간-측정-테스트)
- [트러블 슈팅](#트러블-슈팅)

---

## 동시성 이슈란?
<details>
  <summary>동시성 이슈란?</summary>

동시성 이슈란 공유될 수 있는 하나의 자원에 대해 여러 트랜잭션, 스레드, 프로세스 또는 작업 등이 동시에 접근할 때 충돌이 일어나는 것을 의미합니다.

예를 들어, 하나의 자원이 속성으로 0이라는 값을 가지고 있을 때, 1씩 증가하는 요청을 여러 번 한다고 가정해봅시다.

만약 이 요청이 순차적으로 발생히여 충돌이 생기지 않는다고 하면 다음과 같이 수정됩니다. 

- A : 현재값(0) + 1 -> 1 로 업데이트
- B : 현재값(1) + 1 -> 2 로 업데이트
- C : 현재값(2) + 1 -> 3 로 업데이트
- ...

하지만, 이 요청이 순차적으로 발생한 게 아니라 동시에 발생한다면 다음과 같이 수정됩니다.

- A : 현재값(0) + 1 -> 1 로 업데이트
- A' : 현재값(0) + 1 -> 1 로 업데이트
- A'' : 현재값(0) + 1 -> 1 로 업데이트
- ...

이렇게 충돌이 발생하면 1씩 증가하는 요청을 아무리 여러 번 요청하더라도 그 값이 제대로 반영되지 않을 수 있기 때문에 동시성 이슈가 발생하지 않도록 제어하는 것은 굉장히 중요합니다.

이것을 보고 소위 '동시성 제어'라고 합니다.

</details>

## 이커머스 시나리오에서 발생할 수 있는 동시성 이슈

<details>
  <summary>이커머스 시나리오에서 발생할 수 있는 동시성 이슈</summary>

- 이커머스 시나리오 프로젝트에서도 동시성 이슈가 발생할 수 있는 솽황이 3가지 있습니다.

### 포인트 충전을 위해 포인트 레코드에 접근 - 낙관적 락 사용

포인트를 충전하는 상황에서 여러 번의 결제를 수행하더라도 1번만 포인트가 충전되는 문제 상황이 생길 수 있습니다.

- A : 현재 포인트(100) + 100 -> 200 으로 업데이트
- A' : 현재 포인트(100) + 100 -> 200 으로 업데이트
- A'' : 현재 포인트(100) + 100 -> 200 으로 업데이트


원래는 트랜잭션 안에서 PG 사 결제 승인을 요청하는 등의 상황을 생각해서 비관적 락이 더 적합하지 않을까 생각했는데, 팀원분들과 토론, 그리고 코치님의 피드백을 통해서 낙관적 락과 PG 사 요청을 같이 하는 게 더 좋다는 결론으로 바뀌었습니다.
<br>
토론 당시 제가 생각한 비관적 락의 근거입니다.
```
...
저는 포인트 충전이 현실상황에서는 어떻게 될까를 좀 고려해본 것 같아요
코드에서는 빠져 있지만 실제 포인트 충전을 한다는 것은 사용자의 카드나 현금을 결제한다는 것이고, 그 경우 PG 사에 해당 정보로 승인 요청을 보낼 수 있는 상황까지 오리라고 생각했습니다. 그러면 낙관적 락을 사용하고 100 번 요청을 동시에 받았을 때 PG 사에 100 번 요청이 날아가고, DB 는 한 번만 업데이트하게 되니 문제가 생기지 않을까 했어요.
토스 같은 PG 사는 멱등키를 사용해서 동시성 제어를 하는 방법도 마련해뒀지만, 좀 오래된 PG 사는 멱등키가 없는 경우도 있다고 알아서, 이러한 경우에 대비하여 검증하려는 의도가 있습니다. 한 마디로 PG 사 요청을 한 번만 하기 위해서에요.
비관적 락으로 한 레코드에 대한 접근을 한 번씩만 하게 제어하고, 트랜잭션 진입 후 결제키와 결제상태를 체크하면 PG 사에 요청을 보내기 전에 결제완료된 레코드임을 확인하고 예외를 던져 PG 사 요청을 막을 수 있을 것 같습니다.
근데 이러한 가정이 없다면 낙관적 락이 맞다는 생각이 드네요.
...
```

아래는 항해플러스 코치님의 멘토링 내용 일부입니다.
```
PG 와 같이 외부 의존성이 껴있는 경우, 낙관적 락을 활용해 구현한다면, 트랜잭션이 정상적으로 처리되었을 때만 PG 사에 요청을 보내는 방식을 활용해볼 수도 있을 것 같습니다. 예를 들면 아래와 같겠죠.
tx {
  잔액 조회
  잔액 차감
  ..
  결제 생성
}
PG 전송

다만 여기서 주의해야할 점은, 말씀해주신 것처럼 앞 단에서 트랜잭션 범위 내에서 결제가 정상적으로 생성된 경우만 "결제 시도" 에 성공한 것으로 간주해야하므로 이런 부분을 주의해서 구현해야 합니다.
오히려 이 경우는 결제와 예약에 영향을 줄 수 있는 시도가 동시에 발생할 수 있는 만큼 아래와 같이 각 자원에 대해 락을 적절히 설정하므로서 이점을 얻을 수 있을지? 등에 대해서 고려해보고, PG 전송에 대한 실패가 발생했을 때 재시도 전략 등을 세워 촘촘하게 비즈니스 컨트롤을 해볼 수 있을 것 같아요.
tx {
  예약 조회 + 검증 // 비관락 사용
  잔액 조회 + 차감 및 검증 // 낙관락 사용
  ..
  결제 생성
}
PG 전송
```

`트랜잭션(낙관적 락) -> 트랜잭션 종료 -> PG 전송 -> PG 검증하여 재시도, 보상 트랜잭션, 성공 등을 처리`하는 흐름으로 로직을 구성하면 됩니다.
<br>
비즈니스 로직 동안에는 '모든 로직'에서 트랜잭션이 보장되어야 하니까 PG 사 승인 요청도 트랜잭션 안에 있어야 한다, 라는 착각에 비관적 락을 사용했던 것입니다. 이번 프로젝트 통해 가장 크게 배운 것은 (분산락 사용도 있긴 하지만) **트랜잭션 범위를 적절하게 사용해야 한다**는 교훈 같습니다. 

### 주문/결제 시 - 포인트 감소를 위해 포인트 레코드에 접근 - 낙관적 락 사용

포인트를 사용하여 주문을 하는 상황에서 여러 번 주문을 했음에도 1번만 포인트가 사용되는 문제 상황이 생길 수 있습니다.

- B : 현재 포인트(1000) - 100 -> 900 으로 업데이트
- B' : 현재 포인트(1000) - 100 -> 900 으로 업데이트
- B'' : 현재 포인트(1000) - 100 -> 900 으로 업데이트

이 상황도 위와 마찬가지입니다. 사실 위의 코치님 피드백이 더 잘 설명되어 있지만, 포인트 감소 자체는 낙관적 락으로 하되, 같은 논리적 트랜잭션 안에서 재고 차감 등의 로직에는 비관적 락을 사용하면 전체 로직의 동시성 문제를 제어하면서 비관적 락의 단점인 대기시간도 줄일 수 있습니다.

### 주문/결제 시 - 재고 차감을 위해 재고 레코드에 접근 - 비관적 락 사용

1개 밖에 남지 않은 상품을 주문할 때 여러 번의 주문 모두가 구매에 성공하는 문제 상황이 생길 수 있습니다.

- C : 현재 재고수량(1) - 1 -> 0 으로 업데이트 && 주문 성공
- C' : 현재 재고수량(1) - 1 -> 0 으로 업데이트 && 주문 성공
- C'' : 현재 재고수량(1) - 1 -> 0 으로 업데이트 && 주문 성공

재고의 경우 상황이 좀 달라질 수 있습니다. 여러 건의 요청이 필요한 만큼 성공하기 위해서는 비관적 락이 더 효율적입니다.

예를 들어 100번의 총 10번의 재고 차감이 성공해야 하는 상황에서 낙관적 락을 적용하면 첫 시도에서는 1건 성공 99건 실패, 그 다음에는 1건 성공 98건 실패, 이런 식으로 처리하게 되어 10번 성공을 위해 955번의 요청 또는 재시도를 해야 합니다.

반면에 비관적 락을 사용하면 대기시간이 존재한다는 단점은 있지만, 타임아웃 이내에 100번의 요청 중 10번만 성공시키면 되기 때문에 더 적합하다고 볼 수 있습니다.

그리고 이와 더불어 최종적으로 분산락과 비관락을 같이 적용하여 도메인 별로 DB 가 분리된 분산 환경에서도 동시성을 제어할 수 있도록 했습니다.

분산락 적용은 [STEP12 브랜치](https://github.com/psam1017/hhplus-ecommerce/tree/STEP12)에 반영되어 있습니다.

</details>

## 동시성 제어를 위한 락

<details>
  <summary>동시성 제어를 위한 락</summary>

동시성 이슈를 해결하기 위해서는 락(Lock)이라는 개념을 사용합니다. 락은 공유 자원에 대한 접근을 제어하여 동시에 여러 프로세스나 스레드가 동일한 자원에 접근하는 것을 방지하는 메커니즘입니다. 이를 통해 데이터의 무결성을 유지하고 예기치 않은 충돌이나 오류를 예방할 수 있습니다.

### 낙관적 락과 비관적 락
락에는 낙관적 락과 비관적 락이 있습니다.

- 낙관적 락
  - 데이터 충돌이 드물다고 가정하고, 데이터 수정 시 충돌 여부를 검사하여 문제가 없으면 업데이트를 진행합니다.
  - 버전 번호 또는 타임스탬프를 이용하여 데이터 변경 여부를 확인합니다.
  - 락을 걸지 않기에 시스템 성능 저하가 적다는 장점이 있습니다.
  - 충돌이 발생하면 재시도 로직이 필요할 수 있으며, 동시에 여러 충돌이 발생하여 실패하면 10번 성공해야 할 게 7번만 성공하는 등 문제가 생길 수 있습니다.
  - 결과적으로 성공횟수에 비해 더 많은 로직을 수행해야 하므로 낭비가 발생할 수 있습니다.
  - 따라서 충돌이 드물거나, 또는 한 건만 성공하면 되는 경우 등의 상황에서 적합합니다.
- 비관적 락
  - 데이터 충돌이 빈번하다고 가정하고, 데이터에 접근할 때 락을 걸고 다른 작업을 접근하지 못 하게 막습니다.
  - java 의 synchronized, db 의 select ... for update 등 시스템적인 방법이 있습니다.
  - 데이터 충돌을 사전에 방지하여 안정성을 높일 수 있다는 장점이 있습니다.
  - 락에 따른 대기 시간이 발생하여 성능이 저하될 수 있습니다.
  - 충돌이 빈번하거나, 여러 건의 시도가 하나씩 성공해야 하는 경우 등의 상황에서 적합합니다.

</details>

## 데드락

<details>
  <summary>데드락</summary>

### 데드락의 개념

락에 의해 발생할 수 있는 사이드 이펙트로 데드락이라는 것이 있습니다. 데드락은 서로 다른 작업이 각자에게 필요로 하는 자원을 상대방이 소유하고 있어서 서로의 작업이 끝나기를 대기해버리는 상황입니다.

![데드락 이미지](https://github.com/user-attachments/assets/7d51cb86-5988-4b9f-8ac8-98ecdb98dd5f)

### 데드락 발생 요건

데드락은 아래 4가지 조건이 모두 만족되는 경우 발생될 수 있습니다. 참고로, 데드락은 비단 DB 만에서 발생하는 것이 아니라 컴퓨터 전반에서 발생할 수 있는 현상입니다. 이번 보고서에서는 DB 에서 발생하는 데드락에 초점을 두고 설명합니다.

1. 상호배제
    - 하나의 리소스는 한 번에 한 프로세스(스레드, 트랜잭션 등)만 사용할 수 있다. 즉, 그 리소스가 어떤 프로세스에 의해 잠금이 걸려있다.
    - 사용 중인 자원을 다른 프로세스가 요청하려면 그 자원에 대한 잠금이 해제될 때까지 기다려야 한다.
2. 점유와 대기
    - 한 프로세스가 한 개 이상의 리소스를 보유한 상태(점유)에서 다른 프로세스의 자원을 점유하기 위해 대기하고 있는 상황이다.
3. 비선점
    - 다른 프로세스가 점유한 자원을 강제로 가져올 수 없다. 즉, 그 프로세스가 자원에 대한 잠금을 해제할 때까지 기다려야 한다.
4. 순환 대기
    - 대기 중인 프로세스들이 서로를 기다리고 있어야 한다.

예를 들어, 언급한 재고 차감 상황에 데드락 발생 요건을 적용해보겠습니다.

트랜잭션이 TX1, TX2 로 2개가 있고, 상품은 A, B 2개가 있다고 가정해봅시다.

1. 상호배제
    - TX1 은 상품 A, B 를 잠급니다. 이때 비관적 락이 사용됩니다.
    - TX2 는 상품 A, B 를 잠급니다. 이때 역시 비관적 락이 사용됩니다.
2. 점유와 대기
    - TX1 은 상품 A 를 먼저 잠갔습니다. 그리고 이제 상품 B 를 잠글 차례입니다.
    - TX2 는 상품 B 를 먼저 잠갔습니다. 그리고 이제 상품 A 를 잠글 차례입니다.
    - 하지만 두 트랜잭션이 서로에게 필요한 상품을 잠갔기에 대기해야 하는 상황입니다.
3. 비선점
    - TX1 은 TX2 가 점유한 상품 B 를 강제로 가져올 수 없습니다.
    - 마찬가지로 TX2 는 TX1 이 점유한 상품 A 를 강제로 가져올 수 없습니다.
4. 순환 대기
    - TX1 과 TX2 는 각자에게 필요한 자원을 서로가 기다리고 있기 때문에 순환 대기 구조가 형성됩니다.

만약, 이 상황에서 서로가 동일한 시간 동안 락을 획득하지 못 한다면 두 트랜잭션 모두 실패로 끝나게 될 것입니다.

### 데드락 해결방법

데드락은 예방, 회피, 탐지&복구 등의 방법으로 해결할 수 있습니다.

이 중에서 회피 방법과 탐지&복구 방법은 데드락 회피를 위한 알고리즘적 접근이 필요하기에 필요 이상의 오버헤드라고 판단하고 예방 기법을 사용했습니다.
단, 예방기법은 불필요한 대기를 해야 하거나, 더 많은 시도를 해야 하는 등 자원낭비를 초래하는 방법이기에 항상 더 좋은 방법은 아닙니다.

데드락 예방기법은 4가지가 있습니다.
1. 상호배제 부정
    - 여러 트랜잭션이 동시에 자원에 접근하는 것을 허용하고, 충돌이 발생하는 경우 예외를 발생시키는 낙관적 락 방법이 있습니다.
    - 상품 재고는 여러 트랜잭션이 동시에 접근하고 충돌이 잦을 수 있기에 부적절하다고 판단했습니다.
    - 반면 포인트 충전을 여러 건의 요청 중에서도 한 건만 성공하면 되는 시나리오라고 생각하면 낙관적 락이 적합할 수 있습니다.
2. 점유 및 대기 부정
    - 필요로 하는 모든 자원을 한번에 획득하게 하면 가능합니다.
    - 처음에는 벌크 조회 쿼리에 락을 걸면 가능하리라 생각했지만, 현재 사용 중인 MySQL 에서는 정말로 그런지 찾아보니 공식문서에서는 "레코드를 조우한 순간"에 락을 건다고 합니다. 즉, 여러 건을 쿼리를 실행해도 내부적으로는 하나씩 레코드를 찾고 의도한 방식으로 락을 걸기에 불완전한 방법이라고 판단했습니다.
    - > InnoDB performs row-level locking in such a way that when it searches or scans a table index, it sets shared or exclusive locks on the index records it encounters. Thus, the row-level locks are actually index-record locks.
      <br> [MySQL - 17.7.1 InnoDB Locking](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking.html#innodb-next-key-locks)
    - DB 락을 사용하는 상황에서는 아쉽지만, 분산락을 사용할 때 커넥션&분산락 데드락을 해소하기 위해 사용할 수 있습니다. 이는 아래 [커넥션과 분산락 데드락](#커넥션과-분산락-데드락)에 자세히 기록해두었습니다.
3. 비선점 부정
    - 자원의 선점을 허용하게 하면 가능합니다.
    - 하지만 트랜잭션 중간에 자원을 선점하게 되면 락을 거는 이유가 없어지기 때문에 최후의 수단으로 타임아웃을 설정하여 일정 시간 후 선점을 허용하도록 해야 합니다. 그러면 데드락이 지속되지 않고, 한 트랜잭션에서 롤백되고 락을 해제하면서 다른 트랜잭션에서 레코드를 선점할 수 있게 됩니다.
    - JPA 의 @QueryHint 를 사용하여 타임아웃을 설정했습니다.
    ```
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "5000"
            )
    )
    ```
4. 순환 대기 부정
    - 접근 대상인 자원들을 선형으로 분류 및 고유번호를 할당하고, 각 트랜잭션이 자원에 접근할 때 각 고유번호를 한 쪽 방향으로 순차적으로 접근함으로써 가능합니다.
    - 조회 대상인 상품은 PK 가 BIGINT 단일 컬럼이고, AUTO_INCREMENT 가 적용되어 있어서 선형적인 고유번호를 만족하는 상황입니다.
    - 아래 코드와 같이 조회할 순서를 정렬하고 한 건씩 조회함으로써 순환 대기 부정을 달성할 수 있었습니다.
    ```
    public void deductStocks(Map<Long, Integer> itemIdStockAmountMap) {
        List<Long> itemIds = new ArrayList<>(itemIdStockAmountMap.keySet());
        Collections.sort(itemIds); // (1)
        for (Long itemId : itemIds) { // (2)
            ItemStock itemStock = itemStockRepository.findByItemIdWithLock(itemId).orElseThrow(NoSuchItemStockException::new);
            itemStock.deductStock(itemIdStockAmountMap.get(itemStock.getItem().getId()));
        }
    }
    ```
    - (1) 에서 상품 ID 를 오름차순으로 정렬하여, 순차적으로 접근할 수 있게 합니다.
    - (2) 에서는 (1) 에서 정렬한 오름차순으로만 조회를 하기 때문에 교착상태에 빠지는 것을 막을 수 있게 됩니다.

</details>

## 동시성 제어 방식

<details>
  <summary>동시성 제어 방식</summary>

### 스레드 락

자바에서 동시성 제어를 위해서 synchonized 와 Lock 두 가지를 사용해볼 수 있습니다.

#### synchronized

```
public class Counter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

- 장점
  - 키워드만으로 동기화가 가능하여 코드가 간결합니다.
  - 예외가 발생하더라도 자동으로 락이 해제됩니다.
- 단점
  - 고급 제어가 불가능하고 타임아웃 등이 불가능합니다.
- 복잡도
  - 구현이 가장 간단하고 복잡도가 낮습니다.
- 성능
  - 경쟁이 적은 경우에만 효율적입니다.
- 한계점
  - 하나의 애플리케이션 인스턴스 안에서만 제어가 가능합니다. 멀티 서버로 운영할 경우 동시성 제어가 불가능합니다.

#### Java Lock

```
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private int count = 0;
    private Lock lock = new ReentrantLock();

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
```

- 장점
  - 타임아웃 등이 가능합니다.
  - 락을 원하는 위치에 유연하게 설정할 수 있습니다.
- 단점
  - 락의 획득과 해제를 명시적으로 관리해야 합니다.
- 복잡도
  - 명시적인 락 관리가 필요합니다.
  - 접근하는 자원 하나에 대해서만 구체적으로 락을 걸어야 불필요한 자원 낭비를 막을 수 있습니다.
- 성능
  - 락 알고리즘을 적용할 수 있습니다.
  - 공정한 락으로 기아 상태를 방지할 수 있습니다.
- 한계점
    - 하나의 애플리케이션 인스턴스 안에서만 제어가 가능합니다. 멀티 서버로 운영할 경우 동시성 제어가 불가능합니다.

### DB 락 - 낙관적 락
```
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Version
    private Long version;
}
```

- 장점
  - 락을 사용하기 않기 때문에 데드락이 발생하지 않습니다.
  - 멀티 서버 환경에서도 동시성 제어가 가능합니다.
- 단점
  - 충돌 발생 시 재시도 로직이 필요할 수 있으며, 오버헤드 및 성능 저하로 이어질 수 있습니다.
- 복잡도
  - JPA 프레임워크를 사용할 경우 버전 관리 필드만 있으면 되기에 구현이 간단합니다.
  - 재시도 로직을 AOP 등으로 직접 구현해야 할 수 있습니다.
- 성능
  - 락 획득을 위한 대기가 없어 읽기 작업이 많은 경우 높은 성능을 발휘합니다.
  - 하지만 데이터 충돌이 잦은 수정 작업 등이 있다면 비효율적입니다.
- 한계점
  - 데이터 충돌이 잦은 경우 적합하지 않습니다.
  - 성공해야 할 요청이 마저 성공하지 못 해서 결국 비관적 락보다도 더 많은 자원을 사용해야 할 수 있습니다.
    - 언급했던 이커머스 시나리오에서 동시성 이슈가 발생할 수 있는 상황에서는, 비즈니스 로직 예외를 제외하면 각 요청들이 모두 성공해야 하기 때문에 적합하지 않습니다. 따라서 현재까지 구현된 이커머스 프로젝트에서 낙관적 락은 적용되지 않았습니다.
      - 포인트를 여러 번 충전하면 그 중 하나만 성공하는 게 아니라 모두 성공해야 합니다. 만약 정말로 모든 요청이 실수가 아니라 의도적으로 충전하려는 상황에서도 그렇고, PG 사를 거친 결제 승인 요청이 필요한 상황에서도 비관적 락으로 상태를 검증하기 위해 필요하다고 판단했습니다.
      > 만약 포인트만 충전하는 상황이라면, 낙관적 락으로 실수로 동시에 발생한 요청, 소위 "따닥"을 적은 리소스로 제어할 수 있습니다.
      > 
      > 하지만 실제 포인트 충전을 위해서는 PG 사에 결제 요청을 해야 할 거고, (그 경우 주문 아이디, 결제 키 등이 있다고 하더라도 같은 주문에 대하여)낙관적 락을 사용하면 각 요청이 아직 결제 완료 전 상태의 주문을 조회해서 PG 사에 여러 번 결제 승인이 요청될 수도 있습니다.
      > 
      > 이 경우까지 고려하면, 포인트 충전의 동시성 제어는 두 가지 방안이 있을 것 같습니다. 
      > 1) 낙관적 락과 PG 사 요청 시의 멱등키 사용. 단, PG 사에 따라 멱등키를 지원하지 않을 수도 있기에 PG 사에 따라 제약이 있을 수 있습니다.
      > 2) 비관적 락을 사용하면 동일한 주문 아이디에 대해서 PG 사 결제 승인 요청 전에 레코드 상태를 검증할 수 있고, PG 사가 멱등키를 지원하지 않아도 로직의 제어가 가능합니다.
      - 여러 번의 재고 차감 시도가 있을 때, 재고가 충분할 때는 모두 성공해야 합니다.
      - 여러 번의 포인트 차감 시도가 있을 때, 포인트가 충분할 때는 모두 성공해야 합니다.
  - 분산 DB 환경에서 여러 트랜잭션을 사용하게 되어 복잡도가 올라갈 수 있습니다.

### DB 락 - 비관적 락

- 장점
  - 다른 트랜잭션의 접근을 차단하여 강력한 일관성과 무결성을 보장합니다.
  - 데이터 수정과 충돌이 잦은 경우, 수많은 동시 요청에 대해 재시도를 하지 않아도 됩니다.
    - 예를 들어 낙관적 락을 사용하는 경우 재고 차감을 위해 10,000 번의 요청이 발생하면 1번의 요청만 발생하고, 다시 9,999 번의 요청이 모두 재시도되어야 하기 때문에 비효율적입니다.
- 단점
  - 데드락이 발생할 수 있기에 예방, 탐지, 복구 등 데드락 해결방법이 필요합니다.
  - 락 대기 시간으로 인해 성능이 저하될 수 있습니다.
- 복잡도
  - JPA 프레임워크를 사용할 경우 @Lock 을 사용하여 간단하게 구현할 수 있습니다.
  - 데드락 방지 로직이 필요합니다.
  - 적절한 범위의 락 사용이 필요합니다.
- 성능
  - 데이터 충돌이 잦은 경우 적합합니다.
  - 과도한 락 경합에 의해 성능이 저하될 수 있습니다.
- 한계점
  - 분산 DB 환경에서 일관성이 제공되지 않을 수 있습니다.

### 분산락

만약 우리가 제공하는 서비스의 애플리케이션 인스터스가 하나라면 Java Lock 으로 해결할 수도 있습니다. 하지만 인스턴스가 여러 개라면 일관된 락을 위해 DB Lock 을 사용할 필요가 있습니다.

마찬가지로 만약 DB 가 하나라면 DB Lock 으로 일관된 락을 제공할 수 있습니다. 하지만 만약 도메인 별로 DB 를 각각 구성한다면 DB Lock 도 부족할 수 있습니다.

근본적인 원인은 각 DB 별로 트랜잭션이 다르고, 다른 DB 의 트랜잭션 간에는 락이 공유되지 않는다는 점입니다.

서로 다른 DB 에 걸쳐서 이루어지는 전체 작업을 하나의 논리적인 트랜잭션으로 관리하고 전체 작업의 일관성을 보장해야 하는데, 이때 각 DB 간의 물리적 트랜잭션이 각각 정확하게 보장된다고 하더라도 서로 다른 서버나 DB 에서 일어나는 작업에 의한 롤백 등의 관리가 어려워집니다.

이를 극복하고자 동시성 제어를 관리하는 중앙집중식 서버를 통해 동시성 제어를 하는 전략을 분산락이라고 합니다.

분산락은 서로 다른 DB 에서 일어나는 작업들의 묶음인 논리적 트랜잭션의 진입점을 통제하는 역할을 하면서 일관성을 보장할 수 있습니다.

분산락으로 활용할 수 있는 기술은 여럿 있지만 대표적으로 Redis 와 Kafka 가 있습니다.

### Redis 의 RedLock

Redis 는 RedLock 이라고 하는 분산락을 제공합니다.

- 장점
  - NoSQL DB 로 간단한 구조를 가지고 있어 비교적 쉽게 구현이 가능합니다.
  - Lock Timeout 설정이 가능합니다.
  - Atomic 연산을 제공할 수 있습니다.
  - Pub/Sub 구조 사용으로 리소스를 절약할 수 있습니다.
    - Pub/Sub 은 락을 획득하는 방식 중 하나입니다. Pub 은 Publish, Sub 은 Subscribe 의 약자입니다. 락을 획득하려는 스레드는 락을 획득할 때까지 해당 키를 '구독(Subscribe)'하고 대기를 합니다. 그리고 레디스는 해당 키를 사용할 수 있는 순간이 오면, 구독하고 있는 스레드에게 알림을 이벤트로서 '발행(Publish)'해줍니다. 이렇게 하면 락을 획득하기 위한 경합 과정에서 과도한 요청이 발생하는 것을 줄일 수 있기에 이 방법을 많이 사용합니다.
    - 주로 대비되는 게 스핀락인데, 스핀락은 락을 획득할 때까지 일정시간의 대기 및 재요청을 반복하는 방식이기 때문에, 락 경합이 많을 수록 여러 스레드에서의 요청에 의한 부하가 크게 나타날 수 있습니다. 
    - Master-Slave 복제를 사용하여 단일 장애 지점 문제를 보완할 수 있습니다.
- 단점
  - 스냅샷을 사용할 수 있지만, 메모리 특성상 장애가 발생하여 데이터를 손실할 가능성이 있습니다.
- 복잡도
  - 설치 과정이 어렵지 않고, NoSQL 구조 DB 이기에 직관적으로 사용할 수 있습니다.
  - 락에 대한 키를 관리해야 합니다.
- 성능
  - 인메모리 기반으로 높은 성능을 제공하고, 빠르게 락을 제어할 수 있습니다.
- 한계점
  - 레디스는 단일 스레드에서 동작합니다. 따라서 복제를 위해서는 여러 개의 레디스 노드를 생성하게 되는데, 이 경우 동기화된 시계(synchronized clock)가 없기 때문에 클럭이 정확한 속도로 동작하지 않는 클럭 드리프트(Clock Drift) 현상으로 일관성이 지켜지지 않을 수 있습니다.
  - 애플리케이션 중단 또는 네트워크 지연, Java Garbage Collector 에 의한 시간 차이가 발생하여 동시성 이슈가 발생할 가능성이 있습니다.

(**한계점 참고** : [[Redis] 레디스가 제공하는 분산락(RedLock)의 특징과 한계](https://mangkyu.tistory.com/311))

### Kafka

카프카는 분산 메시지 플랫폼으로서 메시지 큐의 형태로 쓸 수도 있습니다. 이 경우 메시지 간의 순서를 보장할 수 있기 때문에 동시성 이슈가 발생하지 않도록 제어할 수 있습니다.

- 장점
  - 디스크 기반 저장으로 시스템 재시작 시에도 락 상태를 복원할 수 있습니다.
  - 브로커, 파티션을 확장하여 수평적 확장(Scale Out)이 가능합니다.
    - 분산환경에서 용이하게 사용할 수 있습니다.
    - 대용량 데이터를 처리해야 할 때도 원활하게 운영할 수 있습니다.
- 단점
  - 운영 관리가 복잡하고 전문 지식이 필요합니다.
  - 락 관리를 위해 도입하기에는 불필요한 기능들이 많아 시스템 리소스 사용량이 증가합니다.
  - 트랜잭션 범위 바깥에서 실패한 로직에 대한 복구 처리를 비롯한 락 구현을 직접 해야 합니다.
- 복잡도
  - 프로듀서, 컨슈머, 오프셋, 파티셔닝, 그리고 동일 키를 같은 큐에 보내기 위한 해싱 구현 등 설정 비용과 더불어 추가적인 모니터링 구현까지 해야 하므로 가장 높은 복잡도를 가집니다.
  - 로직 실패에 대한 추가 로직 구현이 필요하며, 알고리즘에 대한 높은 이해도가 필요합니다. 
    - 예를 들어, 애플리케이션은 로직을 수행하고 커밋을 함으로써, 카프카의 파티션을 타는 메시지들은 트랜잭션 범위를 벗어났으므로 실패되어야 할 데이터들을 직접 이전 상태로 복구시켜야 하며, 그 과정에서 발생한 예외에 대한 처리도 필요합니다.
- 성능
  - 대용량 데이터 처리에서도 원활하게 동작합니다.
  - 비동기 작업에 의한 지연이 발생할 수 있습니다.
- 한계점
  - 비동기 작업에 의해 실시간 처리가 어렵습니다.
  - 단점이 크다는 게 한계점이 될 수 있습니다. 구현 난이도가 쉽지 않기에 소규모 프로젝트에서 현실적으로 도입을 어려워하는 경우가 많습니다.

### Redisson

분산 환경을 고려하여 분산락을 구현하고자 합니다. 그리고 Kafka 의 구현 복잡도와 오버헤드를 감안하여 RedLock 을 적용하겠습니다.

Java 에는 RedLock 클라이언트로서 Redisson 이라는 라이브러리가 존재합니다. 구현 난이도도 낮은 편이며 조사했을 때 체감상 가장 많은 레퍼런스와 블로그 포스트가 존재했습니다. 이는 그만큼 사용자 층도 두텁다는 뜻이리라 생각합니다.

하지만 RedLock 에는 알고리즘적 한계가 있기 때문에 이후 시간적 여유가 허락된다면 Kafka 의 도입도 시도해보고자 합니다.

</details>

## 락 시간 측정 테스트

<details>
  <summary>락 시간 측정 테스트</summary>

> 시간 측정 테스트 코드는 STEP 12+ 브랜치에 반영되어 있습니다.
> 
> 전체 테스트 코드는 [여기](https://github.com/psam1017/hhplus-ecommerce/blob/STEP12%2B/src/test/java/hhplus/ecommerce/server/integration/infrastructure/lock/LockComparisionTest.java)를 참조해주세요.

언급된 여러 락 중에서, 제가 적용한 락은 낙관적 락, 비관적 락, 분산락 3가지였습니다.

스레드 락은 멀티 서버 환경에서 동시성을 제어할 수 없기에 적용 및 테스트할 가치를 느끼지 못 했습니다.

한편 카프카는 구현 난이도가 높아 이번에는 성능 비교 테스트를 생략했습니다.

```

    @DisplayName("낙관적 락, 비관적 락, 분산락 사이의 시간 차이를 명확하게 비교할 수 있다.")
    @Test
    void compareLock() throws InterruptedException {
        // given
        int tryCount = 5;
        long millis = 100;
        Point point = createPoint();

        // when 1 - 낙관적 락
        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch1 = new CountDownLatch(1);
        CountDownLatch endLatch1 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch1.await();
                    userFinder.findWithOptimisticLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch1.countDown();
                }
            });
        }

        long startMillis = System.currentTimeMillis();
        startLatch1.countDown();
        endLatch1.await();
        long optimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 2 - 비관적 락
        CountDownLatch startLatch2 = new CountDownLatch(1);
        CountDownLatch endLatch2 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch2.await();
                    userFinder.findWithPessimisticLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch2.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch2.countDown();
        endLatch2.await();
        long pessimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 3 - 분산락
        CountDownLatch startLatch3 = new CountDownLatch(1);
        CountDownLatch endLatch3 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch3.await();
                    userFinder.findWithDistributionLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch3.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch3.countDown();
        endLatch3.await();
        long distributionLockDuration = System.currentTimeMillis() - startMillis;

        // then
        assertThat(optimisticLockDuration).isLessThan(pessimisticLockDuration); // (1)
        assertThat(optimisticLockDuration).isLessThan(distributionLockDuration); // (2)
        assertThat(pessimisticLockDuration).isLessThanOrEqualTo(distributionLockDuration); // (3)
    }

```

테스트 의도는 다음과 같습니다.

어떤 레코드를 가져올 때마다 의도적으로 시간지연을 발생시킵니다. 위 테스트에서는 일괄적으로 0.1 초의 지연이 적용되어 있습니다.
  1) 낙관적 락은 서로의 작업을 대기하지 않으므로 0.1초의 지연이 중첩되어 적용될 것이라 예상했습니다.
  2) 비관적 락은 서로의 작업을 대기하므로 0.1초의 지연이 중첩되지 않고 요청 횟수 만큼 지연될 것이라 예상했습니다.
  3) 분산락의 성능을 측정한 과정이 재밌습니다.
      1) 분산락을 걸고, 트랜잭션 안에서는 DB 락을 낙관적 락으로 조회를 하더라도 분산락을 기다리는 시간이 있기 때문에 비관적 락과 동일한, 혹은 초과한 지연이 발생할 것이라 예상했습니다.
      2) 분산락을 걸고, 트랜잭션 안에서는 DB 락을 비관적 락으로 조회하면 비관적 락 행동에 추가로 분산락 획득 및 반납 과정이 생기기 때문에 무조건 비관적 락보다 확실하게 긴 지연이 발생할 것이라 예상했습니다.

위 테스트를 @RepeatedTest 를 사용해서 100번 정도 돌려보았고, 위의 추측 중 낙관적 락과 비관적 락에 대한 추측은 예상대로였습니다.
  - 테스트 주석 (1) 의 결과로, 낙관적 락은 언제나 비관적 락보다 조회 시간이 빨랐습니다. 약 0.1xx초 정도의 시간이 걸렸습니다.
  - 테스트 주석 (2) 의 결과로, 낙관적 락은 언제나 분산락보다 조회 시간이 빨랐습니다. 약 0.5xx초 정도의 시간이 걸렸습니다.

하지만 테스트 주석 (3) 의 결과, 비관적 락이 무조건 분산락보다 빠르다고는 할 수 없음을 확인할 수 있었습니다.
  - 분산락(낙관적 락) 테스트 결과, 100번에 1번 정도는 분산락이 더 빠르게 시간이 측정되었습니다. 낙관적 락은 DB 에 레코드를 잠그지 않고 바로 조회하기 때문에 어느 정도 기대와 다를 수는 있겠다고 내심 생각하고 있었습니다.
  ![lock-comparision-1](https://github.com/user-attachments/assets/0fd9db6d-6a17-4ddd-aaf3-af2d9244d8e5)

  - 분산락(비관적 락) 테스트 결과, 기대했던 것과 달리 여전히 100번에 1번 정도는 분산락이 더 빠르게 시간이 측정되었습니다. 비록 분산락 내부 트랜잭션에서도 비관적 락을 사용했음에도 불구하고 말이죠.
  ![lock-comparision-2](https://github.com/user-attachments/assets/9c0a7f44-015c-424d-b3c0-a61efb0d4ae9)

비관적 락과 분산락 비교를 위해 스레드 개수를 훨씬 크게 잡으면 또 다르게 동작할 가능성도 있지만, 만약 둘 사이에 정말로 확실한 성능 차이가 있다면 동시 요청 횟수가 적더라도 언제나 똑같은 결과를 내야 한다고 생각하고 스레드를 다소 작게 설정하고 반복 테스트로 검증하였습니다.

그러한 가정 하에 두 방식의 조회 성능은, 적은 수의 동시 요청 하에서 가끔은 Java 의 System.currentTimeMillis() 의 오차보다 더 적게 차이가 날 정도로 미비하다는 결론을 도출했습니다.

언급했던 것처럼 Redis 가 메모리 기반으로 동작하기 때문에 높은 성능의 락 제어를 제공한다는 사실을 테스트를 통해 확인할 수 있었습니다.

</details>

## 트러블 슈팅

<details>
  <summary>트러블 슈팅</summary>

- 이 내용은 분산락을 적용하는 과정에서 겪은 문제를 정리한 내용입니다. 분산락 적용은 STEP12 브랜치에 적용되어 있기에 자세한 코드 확인이 필요하신 분은 [STEP12 브랜치](https://github.com/psam1017/hhplus-ecommerce/tree/STEP12)를 참고해주세요.

### 커넥션과 분산락 데드락

- 최종 포인트 충전 및 사용 로직은 분산락을 적용하지 않고 낙관적 락만 적용했습니다. 실제 최종 코드와 다름을 참고해주세요.
  - 분산락은 Pub/Sub 패턴으로 대기를 강제하게 되어 낙관적 락 사용이 어려워집니다.
  - 포인트 DB 가 분리되어 있다고 하더라도 분산락을 사용하지 않음으로 발생할 수 있는 사이드 이펙트를 찾지 못해 처음에 적용했던 분산락을 다시 제거했습니다.
  - 그래도 아래 또 다른 트러블 슈팅인 [보상 트랜잭션](#보상-트랜잭션)과 이어지는 내용이니 먼저 읽고 가시면 좋습니다:)

#### 문제 현상

포인트 충전 및 사용하는 로직에서 분산락을 적용하고 '10건'의 동시 충전 요청을 하는 통합테스트를 작성해본 결과 타임아웃 예외가 발생했습니다.

![trouble-shoot-1](https://github.com/user-attachments/assets/0016387a-13b6-4972-b4e3-23e55b517f9b)

#### 원인 분석

로그를 추적해본 결과 DB 데이터와 관련된 분산락을 획득하는 과정 자체는 문제가 없는데, 커넥션이 부족한 상황에서 새로운 커넥션을 요구하기에 데드락이 발생한 것입니다.

![trouble-shoot-2](https://github.com/user-attachments/assets/04a65950-4dfa-427a-89cf-c2c11180bded)

혹시나 해서 커넥션 개수를 기본 '10개' 에서 '20개'로 바꿔보니 통과하는 것을 확인했습니다. 커넥션 부족이 발생한 이유는 분산락을 획득하는 과정에서 트랜잭션 전파 속성을 REQUIRES_NEW 로 설정했었는데, 10건의 요청이 각자 10개의 커넥션을 하나씩 획득한 상태에서 새로운 커넥션을 요구했기 때문입니다.

트랜잭션 전파 속성으로 REQUIRES_NEW 로 설정해야 하는 이유는 락의 일관성 보장을 위한 것입니다.

![lock-after-tx](https://github.com/user-attachments/assets/a6978dd1-2f62-47a4-992d-3685d7a7d2ed)

그림에서 보이는 것과 같이 락을 획득하기 전에 트랜잭션이 시작되면 같은 상태(시점)의 레코드를 조회하고 이를 변경하기 때문에 동시성 이슈가 발생할 수 있습니다.

![lock-before-tx](https://github.com/user-attachments/assets/50c1dcf6-2e14-4b09-aab6-30d42773a36c)

이를 막기 위해서는 락을 획득한 이후 새로운 트랜잭션을 시작하고 이를 커밋(롤백)한 다음 락을 해제해야 하기 때문에 이를 위한 안전장치로써 트랜잭션 전파 속성을 REQUIRES_NEW 로 설정했습니다.

#### 문제 해결

![trouble-shoot-3](https://github.com/user-attachments/assets/933fbdab-b023-456a-90b7-f907c820ceec)

```
서비스 tx {
    락 획득{
        메서드 tx {
            // 로직 실행
        }
    }
}
```

저의 이전 코드는 위와 같았습니다. 서비스 클래스 위에 @Transactional 이 적용되어 있습니다. 즉 모든 메서드가 커넥션을 획득하게 됩니다. 그리고 usePoint 메서드는 호출되면 새로운 트랜잭션을 시작하여 커넥션을 획득하려고 합니다.

한 스레드가 서비스 로직에 진입하고 메서드를 호출할 때 AOP 로 새로운 커넥션을 획득하려는 상황에서 다른 스레드에서 서비스 로직에 진입하여 이미 커넥션을 확보했다면, 결과적으로 락을 획득하지 못한 스레드는 락 해제를 기다리고, 락을 획득한 스레드는 커넥션을 기다리는 교착상태가 발생하게 됩니다.

![trouble-shoot-4](https://github.com/user-attachments/assets/950ed4a1-69c2-484b-bb97-263e5e193097)

```
서비스 {
    락 획득 {
        메서드 tx {
            // 로직 실행
        }
    }
}
```

위의 테스트에서 했던 것처럼 커넥션을 늘릴 수도 있겠지만, 하나의 로직에서 얼마나 많은 커넥션을 요청하게 될 지 예측할 수 없는 상황에서 이 방법은 현실적이지 못 합니다.

저는 서비스 클래스에 적용되었던 @Transactional 을 문제를 해결했습니다. 이는 데드락 해소 기법 중에서도 점유 및 대기 부정 사례에 해당합니다. 로직 수행 전에 필요한 자원을 모두 확보할 수 있도록 필요로 하는 자원인 DB 커넥션 개수를 최적화함으로써 데드락을 예방했기 때문입니다.

### 보상 트랜잭션

#### 문제 현상

이제 커넥션&분산락 데드락을 해결했으니 과제를 끝낼 수 있겠다! 하는 희망과 함께 주문 분산락을 적용하려고 했는데, 트랜잭션 최소화에 의한 문제가 바로 생겼습니다.

하나의 논리적 트랜잭션 안에서, 분산락 사용을 위해 (propagation = REQUIRES_NEW 속성으로 획득한)새로운 트랜잭션이 커밋되고 나면, 익셉션 발생에 의한 자동 롤백이 되지 않는다는 것입니다.

이전까지는 @Service 와 @Transactional 을 무조건 함께 사용했었는데, 하나의 논리적 트랜잭션 안에서 퍼사드 패턴으로 여러 서비스를 각각의 물리적 트랜잭션을 가지게 하면서 호출하니, 해당 트랜잭션들이 커밋된 이후 발생하고 나면 이를 롤백할 방법을 못 찾은 것입니다.

#### 원인 분석

```
결제 시작 {
    tx1 : 재고 차감();
    tx2 : 포인트 차감();
    tx3 : 주문 생성();
    !예외 발생!
}
```

언급한 대로 각각의 로직들은 트랜잭션이 종료됨과 동시에 커밋을 하기 때문에 예외가 생겨도 롤백이 되지 않습니다.

#### 문제 해결

멘탈이 흔들리던 상황에서 문득 코치님이 멘토링 시간에 '보상 트랜잭션'이라는 언급을 했던 게 기억이 났습니다. 제가 분산락 적용하다가 새로운 트랜잭션이 커밋하면 어떻게 롤백하냐, 라는 질문을 하다가 들은 답변이었는데 그때는 보상 트랜잭션이 그냥 코치님 개인만의 표현 같은 건줄 알았습니다.

혹시나 해서 검색해보니 보상 트랜잭션이라는 개념과 패턴이 존재한다는 것을 깨달았습니다. 보상 트랜잭션은 일련의 작업 중 일부가 실패했을 때, 이전 작업들을 복구시키기 위해 수행되는 트랜잭션입니다.

분산락을 적용하는 여러 블로그 포스트들이 대부분 저와 같은 상황을 겪고 있었기에 해결방법을 찾기도 용이했습니다.

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
                log.error("compensation action failed", e2);
            }
        }
        throw e;
    }
}

private Long processOrder(OrderCommand.CreateOrder command, Deque<Runnable> compensationActions) {
    Set<Long> itemIds = command.toItemIds();
    Map<Long, Integer> itemIdStockAmountMap = command.toItemMap();
    User user = userService.getUser(command.userId());
    List<Item> items = itemService.findItems(itemIds);

    for (Long itemId : itemIdStockAmountMap.keySet()) {
        ItemStock itemStock = itemService.getItemStockByItemId(itemId);
        itemService.deductStock(itemStock.getId(), itemIdStockAmountMap.get(itemId));
        compensationActions.push(() -> itemService.restoreStock(itemStock.getId(), itemIdStockAmountMap.get(itemId)));
    }

    Point point = pointService.getPointByUserId(command.userId());
    int usedPoint = pointService.usePoint(point.getId(), items, itemIdStockAmountMap);
    compensationActions.push(() -> pointService.chargePoint(point.getId(), usedPoint));

    Order order = orderService.createOrderAndItems(command, user, items);
    compensationActions.push(() -> orderService.cancelOrder(order.getId()));

    cartService.deleteCartItems(command.userId(), itemIds);

    orderDataPlatform.saveOrderData(itemIdStockAmountMap);

    return order.getId();
}
```

위에서 보이는 것과 같이 compensationActions 라는 데크를 생성합니다.
  - 참고로 Deque 는 스택처럼 사용하려는 의도인데, Stack 객체를 사용하지 않는 이유는, Stack 의 메서드들이 synchronized 를 사용해서 성능 저하 우려가 있기 때문입니다.

기존 코드에는 성공 로직 밖에 없었으나, 이 코드에서는 실패 시 이를 복구하는 로직이 필요한 데마다 보상 트랜잭션 메서드를 compensationActions 에 담습니다.

이후 정말로 예외가 발생하는 경우 이 메서드들이 실행하여 논리적 트랜잭션의 종료 전에 데이터들을 복구합니다.

한 가지 아쉬운 점은, 제가 구현한 코드는 보상 트랜잭션에서 발생한 실패에 대한 핸들링이 부족하다는 것입니다. 따라서 로깅, 추가로직 구현, 알림 전송, 모니터링 시스템 구현 등의 방법으로 보상 트랜잭션의 실패에 대응할 체계를 구축할 필요가 있습니다.

```
@DisplayName("주문 생성 이후에 로직이 실패하면 트랜잭션 보상 로직으로 주문을 취소하고 포인트와 재고를 복원시킬 수 있다.")
@Test
void createOrder_withFailure() {
    // mock
    willThrow(new RuntimeException())
            .given(orderDataPlatform).saveOrderData(Mockito.anyMap());

    // given
    User user = createUser("testUser");
    Point point = createPoint(50000, user);
    Item item1 = createItem("item1", 1000);
    createItemStock(10, item1);
    Item item2 = createItem("item2", 2000);
    createItemStock(20, item2);

    OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
            user.getId(),
            List.of(
                    new OrderCommand.CreateOrderItem(item1.getId(), 10),
                    new OrderCommand.CreateOrderItem(item2.getId(), 20)
            ));

    // when
    // then
    assertThatThrownBy(() -> orderFacade.createOrder(command))
            .isInstanceOf(RuntimeException.class);

    assertThat(orderJpaRepository.findAll()).isEmpty();
    assertThat(orderItemJpaRepository.findAll()).isEmpty();

    point = pointJpaRepository.findById(point.getId()).orElseThrow();
    assertThat(point.getAmount()).isEqualTo(50000);

    List<ItemStock> itemStocks = itemStockJpaRepository.findAll();
    assertThat(itemStocks).hasSize(2)
            .extracting(is -> tuple(is.getId(), is.getAmount()))
            .containsExactlyInAnyOrder(
                    tuple(item1.getId(), 10),
                    tuple(item2.getId(), 20)
            );
}
```

보상 트랜잭션 로직은 TDD 로 구현하고, 이후 OrderFacade#createOrder 로직 수행 중에 발생하는 예외에 데이터들이 복구가 되는지를 확인하는 테스트 코드를 작성하여 보상 트랜잭션의 동작을 검증할 수 있었습니다.

</details>
