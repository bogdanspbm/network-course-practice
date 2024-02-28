package org.network.backend.controllers

import org.network.backend.dto.ProductDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")

class ProductsController {

    var counter = 0
    val productsMap = hashMapOf<Int, ProductDTO>()

    @PostMapping("/public/products")
    fun addProduct(@RequestBody value: ProductDTO): ResponseEntity<*> {
        value.id = counter++
        productsMap[value.id!!] = value
        return ResponseEntity(value, HttpStatus.OK)
    }

    @GetMapping("/public/products")
    fun getAllProducts(): ResponseEntity<*> {
        return ResponseEntity(productsMap.values.toList(), HttpStatus.OK)
    }

    @GetMapping("/public/products/{id}")
    fun getProduct(@PathVariable id: Int): ResponseEntity<*> {
        if (!productsMap.containsKey(id)) {
            return ResponseEntity("{}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity(productsMap[id], HttpStatus.OK)
    }

    @DeleteMapping("/public/products/{id}")
    fun deleteProduct(@PathVariable id: Int): ResponseEntity<*> {
        if (!productsMap.containsKey(id)) {
            return ResponseEntity("{}", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        val product = productsMap[id];
        productsMap.remove(id)

        return ResponseEntity(product, HttpStatus.OK)
    }

    @PutMapping("/public/products/{id}")
    fun updateProduct(@RequestBody value: ProductDTO, @PathVariable id: Int): ResponseEntity<*> {
        if (!productsMap.containsKey(id)) {
            return ResponseEntity("{}", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        value.id = id
        productsMap[value.id!!] = value
        return ResponseEntity(value, HttpStatus.OK)
    }

}