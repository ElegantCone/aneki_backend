package itmo.devops.aneki;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class AnekiApplicationTests {

    @Test
    void applicationIsBootApp() {
        assertThat(AnekiApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }
}
