# 프로젝트 문서

## 목차
- [기술스택])(#기술스택)
- [마일스톤](#마일스톤)
- [시퀀스 다이어그램](#시퀀스-다이어그램)
- [플로우 차트](#플로우-차트)

---

## 기술스택
<details>
  <summary>기술스택</summary>

## 기술 스택

### Web Application Server
- **Java 17**
- **Spring Boot**
    - Spring Web
    - Spring Validation
    - Spring Security
    - Jwt
    - Spring Data JPA
    - Query DSL

### Messaging Solution
- **Spring for Apache Kafka**

### Database
- **H2** (Domain)
- **Prometheus** (Application Meta Data)
- **Redis** (Caching)

### Monitoring System
- **Spring Actuator**
- **Grafana**

### Documentation
- **Spring Rest Docs**
- **Swagger**

### Test
- **Spring Boot Test**

</details>

## 마일스톤
<details>
  <summary>마일스톤</summary>

## E Commerce 시나리오 기반 서버 구축 마일스톤

### Week 3 (24H)
- 마일스톤 작성 (2H)
- 시나리오 분석 (2H)
- 시퀀스 다이어그램 작성 (4H)
- 플로우 차트 작성 (3H)
- 도메인 모델링 (4H)
- ERD 작성 (1H)
- Mock API 작성 (4H)
- API Spec 작성 (2H)
- 패키지 구조 작성 (1H)
- 기술스택 작성 (1H)

### Week 4 (22H)
- 도메인 엔티티 생성 (4H)
- API 통합테스트 작성 및 Service, DTO 객체 생성 (4H)
- 생성된 DTO 의 Validation 단위테스트 작성 (2H)
- Service 단위테스트 작성, Repository 객체 생성 (4H)
- Repository Interface 생성 (1H)
- RepositoryImpl 과 JpaRepository 기능 구현 (2H)
- JpaRepository 통합 테스트 생성 및 통과 (2H)
- Service 단위 테스트 통과 및 통합 테스트 작성 후 통과 (2H)
- API 통합테스트 통과 및 테스트 전체 점검 (1H)

### Week 5 (18H)
- 동시성 제어가 필요한 케이스 선별 (2H)
- 동시성 제어를 검증할 수 있는 통합테스트 작성 (4H)
- 동시성 제어를 위한 락 구현 (4H)
- 데이터 플랫폼 전송을 위한 Interface Component 생성 (2H)
- 데이터 플랫폼 전송을 위한 Component Mocking 후 테스트 통과 (4H)
- 동시성 통합테스트 통과 보고서 작성 (2H)

</details>

## 시퀀스-다이어그램
<details>
  <summary>시퀀스 다이어그램</summary>

### Point
![1 point](https://github.com/user-attachments/assets/cec67efc-14bd-47d8-897d-3dfbe487c069)
### Item
![2 item](https://github.com/user-attachments/assets/1fbb992f-9c23-473a-9630-3d46f72e3483)
### Order
![3 order](https://github.com/user-attachments/assets/09ab398b-9270-4b25-80d8-732c8b6e945c)

</details>

## 플로우-차트
<details>
  <summary>플로우 차트</summary>

### Point
<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/005e8f70-72c8-4f0f-b13e-0f4e962b7a1b" alt="1 point" width="300"/>
</div>

### Item
<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/6091746d-2bca-4dcd-aec3-81c1da182cf4" alt="2 item" width="300"/>
</div>

### Order
<div style="text-align: center;">
    <img src="https://github.com/user-attachments/assets/ca0f0f49-c228-4c77-b6de-174c82d33d9f" alt="3 order" width="300"/>
</div>

</details>

