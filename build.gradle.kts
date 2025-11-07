plugins {
	java
	id("org.springframework.boot") version "3.4.11"
	id("io.spring.dependency-management") version "1.1.7"
	id("java")
}
val springCloudVersion by extra("2024.0.2")

group = "com.conectaciudad"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// --- SPRING Y DEPENDENCIAS NORMALES ---
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.liquibase:liquibase-core:4.33.0")
	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
	implementation("org.springframework.cloud:spring-cloud-starter")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
	runtimeOnly("org.postgresql:postgresql")

	// --- LOMBOK + MAPSTRUCT (CORRECTO PARA ANNOTATION PROCESSING) ---
	compileOnly("org.projectlombok:lombok")  // solo visible en compilación
	annotationProcessor("org.projectlombok:lombok")

	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

	// evita conflictos entre Lombok y MapStruct
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	// --- TESTS ---
	testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
	}
}


tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
	options.annotationProcessorPath = configurations.annotationProcessor.get()
	options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}