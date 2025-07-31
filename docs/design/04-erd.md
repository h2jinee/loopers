```mermaid
erDiagram
    USERS {
        bigint user_id PK
        varchar email UK
        varchar gender
        date birth_date
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    BRANDS {
        bigint brand_id PK
        varchar name_ko
        varchar name_en
        varchar cover_image_url
        varchar profile_image_url
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    BRAND_CATEGORIES {
        bigint category_id PK
        bigint brand_id FK
        varchar name_ko
        varchar name_en
        varchar code UK
        int display_order
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    PRODUCTS {
        bigint product_id PK
        bigint brand_id FK
        bigint category_id FK
        varchar code UK
        varchar name_ko
        int price
        text description
        int stock
        varchar status
        int release_year
        int shipping_fee
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    PRODUCT_STOCK_RESERVATIONS {
        bigint reservation_id PK
        bigint product_id FK
        bigint order_id FK
        int quantity
        datetime expired_at
        varchar status
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    PRODUCT_IMAGES {
        bigint image_id PK
        bigint product_id FK
        varchar image_url
        varchar image_type
        int display_order
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    PRODUCT_COUNTS {
        bigint product_id PK FK
        int like_count
        int order_count
        datetime last_updated_at
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    ORDERS {
        bigint order_id PK
        bigint user_id FK
        int total_amount
        varchar status
        varchar receiver_name
        varchar receiver_phone
        varchar receiver_zip_code
        varchar receiver_address
        varchar receiver_address_detail
        datetime payment_deadline
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    ORDER_LINES {
        bigint order_line_id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        int price
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    LIKES {
        bigint like_id PK
        bigint user_id FK
        bigint product_id FK
        datetime created_at
        datetime deleted_at
    }

    POINTS {
        bigint point_id PK
        bigint user_id FK UK
        int balance
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    POINT_HISTORIES {
        bigint history_id PK
        bigint user_id FK
        int amount
        varchar type "CHARGE, USE, REFUND"
        varchar description
        bigint order_id FK "nullable"
        datetime created_at
        datetime deleted_at
    }

    PAYMENTS {
        bigint payment_id PK
        bigint order_id FK
        bigint user_id FK
        bigint point_history_id FK
        int amount
        varchar status
        datetime attempted_at
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    DELIVERIES {
        bigint delivery_id PK
        bigint order_id FK
        varchar status
        varchar tracking_number
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    USERS ||--o{ ORDERS : places
    USERS ||--o{ LIKES : has
    USERS ||--|| POINTS : owns
    USERS ||--o{ POINT_HISTORIES : has
    
    BRANDS ||--o{ BRAND_CATEGORIES : has
    BRANDS ||--o{ PRODUCTS : has
    
    BRAND_CATEGORIES }o--|| BRANDS : belongs_to
    BRAND_CATEGORIES ||--o{ PRODUCTS : categorizes
    
    PRODUCTS }o--|| BRANDS : belongs_to
    PRODUCTS }o--|| BRAND_CATEGORIES : categorized_by
    PRODUCTS ||--o{ ORDER_LINES : included_in
    PRODUCTS ||--o{ LIKES : received
    PRODUCTS ||--|| PRODUCT_COUNTS : tracked_by
    PRODUCTS ||--o{ PRODUCT_IMAGES : has
    PRODUCTS ||--o{ PRODUCT_STOCK_RESERVATIONS : reserved
    
    ORDERS ||--o{ ORDER_LINES : contains
    ORDERS ||--o| PAYMENTS : paid_by
    ORDERS ||--o| DELIVERIES : shipped_by
    ORDERS }o--|| USERS : placed_by
    ORDERS ||--o{ PRODUCT_STOCK_RESERVATIONS : reserves
    
    ORDER_LINES }o--|| ORDERS : belongs_to
    ORDER_LINES }o--|| PRODUCTS : references
    
    LIKES }o--|| USERS : by
    LIKES }o--|| PRODUCTS : for
    
    PRODUCT_COUNTS ||--|| PRODUCTS : aggregates
    
    POINTS ||--|| USERS : belongs_to
    
    POINT_HISTORIES }o--|| USERS : belongs_to
    POINT_HISTORIES }o--o| ORDERS : related_to
    
    PAYMENTS ||--|| ORDERS : for
    PAYMENTS }o--|| USERS : paid_by
    PAYMENTS }o--|| POINT_HISTORIES : creates
    
    DELIVERIES ||--|| ORDERS : for
    
    PRODUCT_IMAGES }o--|| PRODUCTS : belongs_to
    
    PRODUCT_STOCK_RESERVATIONS }o--|| PRODUCTS : for
    PRODUCT_STOCK_RESERVATIONS }o--|| ORDERS : by
```
