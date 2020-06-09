package com.github.hxande.ifud.marketplace;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.vertx.mutiny.pgclient.PgPool;

@ApplicationScoped
public class RestauranteCadastrado {
	
	@Inject
	PgPool pgPool;

	@Incoming("restaurantes")
	public void receberRestaurantes(String json) {

		Jsonb create = JsonbBuilder.create();
		Restaurante restaurante = create.fromJson(json, Restaurante.class);

		restaurante.persist(pgPool);

	}
}