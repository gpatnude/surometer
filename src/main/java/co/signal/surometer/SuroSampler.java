package co.signal.surometer;

import java.util.Properties;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.netflix.suro.ClientConfig;
import com.netflix.suro.client.SuroClient;
import com.netflix.suro.message.Message;

/**
 * A {@link org.apache.jmeter.samplers.Sampler Sampler} which produces Suro messages.
 * 
 * @author starzia
 */
public class SuroSampler extends AbstractJavaSamplerClient {

    /** Suro routing key for this particular message. */
    private static final String PARAMETER_MSG_ROUTING_KEY = "SuroSampler.MsgRoutingKey";
    /** Message payload. */
    private static final String PARAMETER_MSG_PAYLOAD = "SuroSampler.MsgPayload";
    
    /**
     * Parameter for setting the Suro servers; it should be comma separated list of $hostname:$port".
     */
    private static final String PARAMETER_LOAD_BALANCER_SERVER = "SuroClient.loadBalancerServer";
    /**
     * Parameter for making requests either synchronous or asynchronous; should be "sync" or "async".
     * Note that "async" mode implies that the sampler will return immediately and report success
     * once the request is queued.
     */
    private static final String PARAMETER_CLIENT_TYPE = "SuroClient.clientType";
    /**
     * When the number of messages queued is up to this value, the client will create and send MessageSet.
     */
    private static final String PARAMETER_ASYNC_BATCH_SIZE = "SuroClient.asyncBatchSize";

    private SuroClient client;


    @Override
    public void setupTest(JavaSamplerContext context) {
        // setup the SuroClient
        final Properties clientProperties = new Properties();
        clientProperties.setProperty( ClientConfig.LB_TYPE, "static" );
        clientProperties.setProperty( ClientConfig.LB_SERVER, 
                                      context.getParameter( PARAMETER_LOAD_BALANCER_SERVER ) );
        clientProperties.setProperty( ClientConfig.CLIENT_TYPE, 
                                      context.getParameter( PARAMETER_CLIENT_TYPE ) );
        clientProperties.setProperty( ClientConfig.ASYNC_BATCH_SIZE, 
                                      context.getParameter( PARAMETER_ASYNC_BATCH_SIZE ) );
        client = new SuroClient(clientProperties);
    }


    @Override
    public void teardownTest( JavaSamplerContext context ) {
        // shutdown the SuroClient
        client.shutdown();
    }


    @Override
    public Arguments getDefaultParameters() {
      Arguments defaultParameters = new Arguments();

      defaultParameters.addArgument( PARAMETER_MSG_ROUTING_KEY, "routingKey");
      defaultParameters.addArgument( PARAMETER_MSG_PAYLOAD, "Hello World");

      defaultParameters.addArgument( PARAMETER_LOAD_BALANCER_SERVER, "localhost:7101");
      defaultParameters.addArgument( PARAMETER_CLIENT_TYPE, "sync");
      defaultParameters.addArgument( PARAMETER_ASYNC_BATCH_SIZE, "200");

      return defaultParameters;
    }


    public SampleResult runTest(JavaSamplerContext context) {
        // sent request
        client.send( new Message( context.getParameter(PARAMETER_MSG_ROUTING_KEY),
                                  context.getParameter(PARAMETER_MSG_PAYLOAD).getBytes()) );

        // create result
        // !!!: I am not aware of any error reporting mechanism for SuroClient.send()
        SampleResult result = new SampleResult();
        result.setSuccessful(true);
        return result;
    }
    

}