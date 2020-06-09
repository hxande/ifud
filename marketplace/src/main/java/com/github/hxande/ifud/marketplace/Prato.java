package com.github.hxande.ifud.marketplace;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Prato {

	public Long id;

	public String nome;

	public String descricao;

	public Restaurante restaurante;

	public BigDecimal preco;

	public static Multi<PratoDTO> findAll(PgPool pgPool) {

		Uni<RowSet<Row>> preparedQuery = pgPool.preparedQuery("select * from prato");
		return preparedQuery.onItem().produceMulti(rowSet -> Multi.createFrom().items(() -> {
			return StreamSupport.stream(rowSet.spliterator(), false);
		})).onItem().apply(PratoDTO::from);
	}

	public static Multi<PratoDTO> findAll(PgPool client, Long idRestaurante) {

		Uni<RowSet<Row>> preparedQuery = client.preparedQuery(
				"select * from prato where prato.restaurante_id = $1 order by asc",
				io.vertx.mutiny.sqlclient.Tuple.of(idRestaurante));

		return uniToMulti(preparedQuery);
	}

	public static Multi<PratoDTO> uniToMulti(Uni<RowSet<Row>> queryResult) {

		return queryResult.onItem().produceMulti(rowSet -> Multi.createFrom().items(() -> {
			return StreamSupport.stream(rowSet.spliterator(), false);
		})).onItem().apply(PratoDTO::from);
	}

	public static Uni<PratoDTO> findById(PgPool client, Long id) {
		return client.preparedQuery("select * from prato where id = $1", Tuple.of(id)).map(RowSet::iterator)
				.map(iterator -> iterator.hasNext() ? PratoDTO.from(iterator.next()) : null);
	}
}
