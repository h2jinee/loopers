dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":modules:redis"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))
    implementation(project(":supports:monitoring"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")

    // resilience
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker")

    implementation ("io.micrometer:micrometer-registry-prometheus")
    implementation ("io.github.resilience4j:resilience4j-micrometer")
    implementation ("io.github.resilience4j:resilience4j-spring-boot3")

    // feign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    
    // querydsl
    implementation("com.querydsl:querydsl-jpa::jakarta")

    // datafaker
    implementation("net.datafaker:datafaker:2.4.1")
    
    // configuration processor for IDE support
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
    testImplementation(testFixtures(project(":modules:redis")))
}
