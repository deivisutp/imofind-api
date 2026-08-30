package br.com.deivisutp.imofindapi.controller;

import br.com.deivisutp.imofindapi.dto.ImovelFilterDTO;
import br.com.deivisutp.imofindapi.dto.ImovelResponseDTO;
import br.com.deivisutp.imofindapi.dto.NeighborhoodAggregate;
import br.com.deivisutp.imofindapi.dto.VariacoesResponseDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.repository.filter.ImovelFilter;
import br.com.deivisutp.imofindapi.service.ImovelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imoveis")
@Tag(name = "Imóveis", description = "API de imóveis")
public class ImovelController {

    @Autowired
    private ImovelService imovelService;

    @Deprecated
    @GetMapping("/busca")
    public ResponseEntity<Page<Imovel>> getImoveis(@RequestParam(required = false) String descricao,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "3") int size) {
        Pageable paging = PageRequest.of(page, size);
        ImovelFilter search = new ImovelFilter();
        return new ResponseEntity<>(imovelService.serachImoveis(search, descricao, paging),
                HttpStatus.OK);
    }

    @Operation(summary = "Buscar imóveis")
    @GetMapping("/buscar")
    public ResponseEntity<ImovelResponseDTO> get(ImovelFilterDTO filter) {
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSize() == null) filter.setSize(20);

        Long totalElements = imovelService.count(filter);
        List<Imovel> result = imovelService.getImoveis(filter, totalElements);

        ImovelResponseDTO response = new ImovelResponseDTO(result, totalElements);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Indicadores agregados por bairro")
    @GetMapping("/indicadores")
    public ResponseEntity<List<NeighborhoodAggregate>> indicadores(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(imovelService.getNeighborhoodAggregates(city, type));
    }

    @Operation(summary = "Fontes disponiveis")
    @GetMapping("/fontes")
    public ResponseEntity<List<String>> fontes() {
        return ResponseEntity.ok(imovelService.getSources());
    }

    @Operation(summary = "Tipos disponiveis")
    @GetMapping("/tipos")
    public ResponseEntity<List<String>> tipos() {
        return ResponseEntity.ok(imovelService.getTypes());
    }

    @Operation(summary = "Cidades disponiveis")
    @GetMapping("/cidades")
    public ResponseEntity<List<String>> cidades() {
        return ResponseEntity.ok(imovelService.getCities());
    }

    @Operation(summary = "Variacoes recentes (novos, reducoes, removidos)")
    @GetMapping("/variacoes")
    public ResponseEntity<VariacoesResponseDTO> variacoes(@RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(imovelService.getVariacoes(dias));
    }
}
