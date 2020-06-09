package com.github.hxande.ifud.cadastro.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.github.hxande.ifud.cadastro.Prato;

@Mapper(componentModel = "cdi")
public interface PratoMapper {

	public Prato toPrato(AdicionarPratoDTO dto);

	public void toPrato(AtualizarPratoDTO dto, @MappingTarget Prato p);

	public PratoDTO toPrato(Prato dto);

}
