package ru.alphatest;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract endpoints test
 */
public abstract class AbstractControllerTest extends AbstractUnitTest {

    @Getter
    protected MockMvc mockMvc;

    @Autowired
    public AbstractControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
