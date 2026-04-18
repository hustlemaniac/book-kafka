package com.bookstore.shippingservice.streams;

import com.bookstore.shippingservice.dto.InventoryEvent;
import com.bookstore.shippingservice.dto.ShipmentEvent;
import com.bookstore.shippingservice.model.Shipment;
import com.bookstore.shippingservice.repository.ShipmentRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Map;

@Configuration
@EnableKafkaStreams
public class ShippingStreamTopology {

    private static final Logger log = LoggerFactory.getLogger(ShippingStreamTopology.class);

    private static final String INPUT_TOPIC  = "inventory-updated";
    private static final String OUTPUT_TOPIC = "order-shipped";

    private final ShipmentRepository shipmentRepository;

    public ShippingStreamTopology(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Bean
    public KStream<String, InventoryEvent> shippingStream(StreamsBuilder builder) {

        JsonSerde<InventoryEvent> inventorySerde = new JsonSerde<>(InventoryEvent.class);
        inventorySerde.configure(
                Map.of(
                        JsonDeserializer.TRUSTED_PACKAGES, "*",
                        JsonDeserializer.USE_TYPE_INFO_HEADERS, "false",
                        JsonDeserializer.VALUE_DEFAULT_TYPE, InventoryEvent.class.getName()
                ), false
        );

        JsonSerde<ShipmentEvent> shipmentSerde = new JsonSerde<>(ShipmentEvent.class);
        shipmentSerde.configure(
                Map.of(
                        JsonDeserializer.TRUSTED_PACKAGES, "*",
                        JsonDeserializer.USE_TYPE_INFO_HEADERS, "false"
                ), false
        );
//     step 1: consume
        KStream<String, InventoryEvent> stream = builder.stream(
                INPUT_TOPIC,
                Consumed.with(Serdes.String(), inventorySerde)
        );

        // ── Step 2: filter ────────────────────────────────────────────
        // Message filtering — only RESERVED events move forward.
        // Events with any other status are silently dropped here.
        KStream<String, InventoryEvent> reservedStream = stream
                .filter((orderId, event) -> {
                    boolean pass = "RESERVED".equals(event.getInventoryStatus());
                    if (!pass) log.warn("Dropping event | orderId={} | status={}",
                            orderId, event.getInventoryStatus());
                    return pass;
                });

        // ── Step 3: mapValues ─────────────────────────────────────────
        // Transform InventoryEvent → ShipmentEvent and save to DB as a side effect
        KStream<String, ShipmentEvent> shipmentStream = reservedStream
                .mapValues(event -> {
                    // persist shipment record
                    Shipment shipment = new Shipment();
                    shipment.setOrderId(event.getOrderId());
                    shipment.setCustomerId(event.getCustomerId());
                    shipment.setStatus("SHIPPED");
                    shipmentRepository.save(shipment);

                    log.info("Shipment created | orderId={}", event.getOrderId());

                    // build outgoing event
                    ShipmentEvent shipmentEvent = new ShipmentEvent();
                    shipmentEvent.setOrderId(event.getOrderId());
                    shipmentEvent.setCustomerId(event.getCustomerId());
                    shipmentEvent.setShipmentStatus("SHIPPED");
                    return shipmentEvent;
                });

        // ── Step 4: to ────────────────────────────────────────────────
        // Write the transformed stream to the output topic
        shipmentStream.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), shipmentSerde));

        return stream;
    }
}