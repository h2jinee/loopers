## 1. 브랜드 목록 조회

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as BrandApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/brands
    C->>AS: 브랜드 목록 조회
    
    AS->>Repo: 브랜드 목록 조회
    Repo->>DB: 브랜드 데이터 조회
    DB-->>Repo: 브랜드 목록
    Repo-->>AS: List<Brand>
    
    Note over AS: 브랜드 이름, 이미지 URL 구성
    
    AS-->>C: 브랜드 목록
    C-->>U: 브랜드 목록 응답
```

## 2. 브랜드 상세 조회 (상품 목록 포함)

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as BrandApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/brands/{brandId}
    C->>AS: 브랜드 상세 조회
    
    AS->>Repo: 브랜드 및 해당 상품 조회
    Repo->>DB: 브랜드, 상품, 좋아요 데이터 조회
    DB-->>Repo: 조회 결과
    
    alt 브랜드 없음
        Repo-->>AS: 브랜드 없음
        AS-->>C: 404 Not Found
        C-->>U: 브랜드를 찾을 수 없습니다
    else 브랜드 존재
        Repo-->>AS: 브랜드 및 상품 목록
        Note over AS: 브랜드 정보와 상품 목록 조합
        AS-->>C: 브랜드 상세 (상품 포함)
        C-->>U: 브랜드 상세 응답
    end
```

## 3. 상품 목록 조회

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as ProductApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/products
    Note over C: 쿼리 파라미터: brandId, sort, page, size
    C->>AS: 상품 목록 조회
    
    Note over AS: 필터/정렬 조건 처리
    
    AS->>Repo: 상품 및 좋아요 수 조회
    Repo->>DB: 조건별 상품 조회
    DB-->>Repo: 상품 데이터
    Repo-->>AS: 상품 목록 및 좋아요 정보
    
    Note over AS: 페이지네이션 처리 (기본 20개)
    
    AS-->>C: 상품 목록
    C-->>U: 상품 목록 응답
```

## 4. 상품 상세 조회

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as ProductApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/products/{productId}
    C->>AS: 상품 상세 조회
    
    AS->>Repo: 상품, 브랜드, 좋아요 조회
    Repo->>DB: 관련 데이터 조회
    DB-->>Repo: 조회 결과
    
    alt 상품 없음
        Repo-->>AS: 상품 없음
        AS-->>C: 404 Not Found
        C-->>U: 상품을 찾을 수 없습니다
    else 상품 존재
        Repo-->>AS: 상품 상세 정보
        Note over AS: 상품, 브랜드, 좋아요 수 조합
        AS-->>C: 상품 상세
        C-->>U: 상품 상세 응답
    end
```

## 5. 좋아요 등록/취소

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as LikeApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: POST/DELETE /api/v1/like/products/{productId}
    Note over C: X-USER-ID 헤더 확인
    
    alt POST (좋아요 등록)
        C->>AS: 좋아요 등록
        AS->>Repo: 기존 좋아요 확인
        
        alt 이미 좋아요 존재
            AS-->>C: 200 OK (멱등성)
        else 좋아요 없음
            AS->>Repo: 좋아요 추가
            AS-->>C: 201 Created
        end
        
    else DELETE (좋아요 취소)
        C->>AS: 좋아요 취소
        AS->>Repo: 좋아요 삭제
        AS-->>C: 200 OK (멱등성)
    end
    
    C-->>U: 처리 완료
```

## 6. 내가 좋아요한 상품 목록

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as LikeApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/like/products
    Note over C: X-USER-ID 헤더 확인
    C->>AS: 좋아요 상품 목록 조회
    
    AS->>Repo: 사용자의 좋아요 및 상품 조회
    Repo->>DB: 좋아요 상품 데이터 조회
    DB-->>Repo: 상품 목록
    Repo-->>AS: 좋아요한 상품 목록
    
    Note over AS: 페이지네이션 처리
    
    AS-->>C: 좋아요 상품 목록
    C-->>U: 좋아요 목록 응답
```

