package org.network.backend.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductDTO (
    @JsonProperty("id")
    var id: Int? = -1 ,
    @JsonProperty("name")
    var name: String? = "",
    @JsonProperty("description")
    var description: String? = ""
)