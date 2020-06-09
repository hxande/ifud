package com.github.hxande.ifud.pedido;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PedidoDeserializer extends ObjectMapperDeserializer<PedidoRealizadoDTO> {

	public PedidoDeserializer() {
		super(PedidoRealizadoDTO.class);
		// TODO Auto-generated constructor stub
	}

}
