package com.vts.fxdata.redis;

import com.vts.fxdata.controllers.MainControllerV2;
import com.vts.fxdata.models.dto.Confirmation;
import com.vts.fxdata.models.dto.TradeAck;
import com.vts.fxdata.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RedisMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(RedisMessageHandler.class);
    @Value("${fxdata.redis.executor.stream}")
    private String tradeExecutorStream;
    @Value("${fxdata.redis.agent.stream}")
    private  String confirmationAgentStream;
    @Value("${fxdata.redis.host}")
    private String hostname;
    @Value("${fxdata.redis.port}")
    private int port;
    @Autowired
    private MainControllerV2 controller;
    @Autowired
    private RedisOffsetManager _redisManager;
    private static RedisOffsetManager redisManager;
    private JsonUtils actionAck;
    private JsonUtils tradeAck;

    // Flags to signal threads to stop
    private final AtomicBoolean runningStream1 = new AtomicBoolean(true);
    private final AtomicBoolean runningStream2 = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        redisManager = _redisManager;
        actionAck = new JsonUtils(Confirmation.class);
        tradeAck = new JsonUtils(TradeAck.class);

        var startId1 = getStreamEntryId(hostname, port, tradeExecutorStream);
        var startId2 = getStreamEntryId(hostname, port, confirmationAgentStream);
        var stream1Reader = new Thread(() -> readStream(hostname, port, tradeExecutorStream, startId1, runningStream1, (m) -> handleTradeExecutedMessage(m)));
        var stream2Reader = new Thread(() -> readStream(hostname, port, confirmationAgentStream, startId2, runningStream2, (m) -> handleActionConfirmedMessage(m)));

        stream1Reader.start();
        stream2Reader.start();
    }

    /**
     * Reads data from a Redis stream and processes it using the provided handler.
     */
    private static void readStream(String redisHost, int redisPort, String streamName, StreamEntryID startId, AtomicBoolean running, StreamHandler handler) {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            XReadParams xReadParams = XReadParams.xReadParams()
                    .count(10) // Max number of messages to read at a time
                    .block(0); // Block indefinitely until messages are available

            while (running.get()) {
                // Read from the stream
                List<Map.Entry<String, List<StreamEntry>>> entries = jedis.xread(xReadParams, Map.of(streamName, startId));

                if (entries != null) {
                    for (Map.Entry<String, List<StreamEntry>> entry : entries) {
                        for (StreamEntry message : entry.getValue()) {
                            // Process the message using the provided handler
                            handler.handleMessage(message);

                            // Update start ID to continue from the last read message
                            startId = message.getID();
                        }
                    }
                    // store the last startId
                    redisManager.setOffset(streamName, startId.toString());
                }
            }
        } catch (Exception e) {
            log.error("Error reading stream {}: {}", streamName, e.getMessage());
        }
    }

    /**
     * Handles Trade Executed Messages.
     */
    private void handleTradeExecutedMessage(StreamEntry streamEntry) {
        log.info("Handling Trade Executed Message");
        var fields = streamEntry.getFields();
        for (var message : fields.values()) {
            try {
                var result = controller.acknowledgeTrade((TradeAck) tradeAck.deserialize(message));
                log.info("acknowledgeTrade() returned: {}", result.toString());
            } catch (Exception e) {
                log.error("Failed to handle trade acknowledgement.", e);
            }
        }
    }

    /**
     * Handles Action Confirmed Messages.
     */
    private void handleActionConfirmedMessage(StreamEntry streamEntry) {
        var fields = streamEntry.getFields();
        for (var key : fields.keySet()) {
            var message = fields.get(key);
            log.info("Handling Action Confirmed Message: {}", key);
            try {
                var result = controller.handleConfirmation((Confirmation) actionAck.deserialize(message));
                log.info("confirmationFound() returned: {}", result.toString());
            } catch (Exception e) {
                log.error("Failed to handle action confirmation.", e);
            }
        }
    }

    @PreDestroy
    private void stopReaders() {
        runningStream1.set(false);
        runningStream2.set(false);
    }

    private static StreamEntryID getStreamEntryId(String redisHost, int redisPort, String streamName) {
        var offset = redisManager.getOffset(streamName);
        if (offset==null || offset.isEmpty()) {
            try (Jedis jedis = new Jedis(redisHost, redisPort)) {
                var entries = jedis.xrevrange(streamName, "+", "-"); // Get the latest message

                if (!entries.isEmpty()) {
                    // Get the last entry
                    return entries.get(0).getID();
                }

            } catch (Exception e) {
                log.error("Error reading stream {}: {}", streamName, e.getMessage());
            }
            offset="0-0";
        }
        return new StreamEntryID(offset);
    }

    /**
     * Functional interface for handling messages from Redis streams.
     */
    @FunctionalInterface
    interface StreamHandler {
        void handleMessage(StreamEntry message);
    }
}
