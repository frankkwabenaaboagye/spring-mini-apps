package rewards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class RewardNetworkTest {

    private RewardNetwork rewardNetwork;

    @BeforeEach
    void setUp() {
        ApplicationContext applicationContext =  SpringApplication.run(TestInfrastructureConfig.class);
        rewardNetwork = applicationContext.getBean("rewardNetwork", RewardNetwork.class);
    }

    @Test
    void testRewardForDining() {
    }
}