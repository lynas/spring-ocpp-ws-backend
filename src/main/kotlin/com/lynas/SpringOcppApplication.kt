package com.lynas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringOcppApplication

fun main(args: Array<String>) {
	runApplication<SpringOcppApplication>(*args)
}
