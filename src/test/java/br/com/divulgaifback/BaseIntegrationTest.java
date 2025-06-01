package br.com.divulgaifback;


import br.com.divulgaifback.config.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {DivulgaifBackApplication.class, TestDatabaseConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public abstract class BaseIntegrationTest {

    @BeforeEach
    void setUp() {
    }
}