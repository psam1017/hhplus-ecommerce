# 이커머스 캐시 적용 보고서

## 목차

1. [캐시 이해하기](#캐시-이해하기)
2. [캐싱 전략](#캐싱-전략)
3. [캐싱할 API 와 전략 선정하기](#캐싱할-API-와-전략-선정하기)
4. [상위 상품 조회 캐싱](#상위-상품-조회-캐싱)
5. [상품 전체 목록 조회 캐싱](#상품-전체-목록-조회-캐싱)
6. [결론](#결론)

## 캐시 이해하기

<details>
  <summary>캐시 이해하기</summary>

- 캐시 : 원본 저장소보다 빠르게 가져올 수 있는 임시 데이터 저장소
- 캐싱 : 캐시에 접근해서 데이터를 빠르게 가져오는 방식
- 캐시 히트 : 데이터 요청 시 캐시에 데이터가 존재하여 빠르게 반환되는 경우
- 캐시 미싱 : 데이터 요청 시 캐시에 데이터가 존재하지 않아 원본 저장소에서 데이터를 가져와야 하는 상황
- 캐시 워밍 : 동시에 발생한 여러 요청에서 캐시 미스가 발생해서 DB 에 부하가 발생하거나, 캐시에 동일한 데이터를 여러 번 삽입하려고 해서 부하가 발생하는 경우를 막기 위해 미리 캐시에 DB 데이터를 넣어두는 작업 
- Expiration : 캐시된 데이터의 유효기간이 지나 캐시에서 삭제되는 과정
- Eviction : 캐시에 저장 공간이 부족할 때 오래된 데이터나 덜 중요한 데이터를 제거하는 과정

> 데이터 조회 성능을 개선하는 방법은 여러 가지가 있습니다.
> 
> - SQL 튜닝
> - Replication 사용
> - 샤딩
> - 캐싱 활용
> - DB 스케일업
>
> 이러한 기법들은 각각의 장단점과 함께 도입/관리 비용 등도 고려해야 합니다. 예를 들어 이번에 적용하게 된 캐시 사용만 하더라도 Redis 등의 추가적인 시스템이나 리소스 등의 관리를 필요로 합니다.
>
> 한편 SQL 튜닝은 인덱스 등을 활용해서 추가적인 비용 없이도 조회 성능을 개선시킬 수 있으며, 나아가서는 아무리 캐시가 잘 되어 있더라도 SQL 자체가 비효율적으로 작성됐다면 캐시의 적용 범위를 넘어선 부분에 대한 조회 성능은 개선될 수 없을 겁니다. 캐시에 DB 의 모든 내용을 담을 수는 없으니까요.
>
> 따라서 캐싱도 데이터 조회 성능을 높이기 위한 좋은 전략이지만 우선적으로 적용할 수 있는 더 효율적인 기법은 없는지 찾아보는 과정이 선행되어야 할 것입니다.

</details>

## 캐싱 전략

<details>
  <summary>캐싱 전략</summary>

### 캐시 읽기 전략

#### Look Aside

데이터를 찾을 때 캐시에서 먼저 데이터를 찾고, 만약 없다면 DB 에서 찾는 전략입니다.

- 장점
  - 캐시와 DB 가 분리되므로 장애 대비 구성이 원활하고 캐시에 장애가 발생해도 서비스 자체에는 문제가 없음
- 단점
  - 캐시에 장애가 발생하면 순간적으로 DB 에 요청이 몰릴 수 있음
    - 캐시 클러스터를 구축하여 가용성을 높이는 방안을 고려해야 함
  - 캐시에 데이터가 삽입된 후, DB 가 업데이트되는 등의 상황에서 서로 간의 불일치가 발생할 수 있음
- 적합한 사례
  - 반복적 읽기가 많은 경우에 적합

#### Read Through

무조건 캐시를 통해서만 데이터를 읽어오는 전략

- 장점
  - 캐시와 DB 간의 데이터 동기화가 항상 이루어지므로 서로 간의 불일치가 발생하지 않음
  - DB 에 대한 조회 접근을 최소화할 수 있음
- 단점
  - 데이터 동기화를 캐시에 위임하므로 조회 속도가 느림
  - 캐시에서 장애가 발생하면 조회 서비스 이용이 불가능해짐
    - 캐시 클러스터를 구축하여 가용성을 높이는 방안을 고려해야 함
- 적합한 사례
  - 반복적 읽기가 많은 경우에 적합

### 캐시 쓰기 전략

#### Write Back

DB 에 저장할 데이터를 캐시에 모아놨다가 배치 작업을 통해 DB 에 일괄 반영함

- 장점
  - DB 에 대한 쿼리 비용과 부하를 줄일 수 있음
- 단점
  - 캐시에서 오류가 발생하면 데이터가 영구적으로 소실될 수 있음
    - 마찬가지로 클러스터를 구축하는 것과 더불어 영속성 기능 등을 사용해서 데이터의 소실을 최소화할 수 있음
- 적합한 사례
  - 쓰기 작업이 빈번하면서 읽기에 대한 리소스가 많이 요구되는 서비스에 적합

#### Write Through

DB 와 캐시에 동시에 데이터를 저장하는 전략. DB 에 저장하는 작업은 캐시에게 위임함.

- 장점
  - DB 와 캐시가 항상 동기화되므로 캐시 데이터를 항상 최신 상태로 유지함
- 단점
  - 쓰기 작업이 두 배로 발생하므로 성능적으로 손해를 볼 수 있음
  - 자주 사용하지 않는 불필요한 리소스까지 캐시에 담아야 함
- 적합한 사례
  - 데이터 유실을 최소화해야 하는 서비스에 적합

#### Write Around

캐시는 갱신하지 않고 DB 에만 저장함. 이후 캐시 미스가 발생할 때 DB 와 캐시에 모두 데이터를 저장함

- 장점
  - 삽입 작업을 최소화하므로 성능적인 이득을 볼 수 있음
- 단점
  - 캐시를 갱신하지 않으므로 캐시와 DB 간 불일치가 발생할 수 있음
    - 캐시의 만료기간(TTL)을 적절히 설정할 필요가 있음
- 적합한 사례
  - 데이터 불일치가 심각한 문제를 초래하지 않고 캐시 쓰기 작업에 의한 부하를 최소화하여 성능적인 이득을 기대하는 경우에 적합

</details>

## 캐싱할 API 와 전략 선정하기

<details>
  <summary>캐시 적용할 API 선정하기</summary>

### 선정 기준

캐시를 정확하게 적용하려면 실제 사용자들의 호출 경향을 파악해야 합니다.

이번 과제에서는 사용 데이터를 수집하기는 어려우므로 다른 선정 기준을 찾아야 합니다. 캐시는 아래와 같은 특성이 있을 때 효과가 좋습니다.

- 자주 사용된다.
- 자주 변경되지 않는다.
- 사용 시 성능 향상을 기대할 수 있다.

이러한 기준을 먼저 고려하고, API 들의 개별적 특성을 개인적으로 고민하면서 캐싱할 대상을 추려본 결과 다음과 같았습니다.

### 캐싱할 API

- 기간별 매출 상위 상품 조회 : `GET /api/items/top`
  - 읽기 전략 : `Look Aside` 전략을 적용하여 반복적 읽기에 대한 성능을 높입니다.
    - 사용자가 페이지에 랜딩했을 때 가장 우선적으로 보여지는 API 이므로 사용 빈도가 높습니다.
    - 여러 사용자가 같은 자원을 조회하는 공유 데이터(Shared Data)입니다. 따라서 동일한 데이터가 자주 사용될 확률이 높습니다.
    - 현재 쿼리는 GROUP BY, ORDER BY 등을 복잡하게 사용하고 있기에 쿼리 실행계획이 비교적 복잡합니다. 따라서 캐싱을 적용하면 무거운 쿼리를 더 적게 호출하므로 성능 향상을 기대할 수 있습니다. 아래의 쿼리 실행계획 분석 결과 조회하는 데 14초 이상이 걸리는 것을 알 수 있습니다.
    ```
     -> Limit: 5 row(s)  (actual time=14300..14300 rows=5 loops=1)
    -> Sort: sum((oi1_0.quantity * oi1_0.price)) DESC, limit input to 5 row(s) per chunk  (actual time=14300..14300 rows=5 loops=1)
        -> Table scan on <temporary>  (actual time=13855..14134 rows=1e+6 loops=1)
            -> Aggregate using temporary table  (actual time=13855..13855 rows=999999 loops=1)
                -> Nested loop inner join  (cost=1.04e+6 rows=997425) (actual time=0.48..9852 rows=1e+6 loops=1)
                    -> Inner hash join (no condition)  (cost=102290 rows=997425) (actual time=0.459..1535 rows=1e+6 loops=1)
                        -> Table scan on i1_0  (cost=102290 rows=997425) (actual time=0.392..1365 rows=1e+6 loops=1)
                        -> Hash
                            -> Filter: ((o1_0.`status` = 'ORDERED') and (o1_0.order_date_time between '2024-11-02T00:00:00' and '2024-11-05T00:00:00'))  (cost=0.35 rows=1) (actual time=0.046..0.0514 rows=1 loops=1)
                                -> Table scan on o1_0  (cost=0.35 rows=1) (actual time=0.0359..0.0411 rows=1 loops=1)
                    -> Filter: (oi1_0.order_id = o1_0.id)  (cost=0.845 rows=1) (actual time=0.00756..0.00809 rows=1 loops=1e+6)
                        -> Index lookup on oi1_0 using idx_order_items_item_id (item_id=i1_0.id)  (cost=0.845 rows=1) (actual time=0.00739..0.00785 rows=1 loops=1e+6)
    ```
    - 기간별 매출 상위 상품 조회는 일별로 조회 결과가 바뀌기 때문에 데이터 불일치 가능성이 적습니다.
    - 데이터 불일치가 심각한 문제를 초래하는 상황을 발견하지는 못 했습니다.
      - 어떤 상품이 판매금지, 또는 노출금지로 상태가 되어 조회에서 제외되어야 하는 상황이 생길 수 있습니다. 이때는 이 상품이 포함된 캐시를 날리는 식으로 대응할 수 있습니다.


- 상품 전체 조회 : `GET /api/items`
  - 읽기 전략 : `Look Aside` 전략을 적용하여 반복적 읽기에 대한 성능을 높입니다.
    - 상품 조회는 주문 및 장바구니 기능 사용을 위해 필수로 사용해야 하는 API 이므로 사용빈도가 높습니다.
    - 여러 사용자가 같은 자원을 조회하는 공유 데이터(Shared Data)입니다. 따라서 동일한 데이터가 자주 사용될 확률이 높습니다.
    - **검색조건이 없을 때에 한정하여** 상품의 신규 등록 빈도보다 조회 빈도가 더 높을 때 데이터 불일치 가능성이 적습니다.
    - 검색조건이 있는 경우에는, 검색 조건이 사용자마다 다르기에 저장공간을 과도하게 차지할 우려가 있습니다.
      - 하지만 그럼에도 사용자들이 많이 검색한 키워드가 있고, 적게 검색한 키워드가 있습니다. 만약 검색한 키워드들을 아카이빙할 수 있다면 이를 조회성능을 높이는데 활용할 여지가 생깁니다.
    - 데이터 불일치가 심각한 문제를 초래하는 상황을 발견하지는 못 했습니다.
      - 어떤 상품이 판매금지, 또는 노출금지로 상태가 되어 조회에서 제외되어야 하는 상황이 생길 수 있습니다. 이때는 이 상품이 포함된 캐시를 날리는 식으로 대응할 수 있습니다.

---

### 캐싱하지 않을 API

- 포인트 잔액 조회 : `GET /api/users/{userId}/points`
  - 읽기 전략 : 적용하지 않습니다.
    - 사용자 ID 를 받아서 인덱스를 사용할 수 있으므로 DB 에서 빠른 조회가 가능합니다.
    - 오직 1명에 의해서만 조회되므로 자주 사용되지 않습니다.
    - 캐시 저장 시 공간을 과도하게 차지할 우려가 있습니다.


- 포인트 충전 : `POST /api/users/{userId}/points/charge`
  - 쓰기 전략 : 조회 시 캐시를 사용하지 않으므로 쓰기 전략은 적용하지 않습니다.


- 장바구니 조회 : `GET /api/users/{userId}/carts/{itemId}`
  - 읽기 전략 : 적용하지 않습니다.
    - 장바구니는 사용자 ID 를 받아서 인덱스를 사용할 수 있으므로 DB 에서 빠른 조회가 가능합니다.
    - 다만, 장바구니에 캐싱하는 데이터에 대한 보안 수준이 아주 중요하지 않으면서, 빠른 조회를 원한다면 사용자의 로컬 캐시나 토큰을 사용하고 `Look Aside` 전략을 적용할 수는 있습니다. Redis 가 유일한 캐시는 아닙니다.


- 장바구니 삽입 : `PUT /api/users/{userId}/carts/{itemId}`
  - 쓰기 전략 : 장바구니에 캐싱을 하지 않는다면 쓰기 전략을 필요하지 않습니다.
    - 다만, 사용자 로컬 캐시나 토큰으로 캐싱을 한다면 삽입 직후 다시 조회 API 를 한 번만 호출하여 사용자 로컬 캐시의 장바구니 데이터를 대체하는 식으로 캐시를 최신화할 수 있습니다.


- 장바구니 삭제 : `DELETE /api/users/{userId}/carts`
  - 쓰기 전략 : 장바구니에 캐싱을 하지 않는다면 쓰기 전략을 필요하지 않습니다.
    - 다만, 사용자 로컬 캐시나 토큰으로 캐싱을 한다면 삽입 직후 다시 조회 API 를 한 번만 호출하여 사용자 로컬 캐시의 장바구니 데이터를 대체하는 식으로 캐시를 최신화할 수 있습니다.


- 주문 목록 조회 : `GET /api/users/{userId}/orders`
  - 읽기 전략 : 장바구니는 사용자 ID 를 받아서 인덱스를 사용할 수 있으므로 DB 에서 빠른 조회가 가능합니다. 한편, 장바구니와는 다르게 삭제되지 않고 계속 쌓이는 경향이 높은 데이터이므로 사용자 로컬 캐시에 담기에도 부담이 될 수 있습니다.


- 주문 상세 조회 : `GET /api/users/{userId}/orders/{orderId}`
  - 읽기 전략 : 주문 ID 를 받아서 인덱스를 사용할 수 있으므로 DB 에서 빠른 조회가 가능합니다. 또한 사용자의 주문 데이터는 오직 1명에 의해서만 조회되므로 자주 사용되지 않고, 저장공간을 과도하게 차지할 우려가 있습니다.


- 주문 및 결제 수행 : `POST /api/users/{userId}/orders`
  - 쓰기 전략 : 조회 시 캐시를 사용하지 않으므로 쓰기 전략은 적용하지 않습니다.


</details>

## 상위 상품 조회 캐싱

<details>
  <summary>상위 상품 조회 캐싱</summary>

### 적용 전 TPS

k6 를 사용하여 30명의 사용자가 1분 동안 상위 상품 조회 API 를 요청하는 시나리오로 테스트해봤습니다.

```
k6 run --vus 30 --duration 1m api-items-top.js
```

![no-cache-3](https://github.com/user-attachments/assets/a3f7b87c-930d-4717-a9ab-88b0686bb0b4)

상위 상품 API 를 호출했을 때 `0.6 TPS` 가 측정되었습니다.

### 적용 후 TPS

동일한 상황에서 조회 코드에 캐시를 아래와 같이 적용하고 다시 테스트해봤습니다.

```
  @Cacheable(
          cacheNames = CacheName.ITEMS_TOP,
          key = "T(java.time.LocalDate).now().toString()"
  )
  public List<Item> findTopItems() {
      LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
      LocalDateTime startDateTime = endDateTime.minusDays(3);
      return itemRepository.findTopItems(startDateTime, endDateTime);
  }
```

캐시의 key 는 일자별 데이터를 적용하여, 유효하지 않은 캐시는 사용자에게 전달되지 않도록 했습니다. 참고로 TTL 은 24시간입니다.

![with-cache-3](https://github.com/user-attachments/assets/11384780-4556-4826-a346-ac6cd426cafa)

동일한 API 에 캐싱을 적용한 결과 `2514 TPS` 가 측정되어 성능이 향상되었음을 확인할 수 있었습니다.

</details>

## 상품 전체 목록 조회 캐싱

<details>
  <summary>상품 전체 목록 조회 캐싱</summary>

### 적용 전

k6 를 사용하여 30명의 사용자가 1분 동안 상품 전체 목록 조회 API 를 요청하는 시나리오로 테스트해봤습니다.

![no-cache-2](https://github.com/user-attachments/assets/0b5eade8-a1a5-4b12-8276-cc0a61022753)

처음에는 전체 상품 API 를 호출한 결과 `13 TPS` 가 측정되었습니다.

### 1차 적용

상품 목록을 가져오는 쿼리에서 병목이 발생한다고 판단하여 아래와 같이 목록을 리스트로 조회하는 메서드에 캐싱을 적용했습니다.

```
  @Cacheable(
          cacheNames = CacheName.ITEMS_PAGE,
          condition = """
                  #searchCond.page() < 100
                  && #searchCond.size() == 10
                  && T(java.util.List).of("id", "price").contains(#searchCond.prop())
                  && T(java.util.List).of("asc", "desc").contains(#searchCond.dir())
                  && #searchCond.keyword() == null || #searchCond.keyword().isBlank()
                  """,
          key = "T(String).format('page:%d:size:%d:prop:%s:dir:%s', #searchCond.page(), #searchCond.size(), #searchCond.prop(), #searchCond.dir())"
  )
  public List<Item> findItemsBySearchCond(ItemCommand.ItemSearchCond searchCond) {
      return itemRepository.findAllBySearchCond(searchCond);
  }
```

특정 조건 아래에서, 검색어가 없을 때는 캐시가 생성되도록 조건을 걸고 캐싱을 적용했습니다. 참고로 TTL 은 10분입니다.

![with-cache-2](https://github.com/user-attachments/assets/9e576479-7174-4104-949b-e2ff2a77118d)

캐싱 적용 후 API 를 호출한 결과 `14 TPS` 로 성능향상이 없음을 확인했습니다.

### 2차 적용

무엇이 잘못된 것인지 이것 저것 테스트해보다가 Pagination 에서 필요로 하는 전체 개수를 조회하는 count 쿼리에서 병목이 생긴다는 것을 찾아냈습니다.

```
-> Aggregate: count(distinct i1_0.id)  (cost=200230 rows=1) (actual time=496..496 rows=1 loops=1)
     -> Covering index scan on i1_0 using PRIMARY  (cost=100488 rows=997425) (actual time=0.057..228 rows=1e+6 loops=1)
```

위 실행결과에서는 0.5초 정도의 시간이 걸렸지만, 최대 1.5초 정도의 시간이 걸릴 때도 있었습니다. 따라서 이 count 쿼리를 실행시키는 메서드에도 캐싱을 추가로 적용하기로 했습니다.

```
  @Cacheable(
          cacheNames = CacheName.ITEMS_PAGE,
          condition = "#searchCond.keyword() == null || #searchCond.keyword().isBlank()",
          key = "'count'"
  )
  public long countItemsBySearchCond(ItemCommand.ItemSearchCond searchCond, int contentSize) {
      if (searchCond.size() > contentSize) {
          if (searchCond.getOffset() == 0 || contentSize != 0) {
              return searchCond.getOffset() + contentSize;
          }
      }
      return itemRepository.countAllBySearchCond(searchCond);
  }
```

상품 목록 페이징하는 findItemsBySearchCond 메서드와 비슷하게 검색어가 없을 때만 캐싱을 적용하고 있습니다. 참고로 TTL 은 10분입니다.

![with-cache-4](https://github.com/user-attachments/assets/4b205979-3d52-4e46-a7fe-117c7aa99be6)

다시 테스트를 해본 결과 이번에는 `1410 TPS` 가 측정되어 성능이 향상되었음을 확인할 수 있었습니다. 

</details>

## 결론

<details>
  <summary>결론</summary>

서버의 성능은 사용자 경험 및 매출과 이어지는 중요한 요소 중 하나입니다.

이 성능을 이끌어내기 위해서는 인덱싱 같은 DB 튜닝과 더불어 캐시와 같은 여러 기법을 적용해야 합니다.

특히 캐시는 '자주 사용되고, 덜 변경되고, 성능 향상이 있을 때' 적용하면 좋습니다.

이번 캐싱 적용 과제에서는 상품 조회 API 2가지에 캐싱을 적용하여 성능 향상을 이끌어낼 수 있었습니다.

Spring 에서는 @Cacheable 을 제공하여 캐시를 쉽게 적용할 수 있도록 하고 있으나, 이와 더불어 TTL, condition, 읽기 전략과 쓰기 전략 등을 적절히 정하여 비즈니스에 적합한 캐시 전략을 세울 필요가 있습니다.

</details>
