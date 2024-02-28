package org.network.backend.controllers

import org.apache.commons.io.IOUtils
import org.network.backend.dto.ProductDTO
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.notExists

@RestController
@RequestMapping("/api/v1")

class ProductsController {

    var counter = 0
    val productsMap = hashMapOf<Int, ProductDTO>()

    final val iconsDir: Path = Paths.get("./icons")

    init {
        if (!iconsDir.toFile().exists()) {
            Files.createDirectories(iconsDir)
        }
    }

    @PostMapping("/public/products")
    fun addProduct(@RequestBody value: ProductDTO): ResponseEntity<*> {
        value.id = counter++
        productsMap[value.id!!] = value
        return ResponseEntity(value, HttpStatus.OK)
    }

    @PostMapping("/public/products/{productID}/image")
    fun uploadProductIcon(@PathVariable productID: Int, @RequestParam("file") file: MultipartFile): ResponseEntity<*> {
        if (!productsMap.containsKey(productID)) {
            return ResponseEntity("Product not found", HttpStatus.NOT_FOUND)
        }
        val fileName = file.originalFilename ?: "icon-$productID"
        val targetLocation = iconsDir.resolve(fileName)
        Files.copy(file.inputStream, targetLocation)

        val product = productsMap[productID]
        product?.icon = fileName
        return ResponseEntity.ok().build<Any>()
    }

    @GetMapping("/public/products/{productID}/image", produces = arrayOf("image/jpeg", "image/png"))
    fun getProductIcon(@PathVariable productID: Int): ResponseEntity<*> {
        val product = productsMap[productID] ?: return ResponseEntity("Product not found", HttpStatus.NOT_FOUND)
        val iconPath = product.icon?.let { iconsDir.resolve(it) } ?: return ResponseEntity("Icon not found", HttpStatus.NOT_FOUND)

        val resource = Files.newInputStream(iconPath)
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/jpeg")).body(IOUtils.toByteArray(resource))
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