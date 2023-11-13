import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ioMockkVersion = "1.13.8"
val springDocVersion = "2.2.0"
val ioGithubOshaiVersion = "5.1.0"
val ninjaSquadVersion = "4.0.2"
val springRetryVersion = "2.0.4"
val ehcacheVersion = "3.10.8"
val javaxCacheVersion = "1.1.1"
val aspectjweaverVersion = "1.9.20.1"
val okHttp3Version = "4.12.0"
val jacocoVersion = "0.8.11"
val jsonWebTokenVersion = "0.12.3"

plugins {
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	id("com.adarshr.test-logger") version "4.0.0"
	id("jacoco")
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
}

group = "com.distasilucas"
version = "1.0.0-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
	implementation("io.github.oshai:kotlin-logging-jvm:$ioGithubOshaiVersion")
	implementation("org.springframework.retry:spring-retry:$springRetryVersion")
	implementation("org.ehcache:ehcache:$ehcacheVersion")
	implementation("javax.cache:cache-api:$javaxCacheVersion")
	implementation("com.squareup.okhttp3:okhttp:$okHttp3Version")
	implementation("io.jsonwebtoken:jjwt-api:${jsonWebTokenVersion}")

	runtimeOnly("io.jsonwebtoken:jjwt-impl:${jsonWebTokenVersion}")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonWebTokenVersion}")
	runtimeOnly("org.aspectj:aspectjweaver:$aspectjweaverVersion")

	testImplementation("io.mockk:mockk:$ioMockkVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.ninja-squad:springmockk:$ninjaSquadVersion") {
		exclude(module = "mockito-core")
	}
	testImplementation("com.squareup.okhttp3:mockwebserver:$okHttp3Version")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

testlogger {
	theme = ThemeType.PLAIN
	showExceptions = true
	showStackTraces = true
	showFullStackTraces = false
	showSummary = true
	showPassed = true
	showSkipped = true
	showFailed = true
}

jacoco {
	toolVersion = jacocoVersion
}

tasks.withType<JacocoReport> {
	reports {
		xml.required.set(false)
		csv.required.set(true)
		html.required.set(true)
	}

	afterEvaluate {
		classDirectories.setFrom(files(classDirectories.files.map {
			fileTree(it).apply {
				exclude(
					"**/configuration/**",
					"**/CryptoBalanceTrackerApplication**",
					"**/*\$logger\$*.class"
				)
			}
		}))
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.bootJar {
	archiveFileName = "${project.name}.jar"
	launchScript()
}