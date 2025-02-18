package io.lsdconsulting.lsd.distributed.firestore.integration.testapp

import io.lsdconsulting.lsd.distributed.firestore.config.LibraryConfig
import io.lsdconsulting.lsd.distributed.firestore.integration.testapp.config.CredentialsConfig
import io.lsdconsulting.lsd.distributed.firestore.integration.testapp.config.TestRestTemplateConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CredentialsConfig::class, LibraryConfig::class, TestRestTemplateConfig::class)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}