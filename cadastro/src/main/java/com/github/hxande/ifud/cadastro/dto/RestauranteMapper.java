package com.github.hxande.ifud.cadastro.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.github.hxande.ifud.cadastro.Restaurante;

@Mapper(componentModel = "cdi")
public interface RestauranteMapper {

	public Restaurante toRestaurante(AdicionarRestauranteDTO dto);
	
	public void toRestaurante(AtualizarRestauranteDTO dto, @MappingTarget Restaurante r);
	
	@Mapping(target = "dataCriacao", dateFormat = "dd/MM/yyyy HH:mmss")
	public RestauranteDTO toRestaurante(Restaurante dto);

}
