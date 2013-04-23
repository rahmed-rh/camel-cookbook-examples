package org.camelcookbook.examples.testing.advicewith;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class that uses Camel's built-in AOP functionality to override the fixed endpoints.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/META-INF/spring/fixedEndpoints-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@UseAdviceWith(true)
public class FixedEndpointEnhancedSpringTest {

    @Autowired
    private ModelCamelContext context;

    @Test
    public void testOverriddenEndpoints() throws Exception {
        context.getRouteDefinition("modifyPayloadBetweenQueues")
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        replaceFromWith("direct:in");

                        interceptSendToEndpoint("activemq:out")
                                .skipSendToOriginalEndpoint()
                                .to("mock:out");
                    }
                });
        context.start();

        MockEndpoint out = context.getEndpoint("mock:out", MockEndpoint.class);
        out.setExpectedMessageCount(1);
        out.message(0).body().isEqualTo("Modified: Cheese");

        ProducerTemplate template = context.createProducerTemplate();
        template.sendBody("direct:in", "Cheese");

        out.assertIsSatisfied();
    }
}
