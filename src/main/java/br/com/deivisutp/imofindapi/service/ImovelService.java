package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.exception.NotFoundException;
import br.com.deivisutp.imofindapi.repository.ImovelRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImovelService {

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Imovel buscarImovelId(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nenhum imóvel encontrado com o id informado: " + id));
    }

    public Imovel buscarEquipePorNome(String titulo) {
        return imovelRepository.findByTitulo(titulo)
                .orElseThrow(() -> new NotFoundException("Nenhum imóvel encontrado com o nome informado"));
    }

    public List<ImovelDTO> listarImoveis() {
        List<ImovelDTO> imoveis = new ArrayList<>();
        imoveis = modelMapper.map(imovelRepository.findAll(), List.class);
        return imoveis;
    }

    public void delete() {
        imovelRepository.deleteAll();
    }

    public void save(List<ImovelDTO> lista) {
        List<Imovel> imoveis = lista
                .stream()
                .map(imovel -> modelMapper.map(imovel, Imovel.class))
                .collect(Collectors.toList());

        imovelRepository.saveAll(imoveis);
    }
}
