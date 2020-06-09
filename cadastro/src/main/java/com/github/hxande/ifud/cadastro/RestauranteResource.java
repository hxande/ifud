package com.github.hxande.ifud.cadastro;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.github.hxande.ifud.cadastro.dto.AdicionarPratoDTO;
import com.github.hxande.ifud.cadastro.dto.AdicionarRestauranteDTO;
import com.github.hxande.ifud.cadastro.dto.AtualizarPratoDTO;
import com.github.hxande.ifud.cadastro.dto.AtualizarRestauranteDTO;
import com.github.hxande.ifud.cadastro.dto.PratoDTO;
import com.github.hxande.ifud.cadastro.dto.PratoMapper;
import com.github.hxande.ifud.cadastro.dto.RestauranteDTO;
import com.github.hxande.ifud.cadastro.dto.RestauranteMapper;
import com.github.hxande.ifud.cadastro.infra.ConstraintViolationResponse;

import io.quarkus.security.ForbiddenException;

@Path("/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "restaurante")
@RolesAllowed("Proprietario")
@SecurityScheme(securitySchemeName = "ifud-oauth", type = SecuritySchemeType.OAUTH2, flows = 
@OAuthFlows(password = @OAuthFlow(tokenUrl = "http://localhost:8180/auth/realms/ifud/protocol/openid-connect/token")))
@SecurityRequirement(name = "ifud-oauth")
public class RestauranteResource {

	@Inject
	RestauranteMapper restauranteMapper;

	@Inject
	PratoMapper pratoMapper;
	
	@Inject
	@Channel("restaurantes")
	Emitter<Restaurante> emitter;
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	@Claim(standard = Claims.sub)
	String sub;

	@GET
	@Counted(name = "Quantidade de buscas de restaurantes")
	@SimplyTimed(name = "Templo simples de busca de restaurantes")
	@Timed(name = "Tempo completo de busca de restaurantes")
	public List<RestauranteDTO> listar() {
		Stream<Restaurante> restaurantes = Restaurante.streamAll();
		return restaurantes.map(r -> restauranteMapper.toRestaurante(r)).collect(Collectors.toList());
	}

	@POST
	@Transactional
	@APIResponse(responseCode = "201", description = "Restaurante cadastrado com sucesso")
	@APIResponse(responseCode = "400", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
	public Response adicionar(@Valid AdicionarRestauranteDTO dto) {
		Restaurante restaurante = restauranteMapper.toRestaurante(dto);
		restaurante.proprietario = sub;
		restaurante.persist();
		
		emitter.send(restaurante);
		
		return Response.status(Status.CREATED).build();
	}

	@PUT
	@Transactional
	@Path("{id}")
	public void atualizar(@PathParam("id") Long id, AtualizarRestauranteDTO dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException();
		}
		Restaurante restaurante = restauranteOp.get();
		
		if (!restaurante.proprietario.equals(sub)) {
			throw new ForbiddenException();
		}
		
		restauranteMapper.toRestaurante(dto, restaurante);
		restaurante.persist();
	}

	@DELETE
	@Transactional
	@Path("{id}")
	public void deletar(@PathParam("id") Long id) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		if (!restauranteOp.get().proprietario.equals(sub)) {
			throw new ForbiddenException();
		}
		restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
			throw new NotFoundException();
		});
	}

	@GET
	@Path("/{idRestaurante}/pratos")
	@Tag(name = "prato")
	public List<PratoDTO> listarPratos(@PathParam("idRestaurante") Long idRestaurante) {
		Stream<Prato> pratos = Prato.streamAll();
		return pratos.map(p -> pratoMapper.toPrato(p)).collect(Collectors.toList());
	}

	@POST
	@Path("/{idRestaurante}/pratos")
	@Transactional
	@Tag(name = "prato")
	public Response adicionarPratos(@PathParam("idRestaurante") Long idRestaurante, AdicionarPratoDTO dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}
		Prato prato = pratoMapper.toPrato(dto);
		prato.restaurante = restauranteOp.get();
		prato.persist();
		return Response.status(Status.CREATED).build();
	}

	@PUT
	@Transactional
	@Path("/{idRestaurante}/pratos/{id}")
	@Tag(name = "prato")
	public void atualizarPratos(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id,
			AtualizarPratoDTO dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Optional<Prato> pratoOp = Prato.findByIdOptional(id);
		if (pratoOp.isEmpty()) {
			throw new NotFoundException("Prato não existe");
		}

		Prato prato = pratoOp.get();
		pratoMapper.toPrato(dto, prato);
		prato.persist();
	}

	@DELETE
	@Transactional
	@Path("/{idRestaurante}/pratos/{id}")
	@Tag(name = "prato")
	public void deletarPratos(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, Prato dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Optional<Prato> pratoOp = Prato.findByIdOptional(id);

		pratoOp.ifPresentOrElse(Prato::delete, () -> {
			throw new NotFoundException();
		});
	}
}