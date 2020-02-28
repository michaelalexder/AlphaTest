package ru.alphatest.user;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.alphatest.AbstractControllerTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User endpoints test
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest extends AbstractControllerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserControllerTest(MockMvc mockMvc) {
        super(mockMvc);
    }

    private void executeInsertQuery(Session session, String id, UserType userType, boolean blackList) {
        session.createNativeQuery(MessageFormat
                .format("INSERT INTO users VALUES (''{0}'', ''{1}'', {2})", id, userType.name(), blackList)).executeUpdate();
    }

    @BeforeAll
    public void insertData() {
        try (Session session = entityManager.getEntityManagerFactory().createEntityManager().unwrap(Session.class)) {
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("CREATE TABLE users (\n" +
                    "\tid varchar(6),\n" +
                    "\tuser_type varchar(6),\n" +
                    "\tblack_list boolean\n" +
                    ")").executeUpdate();
            // for load testing
            IntStream.range(1000, 2000).forEach(value -> executeInsertQuery(session, "ID" + value, UserType.FL, true));

            // for result testing
            executeInsertQuery(session, "ID2001", UserType.FL, true);
            executeInsertQuery(session, "ID2002", UserType.FL, false);
            executeInsertQuery(session, "ID2003", UserType.UL, true);
            executeInsertQuery(session, "ID2004", UserType.UL, false);
            tx.commit();
        }
    }

    private String endpointResponse(String id) throws Exception {
        MvcResult result = mockMvc
                .perform(get("/api/users/" + id))
                .andReturn();
        return mockMvc.perform(asyncDispatch(result)).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void createTest() throws Exception {

        // test status
        this.mockMvc.perform(asyncDispatch(mockMvc
                .perform(get("/api/users/2001"))
                .andReturn())).andExpect(status().isBadRequest());
        this.mockMvc.perform(asyncDispatch(mockMvc
                .perform(get("/api/users/ID2001"))
                .andReturn())).andExpect(status().isOk());

        // test response
        Assert.assertEquals(endpointResponse("ID2001"), RiskProfile.RB.name());
        Assert.assertEquals(endpointResponse("ID2002"), RiskProfile.RG.name());
        Assert.assertEquals(endpointResponse("ID2003"), RiskProfile.CB.name());
        Assert.assertEquals(endpointResponse("ID2004"), RiskProfile.CG.name());

        // test load
        int totalTasks = 1000;
        CountDownLatch latch = new CountDownLatch(totalTasks);
        ExecutorService executor = Executors.newFixedThreadPool(totalTasks);
        Instant start = Instant.now();
        IntStream.range(1, totalTasks + 1).forEach(v -> executor.execute(new RequestPerformer(latch, "ID" + (v + 1000))));
        latch.await();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Total time for 1000 requests: " + timeElapsed + "ms");
    }

    private class RequestPerformer implements Runnable {

        private CountDownLatch latch;

        private String id;

        public RequestPerformer(CountDownLatch latch, String id) {
            this.latch = latch;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                MvcResult result = mockMvc
                        .perform(get("/api/users/" + id))
                        .andReturn();
                mockMvc.perform(asyncDispatch(result)).andDo((responseResult) -> latch.countDown());
            } catch (Exception e) {
                latch.countDown();
                throw new RuntimeException(e);
            }
        }
    }
}
