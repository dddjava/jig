package jig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void 起動するよ() {
        ResponseEntity<String> actual =
                testRestTemplate.getForEntity("/", String.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}