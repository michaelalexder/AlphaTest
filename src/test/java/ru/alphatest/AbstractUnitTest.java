package ru.alphatest;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

/**
 * Abstract unit test
 */
@TestPropertySource(locations = "classpath:application-test.properties")
@Rollback
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractUnitTest {

}
