package br.com.deivisutp.imofindapi.controller;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.dto.ImovelFilterDTO;
import br.com.deivisutp.imofindapi.dto.ImovelResponseDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.repository.filter.ImovelFilter;
import br.com.deivisutp.imofindapi.service.ImovelService;
import br.com.deivisutp.imofindapi.service.ScrappingImoFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imoveis")
@Tag(name = "Imóveis", description = "API de imóveis")
public class ImovelController {

    @Autowired
    private ImovelService imovelService;

    @Autowired
    private ScrappingImoFindService scrappingImoFindService;

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

    @PostMapping("/scrapingRealState")
    public ResponseEntity<String> varrerImoveis() {
        scrappingImoFindService.executeScrappingService();
        return ResponseEntity.ok().body("ok");
    }

    @PostMapping("/update")
    public ResponseEntity update( @Valid @RequestBody ImovelDTO imovel) {
        imovelService.save(imovel);
        return ResponseEntity.noContent().build();
    }
}
