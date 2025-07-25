```mermaid
classDiagram
    class User {
        +userId: String
        +email: String
        +gender: Gender
        +birthDate: Date
        +createdAt: DateTime
        +updatedAt: DateTime
        +deletedAt: DateTime
        +getUserInfo()
    }

    class Point {
        +pointId: String
        +userId: String
        +balance: Integer
        +createdAt: DateTime
        +updatedAt: DateTime
        +getBalance(): Integer
        +checkSufficientBalance(amount: Integer): Boolean
    }

    class PointHistory {
        +historyId: String
        +userId: String
        +amount: Integer
        +type: PointType
        +description: String
        +orderId: String
        +createdAt: DateTime
    }

    class PointType {
        <<enumeration>>
        CHARGE
        USE
        REFUND
    }

    class Product {
        +productId: String
        +brandId: String
        +code: String
        +nameKo: String
        +nameEn: String
        +price: Integer
        +description: String
        +stock: Integer
        +tempReservedStock: Integer
        +status: ProductStatus
        +releaseYear: Integer
        +category: String
        +shippingFee: Integer
        +createdAt: DateTime
        +updatedAt: DateTime
        +deletedAt: DateTime
        +checkStock(): Boolean
        +updateStock(quantity: Integer)
    }

    class Brand {
        +brandId: String
        +nameKo: String
        +nameEn: String
        +categories: List~String~
        +coverImageUrl: String
        +profileImageUrl: String
        +createdAt: DateTime
        +updatedAt: DateTime
        +deletedAt: DateTime
        +getBrandInfo()
        +getCategories(): List~String~
    }

    class Order {
        +orderId: String
        +userId: String
        +totalAmount: Integer
        +status: OrderStatus
        +receiverName: String
        +receiverPhone: String
        +receiverZipCode: String
        +receiverAddress: String
        +receiverAddressDetail: String
        +paymentDeadline: DateTime
        +createdAt: DateTime
        +updatedAt: DateTime
        +deletedAt: DateTime
        +updateStatus(status: OrderStatus)
        +isPaymentExpired(): Boolean
    }

    class OrderLine {
        +orderLineId: String
        +orderId: String
        +productId: String
        +quantity: Integer
        +price: Integer
        +createdAt: DateTime
        +updatedAt: DateTime
    }

    class Like {
        +likeId: String
        +userId: String
        +productId: String
        +createdAt: DateTime
    }

    class ProductImage {
        +imageId: String
        +productId: String
        +imageUrl: String
        +imageType: ImageType
        +displayOrder: Integer
        +createdAt: DateTime
        +updatedAt: DateTime
    }

    class ImageType {
        <<enumeration>>
        THUMBNAIL
        DETAIL
    }

    class LikeAggregation {
        +productId: String
        +likeCount: Integer
        +lastBatchTime: DateTime
        +createdAt: DateTime
        +updatedAt: DateTime
    }

    class Payment {
        +paymentId: String
        +orderId: String
        +amount: Integer
        +status: PaymentStatus
        +attemptedAt: DateTime
        +createdAt: DateTime
        +updatedAt: DateTime
        +processPayment()
    }

    class Delivery {
        +deliveryId: String
        +orderId: String
        +status: DeliveryStatus
        +trackingNumber: String
        +createdAt: DateTime
        +updatedAt: DateTime
        +updateStatus(status: DeliveryStatus)
    }

    class Gender {
        <<enumeration>>
        MALE
        FEMALE
        OTHER
    }

    class ProductStatus {
        <<enumeration>>
        AVAILABLE
        OUT_OF_STOCK
        DISCONTINUED
    }

    class OrderStatus {
        <<enumeration>>
        PAYMENT_PENDING
        PAYMENT_COMPLETED
        PREPARING_SHIPMENT
        SHIPPING
        DELIVERED
        CANCELLED
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        COMPLETED
        FAILED
    }

    class DeliveryStatus {
        <<enumeration>>
        PREPARING
        SHIPPING
        DELIVERED
    }

    %% Relationships
    User "1" --> "*" Order : 소유
    User "1" --> "*" Like : 소유
    User "1" --> "1" Point : 소유
    User "1" --> "*" PointHistory : 소유
    
    Product "*" --> "1" Brand : 참조
    Product "1" --> "*" OrderLine : 참조됨
    Product "1" --> "*" Like : 참조됨
    Product "1" --> "0..1" LikeAggregation : 집계됨
    Product "1" --> "*" ProductImage : 소유
    
    Order "1" --> "*" OrderLine : 소유
    Order "1" --> "0..1" Payment : 소유
    Order "1" --> "0..1" Delivery : 소유
    Order "*" --> "1" User : 참조
    
    Brand "1" --> "*" Product : 소유
    
    OrderLine "*" --> "1" Product : 참조
    OrderLine "*" --> "1" Order : 참조
    
    Like "*" --> "1" User : 참조
    Like "*" --> "1" Product : 참조
    
    Payment "*" --> "1" Order : 참조
    
    Delivery "*" --> "1" Order : 참조
    
    LikeAggregation "*" --> "1" Product : 참조
    
    ProductImage "*" --> "1" Product : 참조
    
    Point "*" --> "1" User : 참조
    
    PointHistory "*" --> "1" User : 참조
    PointHistory "*" --> "0..1" Order : 참조
```
