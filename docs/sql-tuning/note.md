# 이커머스 SQL 튜닝 보고서(Index 위주로)

## 목차

1. [요약](#요약)
2. [SQL 분석 계획](#sql-분석-계획)
3. [주문별 주문 총액 조회](#주문별-주문-총액-조회)
4. [상품 목록 페이징 조회(생성 순서로 정렬)](#상품-목록-페이징-조회생성-순서로-정렬)
5. [상품 목록 페이징 조회(가격 순서로 정렬)](#상품-목록-페이징-조회가격-순서로-정렬)
6. [상품 전체 개수 조회](#상품-전체-개수-조회)
7. [매출 상위 상품 목록 조회](#매출-상위-상품-목록-조회)
8. [결론](#결론)

## 요약

<details>
  <summary>요약</summary>

> EXPLAIN 키워드를 사용해서 실행 계획을 분석하고 튜닝을 수행했습니다.
> 
> - 상품 목록을 price 로 정렬하는 쿼리에서 price 에 대한 인덱스가 없어서 이를 추가하고 성능을 개선했습니다.
> 
> - 페이지네이션을 위해 조건에 맞는 상품 전체 개수를 세는 쿼리에서는 인덱스가 성능에 영향을 주지 못 했고, SELECT 구문에서 distinct 와 불필요한 컬럼 조회를 제거하여 성능을 개선했습니다.
> 
> - 마지막으로 매출 상위 상품 목록 조회 쿼리에서는 커버링 인덱스와 파생 컬럼을 사용하여 성능을 개선했습니다.

</details>

## SQL 분석 계획

<details>
  <summary>SQL 분석 계획</summary>

```
EXPLAIN {SQL}
```

기본적인 실행계획을 확인하는 명령어입니다. 여기에서 확인할 수 있는 정보 중 앞으로 제가 주로 분석할 항목은 아래와 같습니다.

- type : 테이블의 데이터를 어떻게 찾을지를 제공하는 항목입니다. 테이블 풀 스캔을 할 수도 있고, 인덱스를 통해 즉시 찾을 수도 있습니다.
- rows : SQL 수행을 위해 접근하는 데이터의 모든 행 수를 나타내는 예측 항목입니다. 수시로 변동하는 정보를 참고하므로 수치는 정확하지 않습니다. 조회 최종 결과 건수와 rows 의 값이 차이가 많이 날 때는 튜닝 대상이 될 수 있습니다.
- filtered : SQL 문으로 가져온 데이터를 DB 엔진에서 필터 조건에 따라 어느 정보의 비율로 데이터를 제거했는지를 나타냅니다. filtered 의 비율이 너무 낮다면 불필요한 데이터를 너무 많이 가져오고 있다는 뜻이 될 수 있습니다.
- Extra : 추가 정보를 제공하는 항목입니다. Using index, Using filesort, Using temporary 등이 있습니다.

```
EXPLAIN ANALYZE {SQL}
```

쿼리를 실제로 수행하는 데 소요된 시간과 비용을 측정하고 실측 실행 계획과 예측 실행 계획을 모두 확인합니다. 여기에서 확인할 수 있는 정보 중 앞으로 제가 주로 분석할 항목은 아래와 같습니다.

- actual time : 실제로 쿼리를 수행하는 데 소요된 시간입니다. 이 값이 높다면 해당 쿼리가 성능에 영향을 미칠 수 있습니다.
- rows : 예측된 rows 와 실제 rows 가 다르다면, 예측과 다르게 동작하고 있다는 것을 의미합니다. 또 너무 많은 rows 를 조회하지는 않는지 확인할 수 있습니다.

> 참고로 아래 튜닝 과정에서는, `WHERE 절에서 기본키나 외래키만 사용하면서, 함수도 사용하지 않는 단순한 쿼리`는 실행 계획 분석을 생략하고 나머지 쿼리들만 분석했습니다.

</details>

## 주문별 주문 총액 조회

<details>
  <summary>주문별 주문 총액 조회</summary>

### 튜닝 전 SQL

```
select
    oi1_0.order_id,
    sum((oi1_0.price*oi1_0.quantity)) 
from
    order_items oi1_0 
where
    oi1_0.order_id in (1, 10, 100, 1000, 10000, 20000, 30000, 40000, 50000, 100000) 
group by
    oi1_0.order_id;
```

특정 사용자의 주문 내역을 조회할 때 주문 총액을 구하기 위해, 주문 ID 별로 주문총액을 구하는 쿼리입니다.

이때 주문 내역이 많아져서 페이징을 적용할 때, 한 번에 조회할 데이터 수는 10건이라고 가정하기에 WHERE 에서 사용되는 IN 연산에 10개의 ID 만 임의로 넣었습니다. 단, 조회할 현재 등록된 10만 건의 주문 내에서 최대한 분산시켜봤습니다.

쿼리 안에서 GROUP BY 키워드와 SUM() 함수를 사용하고 있어서 한 번 실행 계획을 분석해봤습니다.

### 튜닝 전 실행 계획

| id | select_type | table | partitions | type  | possible_keys            | key                      | key_len | ref  | rows | filtered | Extra                 |
|----|-------------|-------|------------|-------|--------------------------|--------------------------|---------|------|------|----------|-----------------------|
| 1  | SIMPLE      | oi1_0 | NULL       | range | idx_order_items_order_id | idx_order_items_order_id | 9       | NULL | 100  | 100.00   | Using index condition |

- type 은 range 입니다. 쿼리에 IN 연산이 포함되어 있기에 range 로 조회되었습니다. range 는 주어진 범위 내에서 행 단위로 스캔하지만, 스캔 범위가 넓으면 성능 저하의 요인이 될 수는 있습니다.
- filtered 는 100.00 입니다. 인덱스를 잘 타서 불필요한 레코드 없이 알맞게 가져왔습니다.
- Extra 는 Using index condition 만 있습니다. 필터 조건을 스토리지 엔진으로 전달하고 MySQL 엔진의 필터링 작업에 대한 부하를 줄임으로써 성능 효율이 높게 측정됩니다.

```
-> Group aggregate: sum((oi1_0.price * oi1_0.quantity))  (cost=63.3 rows=100) (actual time=0.117..1.47 rows=10 loops=1)
    -> Index range scan on oi1_0 using idx_order_items_order_id over (order_id = 1) OR (order_id = 10) OR (8 more), with index condition: (oi1_0.order_id in (1,10,100,1000,10000,20000,30000,40000,50000,100000))  (cost=53.3 rows=100) (actual time=0.058..1.42 rows=100 loops=1)
```

idx_order_items_order_id 인덱스를 사용하여 조건에 맞는 order_id 가 포함된 데이터만 효율적으로 스캔하고, 이렇게 가져온 데이터를 기반으로 각 주문별 총 금액을 계산합니다.

실측 실행 계획에서도 필요한 데이터(정확히 rows 100건)을 가져오고, actual time 도 1.47ms 로 빠르게 조회되었습니다.

### 튜닝 수행

실행 계획 조회 결과 주요 분석 컬럼들에서 병목이 없었기에 별도의 튜닝을 하지 않았습니다.

### 튜닝 결과

인덱스가 있는 order_id 로 필요한 데이터만 가져오고, 그 이후에 GROUP BY 와 SUM() 함수를 사용하여 주문별 총액을 계산하게 되어 효율적으로 동작했습니다.

별도의 튜닝 없이도 효율적으로 동작하는 쿼리였습니다.

</details>

## 상품 목록 페이징 조회(생성 순서로 정렬)

<details>
  <summary>상품 목록 페이징 조회(생성 순서로 정렬)</summary>

### 튜닝 전 SQL

```
select
    i1_0.id,
    i1_0.name,
    i1_0.price,
    i1_0.status 
from
    items i1_0 
where i1_0.status = 'ACTIVE' 
  and i1_0.name like '%Title%' escape '!' 
order by
    i1_0.id desc 
limit
    0, 10;
```

상품 목록을 전달하기 위해 일정 개수 만큼 상품 목록을 가져오는 쿼리입니다.

이때 검색어가 없으면 검색어 부분 쿼리는 제외되는 동적 쿼리이나, 분석 결과에서 검색어가 없는 경우만 특별히 봐야 하는 상황은 없었기에 해당 부분은 생략했습니다.

이 쿼리는 애플리케이션에서 동적으로 id 또는 price 로 정렬하는데, 이번에는 id 로 정렬하는 경우입니다.

### 튜닝 전 실행 계획

100만 건의 데이터가 있는 상황에서 id 로 정렬할 때는 수행 시간이 1ms 이하로 빠르게 조회가 되었습니다.

| id | select_type | table | partitions | type  | possible_keys | key     | key_len | ref  | rows | filtered | Extra                            |
|----|-------------|-------|------------|-------|---------------|---------|---------|------|------|----------|----------------------------------|
| 1  | SIMPLE      | i1_0  | NULL       | index | NULL          | PRIMARY | 8       | NULL | 10   | 1.11     | Using where; Backward index scan |

- type 은 index 입니다. ORDER BY 에서 id 로 정렬했기 때문에 key 에서 PRIMARY 키가 사용되면서 인덱스 풀 스캔을 했음을 알 수 있습니다.
- filtered 는 1.11 입니다. 인덱스 기반으로 검색하고 남은 결과 1.11% 외에 모두 필터링되었습니다. 스토리지 엔진에서 필터링을 하지 않고 MySQL 엔진에서 WHERE 조건으로 필터링하고 있음을 알 수 있습니다.
- Extra - Using where; WHERE 절의 필터 조건으로 MySQL 엔진으로 가져온 데이터를 추출하고 있습니다.
- Extra - Backward index scan 은 내림차순 정렬 때문에 생긴 정보입니다.

(검색어가 '10Title' 인 경우 -> 실제로 존재하는 값을 검색)
```
-> Limit: 10 row(s)  (cost=1.02 rows=0.111) (actual time=0.0917..2361 rows=10 loops=1)
    -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%10Title%' escape '!'))  (cost=1.02 rows=0.111) (actual time=0.0905..2361 rows=1loops=1)
        -> Index scan on i1_0 using PRIMARY (reverse)  (cost=1.02 rows=10) (actual time=0.0847..2160 rows=890010 loops=1)
```

(검색어가 '10itle' 인 경우 -> 존재하지 않는 값을 검색)
```
-> Limit: 10 row(s)  (cost=0.999 rows=0.111) (actual time=751..751 rows=0 loops=1)
    -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%10itle%' escape '!'))  (cost=0.999 rows=0.111) (actual time=751..751 rows=0 loops=1)
        -> Index scan on i1_0 using PRIMARY (reverse)  (cost=0.999 rows=10) (actual time=0.0537..531 rows=1e+6 loops=1)
```

- 인덱스 스캔 : 상품 테이블의 PK 를 역순으로 스캔하여 데이터를 가져옵니다.
- 필터링 : 인덱스 스캔 결과에서 status 가 'ACTIVE'이고 name 컬럼이 '%Title%' 패턴과 일치하는 행을 필터링합니다.
- 실측 결과에서는 rows 수가 890010 으로 거의 모든 데이터를 가져온 다음 필터링을 하고 있음을 알 수 있습니다.
- 그리고 actual time 은 검색어가 무엇인지에 따라 다르게 나타납니다. 실제로 존재하는 값을 검색한 경우가 2.361초로 더 오래 걸렸습니다.

### 튜닝 수행

- status 의 값은 'ACTIVE' 아니면 'HIDDEN' 입니다. 즉, 선택도는 1/2 이고 카디널리티(50만 추정)는 ID 나 name, price(100만 추정) 에 비해 매우 낮습니다. 단일 인덱스로는 의미가 없고, 복합인덱스로 카디널리티 순서를 고려할 때 앞쪽에 배치하면 좋다는 것을 알 수 있습니다.
- name 에서는 앞뒤로 와일드카드를 사용한 패턴 검색을 하기에 인덱스로 해결하기가 어렵습니다. 인덱스는 결국 어떤 컬럼(들)을 정렬한 것이므로, 검색어가 포함되었는지를 찾는 LIKE 연산을 하게 되면 이를 사용할 수 없습니다. 상품 이름 검색의 성능 향상을 위해서는 역치 인덱스를 사용한 Full Text 나 Elastic Search 사용이 필요한 상황이므로, 인덱스 튜닝 대상에서는 제외합니다.
  - 다만 검색 조건을 바꿔서 'Title%' 같은 식으로 검색하게 하면 인덱스를 사용할 수 있을 것입니다. 하지만 요구사항 자체가 변경되어야 하기에 이는 제외했습니다.

> 역치 인덱스(Inverted Index)란?
> 
> 역치 인덱스는 텍스트 검색을 위해 사용되는 인덱스로, 텍스트를 단어 단위로 쪼개어 단어가 어느 문서에 포함되어 있는지를 기록합니다.
> 
> 이후 검색어가 주어지면 해당 단어가 포함된 데이터를 찾아내는 방식으로 동작합니다. 이를 통해 텍스트 검색을 빠르게 수행할 수 있습니다.

위 사항들을 고려했을 때 키워드 검색이라는 문제를 해결하지 못 한다면 결국 모든 레코드를 다 탐색해야 하기에 인덱스를 추가하는 것은 의미가 없습니다. 따라서 최소한 검색어가 없는 경우의 탐색을 최적화하고자 계획 실행 분석을 진행해보겠습니다.

| id | select_type | table | partitions | type  | possible_keys | key     | key_len | ref  | rows | filtered | Extra                            |
|----|-------------|-------|------------|-------|---------------|---------|---------|------|------|----------|----------------------------------|
| 1  | SIMPLE      | i1_0  | NULL       | index | NULL          | PRIMARY | 8       | NULL | 10   | 10.00    | Using where; Backward index scan |

```
-> Limit: 10 row(s)  (cost=0.932 rows=1) (actual time=0.087..0.0917 rows=10 loops=1)
    -> Filter: (i1_0.`status` = 'ACTIVE')  (cost=0.932 rows=1) (actual time=0.0848..0.0889 rows=10 loops=1)
        -> Index scan on i1_0 using PRIMARY (reverse)  (cost=0.932 rows=10) (actual time=0.0754..0.0795 rows=11 loops=1)
```

튜닝 전 실행 계획에서 이미 조건에 해당하는 적은 수의 데이터만 가지고 와서 필터링을 하는 것으로 보아, 튜닝이 따로 필요해보이진 않습니다. 다만 궁금한 마음에 status 인덱스, (status, id) 복합 인덱스로 실측 실행 시간만 비교해봤습니다.

#### status 단일 인덱스 생성

```
CREATE INDEX idx_items_status ON items (status);
```

```
-> Limit: 10 row(s)  (cost=52239 rows=10) (actual time=0.358..0.363 rows=10 loops=1)
    -> Index lookup on i1_0 using idx_items_status (status='ACTIVE') (reverse)  (cost=52239 rows=498597) (actual time=0.357..0.361 rows=10 loops=1)
```

#### status, id 복합 인덱스 생성
```
CREATE INDEX idx_items_status_id ON items (status, id);
```

```
-> Limit: 10 row(s)  (cost=52239 rows=10) (actual time=0.14..0.145 rows=10 loops=1)
     -> Index lookup on i1_0 using idx_items_status_id (status='ACTIVE') (reverse)  (cost=52239 rows=498597) (actual time=0.139..0.143 rows=10 loops=1)
```

추가로 인덱스를 만들고 실행한 결과, 실측 실행 계획에서 가져오는 rows 수는 비슷하게 10개로 동일하게 나왔지만(테스트한다고 레코드 값을 변경하느라 그렇습니다), actual time 은 오히려 더 증가했음을 알 수 있습니다.

따라서 추가로 튜닝하지 않고 추가했던 인덱스들은 삭제했습니다.

```
DROP INDEX idx_items_status ON items;
DROP INDEX idx_items_status_id ON items;
```

### 튜닝 결과

키워드 검색을 하고 있기에 name 컬럼에 대한 인덱스는 활용이 불가능했습니다. 이를 위해 FULL TEXT 나 Elastic Search 같은 검색 엔진을 사용하는 것이 필요하다는 결론을 내렸습니다.

한편 키워드 검색이 없는 경우에도 불필요하게 status 와 관련된 인덱스는 굳이 필요하지 않았으며 오히려 조회 시간이 오래 걸리는 것을 확인했기에 인덱스를 추가하지 않았습니다.

</details>

## 상품 목록 페이징 조회(가격 순서로 정렬)

<details>
  <summary>상품 목록 페이징 조회(가격 순서로 정렬)</summary>

### 튜닝 전 SQL

```
select
    i1_0.id,
    i1_0.name,
    i1_0.price,
    i1_0.status 
from
    items i1_0 
where i1_0.status = 'ACTIVE' 
  and i1_0.name like '%Title%' escape '!' 
order by
    i1_0.price desc 
limit
    0, 10;
```

위 항목의 생성 순서 정렬 쿼리와 유사하지만, price 로 정렬하는 경우입니다.

price 에는 인덱스가 없다는 것 외에는 동일하므로 인덱스 유무에 따른 성능 차이만 주목하기로 했습니다.

### 튜닝 전 실행 계획

| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows   | filtered | Extra                       |
|----|-------------|-------|------------|------|---------------|------|---------|------|--------|----------|-----------------------------|
| 1  | SIMPLE      | i1_0  | NULL       | ALL  | NULL          | NULL | NULL    | NULL | 997078 | 1.11     | Using where; Using filesort |

- type 이 ALL 입니다. 풀 테이블 스캔이 발생했습니다.
- 사용된 key 는 없습니다.
- rows 는 997,078 입니다. 거의 모든 행을 조회했습니다.
- filtered 도 마찬가지로 1.11 입니다. 가져온 데이터 중 1.11% 외에 모두 필터링되었습니다.
- Extra - Using where; WHERE 절의 필터 조건으로 MySQL 엔진으로 가져온 데이터를 추출하고 있습니다.
- Extra - Using filesort 는 정렬이 필요한 데이터를 메모리에 올리고 정렬 작업을 수행했음을 의미합니다. 이는 추가적인 정렬 작업이기에 인덱스를 활용할 여지가 있습니다.

```
-> Limit: 10 row(s)  (cost=100517 rows=10) (actual time=705..705 rows=10 loops=1)
    -> Sort: i1_0.price DESC, limit input to 10 row(s) per chunk  (cost=100517 rows=997078) (actual time=705..705 rows=10 loops=1)
        -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%Title%' escape '!'))  (cost=100517 rows=997078) (actual time=0.594..587 rows=1e+6 loops=1)
            -> Table scan on i1_0  (cost=100517 rows=997078) (actual time=0.558..359 rows=1e+6 loops=1)
```

- Table scan 이 발생하고 있어서 비효율적인 동작이 발생하고 있습니다.
- rows 의 수가 너무 많습니다. Table scan, Filter 에서 rows 100만 건을 다루고 있습니다.
- 총 수행 시간은 705ms 로, 이전에 id 로 정렬할 때보다 훨씬 느리게 동작하고 있습니다.

### 튜닝 수행

- id 와 price 인덱스 유무에 따라 DB 엔진의 동작이 크게 달라졌습니다. 그 외에는 조건이 같으므로 price 인덱스만 추가했습니다.

```
CREATE INDEX idx_items_price ON items (price);
```

| id | select_type | table | partitions | type  | possible_keys | key             | key_len | ref  | rows | filtered | Extra                            |
|----|-------------|-------|------------|-------|---------------|-----------------|---------|------|------|----------|----------------------------------|
| 1  | SIMPLE      | i1_0  | NULL       | index | NULL          | idx_items_price | 4       | NULL | 10   | 1.11     | Using where; Backward index scan |

```
-> Limit: 10 row(s)  (cost=0.908 rows=1) (actual time=0.0942..0.098 rows=10 loops=1)
    -> Filter: (i1_0.`status` = 'ACTIVE')  (cost=0.908 rows=1) (actual time=0.093..0.0961 rows=10 loops=1)
        -> Index scan on i1_0 using idx_items_price (reverse)  (cost=0.908 rows=10) (actual time=0.0895..0.0913 rows=10 loops=1)
```

- Table scan 에서 Index scan 으로 변경되었고, 실행 시간도 0.098ms 로 훨씬 빨라졌습니다.
- 조회 프로세스는 생성 순서로 정렬할 때와 동일하게 동작하고 있습니다.

### 튜닝 결과

```
CREATE INDEX idx_items_price ON items (price);
```

price 단일 인덱스를 생성함으로써 price 로 정렬하는 쿼리가 id 로 정렬할 때와 동일한 수준의 성능을 가지게 되었습니다.

</details>

## 상품 전체 개수 조회

<details>
  <summary>상품 전체 개수 조회</summary>

### 튜닝 전 SQL

```
select
    count(distinct i1_0.id) 
from
    items i1_0 
where i1_0.status = 'ACTIVE'
  and i1_0.name like '%Title%' escape '!';
```

페이지네이션을 할 때 사용되는 카운트 쿼리입니다.

실행 계획을 분석하기도 전에 distinct 라는 불필요한 필터링 과정이 포함된 게 보였습니다. distinct 를 하면 중복을 제거하는 과정이 추가되기에 불필요하다면 성능 저하의 요인이 될 수 있습니다.

실제 분석 결과에서도 count(distinct id) 가 평균 900ms 가 소요된 반면, count(id) 는 평균 500ms 가 소요되었습니다.

(count(distinct id))
```
-> Aggregate: count(distinct i1_0.id)  (cost=101621 rows=1) (actual time=919..919 rows=1 loops=1)
    -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%Title%' escape '!'))  (cost=100513 rows=11079) (actual time=0.0782..601 rows=999998 loops=1)
        -> Table scan on i1_0  (cost=100513 rows=997194) (actual time=0.0709..332 rows=1e+6 loops=1)
```

(count(id))
```
-> Aggregate: count(i1_0.id)  (cost=101621 rows=1) (actual time=553..553 rows=1 loops=1)
    -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%Title%' escape '!'))  (cost=100513 rows=11079) (actual time=0.0737..499 rows=999998 loops=1)
        -> Table scan on i1_0  (cost=100513 rows=997194) (actual time=0.0675..272 rows=1e+6 loops=1)
```

추가로, count(id) 대신 count(*) 을 사용하면 SQL 엔진이 최적화하여 레코드 수를 계산하고, NULL 인지 아닌지 확인하지 않고 레코드를 포함하거나 메타 정보를 활용하는 등의 이점이 있기 때문에 *(애스터리스크)를 사용했습니다.

```
select
    count(*) 
from
    items i1_0 
where i1_0.status = 'ACTIVE'
  and i1_0.name like '%Title%' escape '!';
```

위와 같이 쿼리를 먼저 변경한 후 마저 실행 계획을 분석했습니다.

### 튜닝 전 실행 계획

| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows   | filtered | Extra       |
|----|-------------|-------|------------|------|---------------|------|---------|------|--------|----------|-------------|
| 1  | SIMPLE      | i1_0  | NULL       | ALL  | NULL          | NULL | NULL    | NULL | 997194 | 1.11     | Using where |

사실 count 함수는 해당하는 모든 레코드를 세야 하기 때문에 비용이 클 수 밖에 없습니다.

count 를 할 때, type 은 ALL 로 고정이고 변하지는 않을 것입니다.

```
-> Aggregate: count(0)  (cost=101621 rows=1) (actual time=536..536 rows=1 loops=1)
    -> Filter: ((i1_0.`status` = 'ACTIVE') and (i1_0.`name` like '%Title%' escape '!'))  (cost=100513 rows=11079) (actual time=0.112..480 rows=999998 loops=1)
        -> Table scan on i1_0  (cost=100513 rows=997194) (actual time=0.107..245 rows=1e+6 loops=1)
```

프로세스가 수행될 때마다 rows 수가 기하급수적으로 줄어드는 게 눈에 띕니다. 처음에는 풀 테이블 스캔으로 모든 레코드를 조회하고 그 이후 Filter 와 Aggregate 로 연산하면서 필요한 정보를 가져옵니다.

### 튜닝 수행

- 지금 여기에서 사용되고 있는 컬럼은 status 와 name 2가지입니다.
  - 페이지네이션에서는 status 관련 컬럼의 추가가 오히려 손해였는데, 카운트 쿼리에서는 또 어떤지 알아보겠습니다.

#### status 단일 인덱스 생성

```
CREATE INDEX idx_items_status ON items (status);
```

| id | select_type | table | partitions | type | possible_keys    | key              | key_len | ref   | rows   | filtered | Extra       |
|----|-------------|-------|------------|------|------------------|------------------|---------|-------|--------|----------|-------------|
| 1  | SIMPLE      | i1_0  | NULL       | ref  | idx_items_status | idx_items_status | 126     | const | 498597 | 11.11    | Using where |

```
-> Aggregate: count(0)  (cost=13459 rows=1) (actual time=2289..2289 rows=1 loops=1)
    -> Filter: (i1_0.`name` like '%Title%' escape '!')  (cost=7919 rows=55394) (actual time=0.227..2236 rows=999998 loops=1)
        -> Index lookup on i1_0 using idx_items_status (status='ACTIVE')  (cost=7919 rows=498597) (actual time=0.223..2082 rows=999998 loops=1)
```

idx_items_status 인덱스를 사용하게 되면서 성능이 급격하게 하락했습니다.

3번째 줄의 Index lookup 을 보면 상품 목록에서 복합 인덱스를 만들었을 때와 유사하게, 가져올 때부터 인덱스 조건에 해당하는 컬럼들을 찾아오는 과정 자체에서 시간이 오래 소요되었음을 확인할 수 있습니다.

#### status, name 복합 인덱스 생성

```
CREATE INDEX idx_items_status_name ON items (status, name);
```

| id | select_type | table | partitions | type | possible_keys         | key                   | key_len | ref   | rows   | filtered | Extra                    |
|----|-------------|-------|------------|------|-----------------------|-----------------------|---------|-------|--------|----------|--------------------------|
| 1  | SIMPLE      | i1_0  | NULL       | ref  | idx_items_status_name | idx_items_status_name | 126     | const | 498597 | 11.11    | Using where; Using index |

```
-> Aggregate: count(0)  (cost=73379 rows=1) (actual time=548..548 rows=1 loops=1)
    -> Filter: (i1_0.`name` like '%Title%' escape '!')  (cost=67840 rows=55394) (actual time=0.0758..493 rows=999998 loops=1)
        -> Covering index lookup on i1_0 using idx_items_status_name (status='ACTIVE')  (cost=67840 rows=498597) (actual time=0.0718..333 rows=999998 loops=1)
```

커버링 인덱스를 사용하는 등의 변경사항은 생겼지만, 기존 쿼리와 유의미한 시간 차이가 없습니다. 따라서 필요하지 않다면 불필요한 인덱스를 만들 필요는 없습니다.

### 튜닝 결과

```
count(*)
```

인덱스 사용은 큰 의미가 없었으며, 쿼리에서 distinct 를 제거함으로써 성능을 향상시킬 수 있었습니다.

한편, 이러한 count 쿼리 자체가 계속해서 성능에 영향을 주거나 부담이 된다면, 다음 페이지 셋까지만 알려주도록 웹 사이트를 디자인하거나 슬라이싱을 하도록 유스케이스를 조정할 수도 있고, 적절한 만료 시간을 지닌 캐시를 사용할 수도 있습니다.

</details>

## 매출 상위 상품 목록 조회

<details>
  <summary>매출 상위 상품 목록 조회</summary>

### 튜닝 전 SQL

```
select
    i1_0.id,
    i1_0.name,
    i1_0.price,
    i1_0.status 
from
    items i1_0 
left join
    order_items oi1_0 
        on i1_0.id=oi1_0.item_id 
left join
    orders o1_0 
        on oi1_0.order_id=o1_0.id 
where i1_0.status = 'ACTIVE'
    and o1_0.status='ORDERED' 
    and o1_0.order_date_time between '2024-11-09T00:00:00' and '2024-11-12T00:00:00' 
group by
    i1_0.id 
order by
    sum((oi1_0.quantity*oi1_0.price)) desc 
limit
    5;
```

상품 랭킹 조회 쿼리입니다. 조금 케이스를 어렵게 해보려고 '매출'을 기준으로 잡아봤습니다. 따라서 ORDER BY 절에서 SUM() 함수와 곱셈 연산이 사용되고 있습니다.

쿼리 실행계획을 분석하기 전에 하나 개선해야 할 점이 하나 보이는데, 주문과 상품 테이블이 조인되고 있다는 점입니다. 만약 분산 DB 환경에서 주문과 상품이 다른 도메인으로 취급되고 다른 DB 에서 각각 다루고 있다면, 주문 도메인 DB 에서 상품 ID 만 가지고 오고, 이후 이 상품 ID 를 가지고 상품 도메인 DB 에서 상품 정보를 가지고 와야 할 수 있습니다.

필요에 의해서 items 테이블을 조인에서 제거했지만, 이렇게 하면 조인하는 테이블 수도 줄어들어 튜닝하기 좀 더 쉬워진다는 부가적인 장점도 생깁니다.

한편, 이렇게 했을 때 WHERE 절에서 items.status 조건을 걸 수 없다는 문제점이 있습니다.

> 멘토링 전에 먼저 구상한 건 애플리케이션이 해당 조건을 만족하는 상품을 넉넉하게 (10건 정도) 가져오고, 만족하는 개수(5건)가 될 때까지 추가로 더 쿼리를 날리는 식이었습니다.
> 
> 그런데 항해플러스 멘토링을 진행하면서 좀 맘에 드는 아이디어를 좀 얻은 게 있는데, 추가로 더 가져오지 않고 그냥 그대로 응답해버리는 겁니다.
> 
> 이렇게 하면 프론트엔드에서 적절히 유스케이스에 맞게 데이터를 직접 가공할 수도 있고, 만약 부족하다면 추가로 더 상품을 가져온다는 식으로 보완할 수 있습니다.
> 
> 슬라이싱을 할 때도 비록 응답해야 할 개수가 부족하더라도 조회한 만큼 바로 응답하더라도 다음 슬라이스를 가져오면서 부족했던 부분을 채울 수 있기 때문에 사용자에게 티만 좀 덜 나게 하면 됩니다.
> 
> 개인적으로 이 방법은 과제에서뿐만 아니라 실무에서도 좀 유용하게 써먹을 수 있겠다는 생각이 듭니다(감사합니다 코치님ㅎㅎ).

```
SELECT
    oi.item_id
FROM
    order_items oi
INNER JOIN
    orders o
	    ON o.id = oi.order_id
WHERE o.status = 'ORDERED'
  AND o.order_date_time BETWEEN '2024-11-09T00:00:00' AND '2024-11-12T00:00:00'
GROUP BY
    oi.item_id
ORDER BY
    sum(oi.quantity*oi.price) DESC 
LIMIT
    10;
```

변경된 쿼리에서는 items 테이블을 조인하지 않고, 매출 기준 내림차순으로 정렬하여 상품 ID 를 반환하고 있습니다.

## 실행 계획 분석

| id | select_type | table | partitions | type | possible_keys                                    | key                      | key_len | ref               | rows  | filtered | Extra                                        |
|----|-------------|-------|------------|------|--------------------------------------------------|--------------------------|---------|-------------------|-------|----------|----------------------------------------------|
| 1  | SIMPLE      | o     | NULL       | ALL  | PRIMARY                                          | NULL                     | NULL    | NULL              | 99913 | 2.78     | Using where; Using temporary; Using filesort |
| 1  | SIMPLE      | oi    | NULL       | ref  | idx_order_items_order_id,idx_order_items_item_id | idx_order_items_order_id | 8       | ecommerce.o1_0.id | 9     | 100.00   | NULL                                         |

- 먼저 orders 테이블에서는 풀 테이블 스캔을 하면서 인덱스는 사용하지 않고 있고 rows 는 99,960 건으로 거의 모든 레코드를 조회하고 있음을 볼 수 있습니다. Extra 에서는 where, temporary, filesort 등 여러 추가 동작들을 조회할 수 있습니다.
  - Using where : WHERE 절의 필터 조건을 사용해서 MySQL 엔진으로 가져온 데이터를 추출할 것입니다.
  - Using temporary : 데이터 중간 결과를 저장하고자 임시 테이블이 생성됩니다. 메모리 영역을 초과하여 디스크에 임시 테이블을 생성하면 성능 저하의 원인이 될 수 있습니다.
  - Using filesort : 정렬이 필요한 데이터를 메모리에 올리고 정렬 작업이 수행됩니다. 추가적인 정렬 작업은 인덱스를 활용하도록 SQL 튜닝 검토 대상이 될 수 있습니다.
- 이후 order_items 테이블에서 ref 타입으로 order_id 컬럼의 인덱스를 사용하면서 다수의 주문 상품을 가져오고 있습니다. 좀 더 구체적으로 이해하기 위해 실측 실행 계획을 출력해봤습니다.

```
-> Limit: 10 row(s)  (actual time=4226..4226 rows=10 loops=1)
    -> Sort: sum((oi.price * oi.quantity)) DESC, limit input to 10 row(s) per chunk  (actual time=4226..4226 rows=10 loops=1)
        -> Table scan on <temporary>  (actual time=4038..4150 rows=500000 loops=1)
            -> Aggregate using temporary table  (actual time=4038..4038 rows=499999 loops=1)
                -> Nested loop inner join  (cost=19182 rows=11127) (actual time=47.3..2579 rows=500000 loops=1)
                    -> Filter: ((o.`status` = 'ORDERED') and (o.order_date_time between '2024-11-10T00:00:00' and '2024-11-13T00:00:00'))  (cost=10228 rows=1111) (actual time=46.5..148 rows=50000 loops=1)
                        -> Table scan on o  (cost=10228 rows=99960) (actual time=0.0752..89.1 rows=100000 loops=1)
                    -> Index lookup on oi using idx_order_items_order_id (order_id=o.id)  (cost=7.06 rows=10) (actual time=0.0458..0.0477 rows=10 loops=50000)
```

주문 10만, 주문 상품 100만 조회하고, 필터링된 50만 개의 레코드로 구성된 임시 테이블을 만들었습니다. 임시 테이블에서 Aggregate 연산, 정렬 및 LIMIT 을 하는 프로세스입니다.

- Nested loop inner join : orders 테이블과 order_items 테이블을 조인하면서 2.5초 가량 소요되었습니다.
- Aggregate using temporary table : 임시 테이블을 만들고 Aggregate 연산을 수행했습니다. 조인 결과를 임시 테이블로 생성하면서 1.5 초 가량(4038 - 2579) 소요된 것으로 파악됩니다.

총 실행 시간은 약 4.2초이고, 이중 가장 큰 병목을 차지한 것은 조인과 임시 테이블 생성이었습니다.

### 튜닝 수행

이번 튜닝은 아래 3단계로 진행됩니다.

1. orders 에서 커버링 인덱스를 활용
2. total_amount 파생 컬럼 추가
3. order_items 에서 커버링 인덱스를 활용

#### orders 에서 커버링 인덱스를 활용

```
CREATE INDEX idx_orders_composite ON orders (status, order_date_time);
```

| id | select_type | table | partitions | type  | possible_keys                                    | key                      | key_len | ref            | rows  | filtered | Extra                                                     |
|----|-------------|-------|------------|-------|--------------------------------------------------|--------------------------|---------|----------------|-------|----------|-----------------------------------------------------------|
| 1  | SIMPLE      | o     | NULL       | range | PRIMARY,idx_orders_composite                     | idx_orders_composite     | 11      | NULL           | 49956 | 100.00   | Using where; Using index; Using temporary; Using filesort |
| 1  | SIMPLE      | oi    | NULL       | ref   | idx_order_items_order_id,idx_order_items_item_id | idx_order_items_order_id | 8       | ecommerce.o.id | 9     | 100.00   | NULL                                                      |

```
-> Limit: 10 row(s)  (actual time=3839..3839 rows=10 loops=1)
    -> Sort: sum((oi.price * oi.quantity)) DESC, limit input to 10 row(s) per chunk  (actual time=3839..3839 rows=10 loops=1)
        -> Table scan on <temporary>  (actual time=3629..3755 rows=500000 loops=1)
            -> Aggregate using temporary table  (actual time=3629..3629 rows=499999 loops=1)
                -> Nested loop inner join  (cost=349615 rows=500771) (actual time=0.445..2170 rows=500000 loops=1)
                    -> Filter: ((o.`status` = 'ORDERED') and (o.order_date_time between '2024-11-10T00:00:00' and '2024-11-13T00:00:00'))  (cost=11609 rows=49980) (actual time=0.0292..101 rows=50000 loops=1)
                        -> Covering index range scan on o using idx_orders_composite over (status = 'ORDERED' AND '2024-11-10 00:00:00.000000' <= order_date_time <= '2024-11-13 00:00:00.000000')  (cost=11609 rows=49980) (actual time=0.0242..60.3 rows=50000 loops=1)
                    -> Index lookup on oi using idx_order_items_order_id (order_id=o.id)  (cost=5.76 rows=10) (actual time=0.0385..0.0405 rows=10 loops=50000)
```

7번째 depth 에서 idx_orders_composite 인덱스를 커버링 인덱스로 사용하게 되면서 기존에 100만 개를 가져오던 게 50만 개로 줄어들면서 모수를 줄일 수 있었습니다.

시간 상의 차이는 100만 개의 레코드 내에서는 두드러지게 나오지는 않았지만 rows 를 줄이는 것으로 성능 개선을 확인할 수 있었습니다.

#### total_amount 파생 컬럼 추가

이 쿼리에서 구하고자 하는 정보는 price 와 quantity 가 아닙니다. 정확하게는 그들을 곱한 주문 상품 금액입니다. 

따라서 이들을 미리 계산해 둔 파생컬럼을 둔다면 다음과 같은 장점이 있습니다.

1. 정렬하는 과정에서 수행하는 price * quantity 연산을 줄일 수 있습니다.
2. 다음 절과 이어지는 내용입니다만, 복합 인덱스를 사용할 때 대상을 price, quantity 2 개의 컬럼에서 total_amount 1개의 컬럼으로 줄일 수 있습니다. 이는 조회할 때만이 아니라 CUD 에서 인덱스를 추가하는 과정에서도 성능을 개선할 수 있음을 의미합니다.
  - 단, 파생컬럼의 정합성을 지키기 위해 price, quantity 가 변경될 때마다 total_amount 같이 수정하는 걸 잊지 말아야 합니다.

```
ALTER TABLE order_items ADD COLUMN total_amount INT;
SET SQL_SAFE_UPDATES = 0;
UPDATE order_items
SET total_amount = price * quantity;
SET SQL_SAFE_UPDATES = 1;
```

```
SELECT oi.item_id
FROM order_items oi
INNER JOIN orders o
	ON o.id = oi.order_id
WHERE o.status = 'ORDERED'
  AND o.order_date_time BETWEEN '2024-11-09T00:00:00' AND '2024-11-10T00:00:00'
GROUP BY oi.item_id
ORDER BY sum(oi.total_amount) DESC 
LIMIT 10;
```

```
-> Limit: 10 row(s)  (actual time=3886..3886 rows=10 loops=1)
    -> Sort: sum(oi.total_amount) DESC, limit input to 10 row(s) per chunk  (actual time=3886..3886 rows=10 loops=1)
        -> Table scan on <temporary>  (actual time=3673..3801 rows=500000 loops=1)
            -> Aggregate using temporary table  (actual time=3673..3673 rows=499999 loops=1)
                -> Nested loop inner join  (cost=374541 rows=500771) (actual time=0.0909..2240 rows=500000 loops=1)
                    -> Filter: ((o.`status` = 'ORDERED') and (o.order_date_time between '2024-11-10T00:00:00' and '2024-11-13T00:00:00'))  (cost=11609 rows=49980) (actual time=0.0321..104 rows=50000 loops=1)
                        -> Covering index range scan on o using idx_orders_composite over (status = 'ORDERED' AND '2024-11-10 00:00:00.000000' <= order_date_time <= '2024-11-13 00:00:00.000000')  (cost=11609 rows=49980) (actual time=0.0266..61 rows=50000 loops=1)
                    -> Index lookup on oi using idx_order_items_order_id (order_id=o.id)  (cost=6.26 rows=10) (actual time=0.0399..0.0418 rows=10 loops=50000)
```

실측 실행 계획에서 100만개의 레코드에 대해서 성능개선이 나타나진 않았습니다.

다만 위에서 언급한 대로 곱셈 연산 과정이 사라지고, 파생 컬럼을 사용하고 있음을 확인할 수 있습니다.

또한 total_amount 는 복합 인덱스로 사용할 것이기 때문에 CUD 작업에서도 성능 개선을 기대할 수 있습니다.

#### order_items 에서 커버링 인덱스를 활용

마지막으로 4가지 컬럼에 대한 복합 인덱스를 활용했습니다.

order_items 테이블과 orders 테이블은 order_id 를 기준으로 조인하게 되고, 그 이후 item_id 에 대하여 total_amount 에 대한 집계 함수를 사용합니다. 따라서 이러한 프로세스 흐름과 일치하는 인덱스를 생성하면 성능 개선이 있을 것이라고 기대했습니다.

```
CREATE INDEX idx_order_items_composite ON order_items (order_id, item_id, total_amount);
```

| id | select_type | table | partitions | type  | possible_keys                                                              | key                       | key_len | ref            | rows  | filtered | Extra                                                     |
|----|-------------|-------|------------|-------|----------------------------------------------------------------------------|---------------------------|---------|----------------|-------|----------|-----------------------------------------------------------|
| 1  | SIMPLE      | o     | NULL       | range | PRIMARY,idx_orders_composite                                               | idx_orders_composite      | 264     | NULL           | 49980 | 100.00   | Using where; Using index; Using temporary; Using filesort |
| 1  | SIMPLE      | oi    | NULL       | ref   | idx_order_items_order_id,idx_order_items_item_id,idx_order_items_composite | idx_order_items_composite | 8       | ecommerce.o.id | 10    | 100.00   | Using index                                               |

```
-> Limit: 10 row(s)  (actual time=1773..1773 rows=10 loops=1)
    -> Sort: sum(oi.total_amount) DESC, limit input to 10 row(s) per chunk  (actual time=1773..1773 rows=10 loops=1)
        -> Table scan on <temporary>  (actual time=1590..1701 rows=500000 loops=1)
            -> Aggregate using temporary table  (actual time=1590..1590 rows=499999 loops=1)
                -> Nested loop inner join  (cost=113414 rows=502789) (actual time=0.068..358 rows=500000 loops=1)
                    -> Filter: ((o.`status` = 'ORDERED') and (o.order_date_time between '2024-11-10T00:00:00' and '2024-11-13T00:00:00'))  (cost=11609 rows=49980) (actual time=0.0374..58.5 rows=50000 loops=1)
                        -> Covering index range scan on o using idx_orders_composite over (status = 'ORDERED' AND '2024-11-10 00:00:00.000000' <= order_date_time <= '2024-11-13 00:00:00.000000')  (cost=11609 rows=49980) (actual time=0.0314..27.2 rows=50000 loops=1)
                    -> Covering index lookup on oi using idx_order_items_composite (order_id=o.id)  (cost=1.03 rows=10.1) (actual time=0.00352..0.00519 rows=10 loops=50000)
```

사용된 인덱스가 idx_order_items_order_id 에서 idx_order_items_composite 로 변경되고 커버링 인덱스로 활용되었습니다. 이 쿼리에서는 order_items 테이블의 name 이 필요 없고 오직 idx_order_items_composite 인덱스가 가지고 있는 컬럼만으로도 작업을 완료할 수 있기 때문입니다.

그러면서 조인 시간이 기존의 2.5초에서 0.35초로 줄어들었습니다. 그리고 약간이긴 하지만 임시 테이블 생성 시간도 1.5초에서 1.2초로 줄어들었습니다.

### 튜닝 결과

매출 상위 상품 목록 조회를 위해 사용하던 쿼리에서 items 에 대한 조인을 제거하고, 이 쿼리에 필요한 컬럼들로 구성된 복합 인덱스를 order_items 테이블에 만들어 이를 커버링 인덱스로 활용하면서 조회 시간을 4.2초에서 1.7초로 줄일 수 있었습니다.

만약 이러한 조회 유스케이스가 중요하거나 더욱 성능을 개선시켜야 한다면 통계 테이블, 캐시 등의 방법도 추가로 고려해볼 수 있겠습니다.

특히, 이 매출 상위 상품 조회 쿼리는 1일 단위로 같은 결과를 반환하기 때문에 캐싱하고 재사용하면 성능 향상을 기대할 수 있습니다. 추가로 적절한 Warming 과 이벤트 기반의 Eviction 을 활용하면 부족한 정합성도 보완할 수 있습니다.

> 참고로 서브쿼리를 사용해서 Materialize 하는 방법도 수행해봤으나 조인하고 임시 테이블을 작업은 여전했고, 그 임시 테이블로부터 Materialize 한 후 데이터를 조회했습니다. 즉, 프로세스가 늘어나기만 했을 뿐 성능 개선은 없었기에 해당 내용은 생략했습니다.

</details>

## 결론

<details>
  <summary>결론</summary>

이번 기회를 통해 애매하게 알던 인덱스에 대한 개념을 좀 더 제대로 잡고, 실습을 진행했습니다. 그리고 조금 알게 됐기 때문에 더 알아야 할 게 많다는 것도 깨달았습니다.

이번 분석 과정에서는 EXPLAIN 키워드를 사용하면서 실제로 DB 엔진이 어떻게 동작하고 어떤 프로세스로 데이터를 가져오는지 알 수 있었습니다.

그리고 때로는 쿼리를 수정하고, 때로는 인덱스를 추가하면서 쿼리 성능을 개선시켜봤습니다.

- 상품 목록을 price 로 정렬하는 쿼리에서 price 에 대한 인덱스가 없어서 이를 추가하고 성능을 개선했습니다.
- 상품 전체 개수를 세는 쿼리에서는 인덱스가 성능에 영향을 주지 못 했고, SELECT 구문에서 distinct 와 불필요한 컬럼 조회를 제거하여 성능을 개선했습니다.
- 마지막으로 매출 상위 상품 목록 조회 쿼리에서는 커버링 인덱스와 파생컬럼을 사용하여 성능을 개선했습니다.

추가로 성능 향상을 위해 다음과 같은 사항들도 고려해볼 수 있습니다.

- 검색어 탐색을 위해 역치 인덱스를 활용한 Full Text 나 Elastic Search 등의 검색 엔진 사용
- count 를 지양하는 유스케이스 및 디자인, 또는 캐시 사용
- 매출 상위 상품 목록 조회 쿼리 결과를 캐싱

인덱스는 데이터베이스 성능 튜닝에서 가장 중요한 요소 중 하나이며, 쿼리 성능을 향상시킬 수 있으나 과도한 인덱스는 CUD 작업의 성능 저하를 초래할 수 있습니다.

따라서 도움이 되는 적절한 인덱스를 만들고 테스트하면서 최적의 인덱스로 적절히 튜닝해야 합니다.

</details>
