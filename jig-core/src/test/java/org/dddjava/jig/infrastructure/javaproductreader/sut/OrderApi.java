package org.dddjava.jig.infrastructure.javaproductreader.sut;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderApi {

    // Javadocを書かないことで、Swaggerの説明が用語になることを表す
    @PostMapping("/orders")
    @Operation(summary = "注文する")
    public void order() {
    }
}
