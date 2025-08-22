plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // jpa
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    // configuration processor for IDE support
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // querydsl
    api("com.querydsl:querydsl-jpa::jakarta")
    // jdbc-mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.testcontainers:mysql")

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation("org.testcontainers:mysql")
}
