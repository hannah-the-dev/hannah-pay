# HannahPay-design.md

# HannahPay - 간편결제 및 송금 플랫폼 설계서

---

# 1. 프로젝트 개요

## 1.1 목적

HannahPay는 사용자 간 송금 및 가맹점 결제를 지원하는 간편결제 플랫폼이다.

본 프로젝트는 다음 목표를 가진다.

- 금융 트랜잭션 정합성 보장
- 이벤트 기반 비동기 아키텍처 구현
- 대용량 처리 가능한 구조 설계
- MSA 확장 가능한 구조 확보
- Docker/AWS 기반 운영 환경 구성
- Kafka/Redis 실무 활용 경험 확보

---

# 2. 주요 기능

## 2.1 회원 기능
- 회원가입
- 로그인
- 사용자 정보 조회

## 2.2 Wallet 기능
- 잔액 조회
- 충전
- 거래 내역 조회

## 2.3 결제 기능
- 가맹점 결제
- 결제 취소
- 결제 상태 조회

## 2.4 송금 기능
- 사용자 간 송금
- 송금 이력 조회

## 2.5 알림 기능
- 결제 완료 알림
- 송금 완료 알림
- 실패 알림

---

# 3. 시스템 아키텍처

## 3.1 전체 구조

```text
[ Client ]
      ↓
[ API Gateway ]
      ↓
[ Payment Service ]
      ↓
[ Kafka ]
 ┌──────────────┬──────────────┬──────────────┐
 ↓              ↓              ↓
[ Wallet ]   [ Ledger ]   [ Notification ]
 Service       Service         Service
      ↓
   [ Redis ]
      ↓
[ PostgreSQL ]
```

---

# 4. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot |
| ORM | JPA / Hibernate |
| Message Queue | Kafka |
| Cache | Redis |
| Database | PostgreSQL |
| Infra | Docker / Docker Compose |
| Cloud | AWS EC2 / RDS / ElastiCache |
| Build | Gradle |
| API | REST API |
| Authentication | JWT |

---

# 5. 서비스 구성

# 5.1 Payment Service

## 역할
- 결제 요청 처리
- 송금 요청 처리
- 거래 상태 관리
- Kafka 이벤트 발행

## 주요 API

| Method | URL | 설명 |
|---|---|---|
| POST | /payments | 결제 요청 |
| POST | /payments/cancel | 결제 취소 |
| POST | /transfers | 송금 |
| GET | /payments/{id} | 거래 조회 |

## 결제 처리 흐름

```text
1. 결제 요청 수신
2. Redis 중복 요청 확인
3. Wallet 잔액 검증
4. Redis Lock 획득
5. DB 트랜잭션 시작
6. 잔액 차감
7. 거래 저장
8. Ledger 저장
9. Kafka 이벤트 발행
10. 트랜잭션 커밋
11. 응답 반환
```

---

# 5.2 Wallet Service

## 역할
- 사용자 잔액 관리
- 충전 처리
- 잔액 검증

## 주요 API

| Method | URL | 설명 |
|---|---|---|
| GET | /wallets/{userId} | 잔액 조회 |
| POST | /wallets/charge | 충전 |
| POST | /wallets/deduct | 잔액 차감 |

## Wallet Entity 예시

```java
@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long balance;

    @Version
    private Long version;

    public void deduct(Long amount) {
        if (balance < amount) {
            throw new IllegalStateException("잔액 부족");
        }

        this.balance -= amount;
    }

    public void charge(Long amount) {
        this.balance += amount;
    }
}
```

---

# 5.3 Ledger Service

## 역할
- 거래 원장 저장
- 거래 이력 관리
- 감사 추적(Audit)

## 원장 관리 목적

단순 balance 컬럼만 사용하지 않고,
모든 거래 이력을 저장하여 금융 정합성을 보장한다.

## Ledger 예시 데이터

| transaction_id | type | amount | balance |
|---|---|---|---|
| TX1001 | CHARGE | +50000 | 50000 |
| TX1002 | PAYMENT | -12000 | 38000 |
| TX1003 | TRANSFER | -5000 | 33000 |

---

# 5.4 Notification Service

## 역할
- 알림 발송
- Kafka Consumer 처리

## Consumer 흐름

```text
Kafka Topic 구독
→ 이벤트 수신
→ 알림 생성
→ Push/SMS/Email 발송
```

---

# 6. 데이터베이스 설계

# 6.1 users

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# 6.2 wallets

```sql
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);
```

---

# 6.3 transactions