## 7. 주문 생성 단계 (주문 API 내부 - 1단계)

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as OrderApplicationService
    participant DS as OrderDomainService
    participant Repo as Repository
    participant DB as Database

    U->>C: POST /api/v1/orders
    Note over C: X-USER-ID 헤더 확인
    C->>AS: 주문 요청
    
    Note over AS: === 주문 생성 단계 시작 ===
    
    AS->>Repo: 사용자, 상품 조회
    Repo->>DB: 필요 데이터 조회
    DB-->>Repo: 사용자 및 상품 정보
    Repo-->>AS: 도메인 객체
    
    AS->>DS: 주문 생성 처리
    Note over DS: 재고 확인<br/>재고 임시 차감<br/>주문 엔티티 생성
    
    alt 재고 부족
        DS-->>AS: 재고 부족 예외
        AS-->>C: 409 Conflict
        C-->>U: 재고가 부족합니다
    else 재고 있음
        DS->>Repo: 주문 임시 저장
        Repo->>DB: 주문 저장 (결제 대기)
        DB-->>Repo: 저장 완료
        Repo-->>DS: 주문 객체
        DS-->>AS: 주문 생성 완료
        
        Note over AS: === 주문 생성 단계 완료 ===<br/>결제 처리 단계로 진행
    end
```

## 8. 결제 처리 단계 (주문 API 내부 - 2단계)

```mermaid
sequenceDiagram
    participant AS as OrderApplicationService
    participant PAS as PaymentApplicationService
    participant PDS as PaymentDomainService
    participant DS as OrderDomainService
    participant Repo as Repository
    participant DB as Database
    participant C as Controller
    participant U as User
    
    AS->>PAS: 결제 처리 요청
    PAS->>Repo: 사용자 포인트 조회
    Repo->>DB: 포인트 데이터 조회
    DB-->>Repo: 포인트 정보
    Repo-->>PAS: 포인트 도메인 객체
    
    PAS->>PDS: 결제 가능 여부 확인
    Note over PDS: 포인트 잔액 검증<br/>결제 금액 계산
    
    alt 포인트 부족
        PDS-->>PAS: 포인트 부족
        PAS-->>AS: 결제 실패
        AS->>DS: 주문 취소 처리
        Note over DS: 재고 롤백<br/>주문 상태 변경
        AS->>Repo: 변경사항 저장
        AS-->>C: 409 Conflict
        C-->>U: 포인트가 부족합니다
    else 포인트 충분
        PDS-->>PAS: 결제 가능
        PAS->>PDS: 포인트 차감 처리
        Note over PDS: 포인트 차감<br/>포인트 이력 생성
        PDS-->>PAS: 차감된 포인트 객체
        
        PAS->>Repo: 포인트 및 이력 저장
        Repo->>DB: 포인트 업데이트
        PAS-->>AS: 결제 완료
        
        AS->>DS: 주문 확정 처리
        Note over DS: 주문 상태 변경 (결제완료)<br/>재고 확정 차감
        
        AS->>Repo: 주문 상태 업데이트
        AS->>Repo: 트랜잭션 커밋
        Repo->>DB: 모든 변경사항 확정
        DB-->>Repo: 커밋 완료
        
        Note over AS: === 결제 처리 단계 완료 ===
        
        AS-->>C: 주문 및 결제 완료
        C-->>U: 201 Created
    end
```

## 9. 주문 목록 조회

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as OrderApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/orders
    Note over C: X-USER-ID 헤더 확인
    C->>AS: 주문 목록 조회
    
    AS->>Repo: 사용자의 주문 및 상품 조회
    Repo->>DB: 주문 데이터 조회
    DB-->>Repo: 주문 목록
    Repo-->>AS: 주문 및 관련 정보
    
    Note over AS: 주문별 상품 정보 조합<br/>페이지네이션 처리
    
    AS-->>C: 주문 목록
    C-->>U: 주문 목록 응답
```

## 10. 주문 상세 조회

```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant AS as OrderApplicationService
    participant Repo as Repository
    participant DB as Database

    U->>C: GET /api/v1/orders/{orderId}
    Note over C: X-USER-ID 헤더 확인
    C->>AS: 주문 상세 조회
    
    AS->>Repo: 주문 및 상품 정보 조회
    Repo->>DB: 주문 상세 데이터 조회
    DB-->>Repo: 조회 결과
    
    alt 주문 없음 또는 권한 없음
        Repo-->>AS: 조회 실패
        AS-->>C: 404 Not Found
        C-->>U: 주문을 찾을 수 없습니다
    else 정상 조회
        Repo-->>AS: 주문 상세 정보
        Note over AS: 주문, 상품 정보 조합
        AS-->>C: 주문 상세
        C-->>U: 주문 상세 응답
    end
```