```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(30),
    sender_user_id BIGINT,
    receiver_user_id BIGINT,
    amount BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# 6.4 ledger

```sql
CREATE TABLE ledger (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT,
    user_id BIGINT,
    amount BIGINT,
    balance BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# 7. Kafka 설계

# 7.1 Topic 구성

| Topic | 설명 |
|---|---|
| payment.requested | 결제 요청 |
| payment.completed | 결제 완료 |
| payment.failed | 결제 실패 |
| transfer.completed | 송금 완료 |
| notification.send | 알림 발송 |

---

# 7.2 이벤트 예시

## payment.completed

```json
{
  "transactionId": "TX10001",
  "userId": 1001,
  "amount": 15000,
  "status": "SUCCESS",
  "createdAt": "2026-05-08T10:00:00"
}
```

---

# 7.3 Kafka Producer 예시

```java
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCompletedEvent(Object event) {
        kafkaTemplate.send("payment.completed", event);
    }
}
```

---

# 7.4 Kafka Consumer 예시

```java
@Component
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "payment.completed")
    public void consume(String message) {
        log.info("message={}", message);
    }
}
```

---

# 8. Redis 설계

# 8.1 잔액 캐시

```text
wallet:user:{userId}
```

예시:

```text
wallet:user:1001
```

---

# 8.2 중복 결제 방지

```text
payment:request:{requestId}
```

TTL 기반 처리.

---

# 8.3 Rate Limit

```text
transfer:limit:{userId}
```

송금 API 호출 제한 관리.

---

# 8.4 Redis Lock 예시

```java
Boolean locked = redisTemplate
    .opsForValue()
    .setIfAbsent(
        "payment-lock:" + userId,
        "LOCK",
        Duration.ofSeconds(3)
    );
```

---

# 9. 트랜잭션 처리 전략

# 9.1 잔액 정합성

동시 결제 시 잔액 초과 사용을 방지한다.

## 적용 방식
- Redis 분산 락
- DB 트랜잭션
- 낙관적 락(version column)

---

# 9.2 중복 요청 방지

Idempotency Key 사용.

예시:

```http
Idempotency-Key: UUID
```

동일 요청 재처리 방지.

---

# 9.3 장애 대응

Kafka Consumer 실패 시:
- Retry
- DLQ(Dead Letter Queue)

적용.

---

# 9.4 장애 시나리오

## 상황
결제 완료 후 Kafka 발행 실패

## 해결
Transaction Outbox Pattern 적용

```text
DB Commit
→ Outbox 저장
→ 별도 Scheduler가 Kafka 발행
```

---

# 10. Docker 구성

## docker-compose.yml

```yaml
version: '3'

services:

  postgres:
    image: postgres:15
    container_name: HannahPay-postgres
    environment:
      POSTGRES_DB: HannahPay
      POSTGRES_USER: HannahPay
      POSTGRES_PASSWORD: HannahPay
    ports:
      - "5432:5432"

  redis:
    image: redis:7
    container_name: HannahPay-redis
    ports:
      - "6379:6379"

  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: HannahPay-zookeeper
    environment:
      ALLOW_ANONYMOUS_LOGIN: yes

  kafka:
    image: bitnami/kafka:latest
    container_name: HannahPay-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_ZOOKEEPER_CONNECT: zookeeper:2181
      ALLOW_PLAINTEXT_LISTENER: yes
    depends_on:
      - zookeeper

  payment-api:
    build: .
    container_name: HannahPay-api
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
      - kafka
```

---

# 11. AWS 구성

| 서비스 | 용도 |
|---|---|
| EC2 | 애플리케이션 서버 |
| RDS | PostgreSQL |
| ElastiCache | Redis |
| ECR | Docker 이미지 저장 |
| ECS | 컨테이너 운영 |
| CloudWatch | 로그 및 모니터링 |

---

# 12. 보안 고려사항

- JWT 인증
- HTTPS 적용
- 개인정보 암호화
- API Rate Limit
- 거래 요청 검증
- Password BCrypt 암호화

---

# 13. 프로젝트 디렉토리 구조

```text
HannahPay/
 ├── payment-service/
 ├── wallet-service/
 ├── ledger-service/
 ├── notification-service/
 ├── docker-compose.yml
 └── README.md
```

---

# 14. 향후 확장 계획

- QR 결제
- 정산 시스템
- 관리자 페이지
- FDS(Fraud Detection)
- Open Banking 연동
- Multi Currency 지원
- 실시간 통계 대시보드
- Kubernetes 배포

---

# 15. 기대 효과

- 이벤트 기반 아키텍처 이해
- 금융 트랜잭션 정합성 경험
- Kafka/Redis 실무 활용 경험
- Docker/AWS 운영 경험 확보
- 핀테크 서비스 구조 이해 가능
- 대용량 처리 시스템 설계 경험

---

# 16. 실행 방법

## 16.1 Docker 실행

```bash
docker-compose up -d
```

---

## 16.2 Kafka Topic 생성

```bash
docker exec -it HannahPay-kafka kafka-topics.sh \
--create \
--topic payment.completed \
--bootstrap-server localhost:9092
```

---

## 16.3 Spring Boot 실행

```bash
./gradlew bootRun
```

---

# 17. 테스트 시나리오

## 결제 성공

```text
1. 사용자 충전
2. 잔액 확인
3. 결제 요청
4. 잔액 차감 확인
5. Kafka 이벤트 발행 확인
6. 알림 생성 확인
```

---

## 동시 결제 테스트

```text
1. 동일 계정으로 동시에 결제 요청
2. Redis Lock 동작 확인
3. 잔액 음수 미발생 확인
```

---

## Kafka 장애 테스트

```text
1. Kafka 중지
2. 결제 요청
3. Outbox 저장 확인
4. Kafka 재기동
5. 이벤트 재전송 확인
```
