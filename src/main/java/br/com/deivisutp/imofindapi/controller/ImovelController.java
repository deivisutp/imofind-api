package br.com.deivisutp.imofindapi.controller;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.dto.ImovelResponseDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.exception.StandardError;
import br.com.deivisutp.imofindapi.repository.filter.ImovelFilter;
import br.com.deivisutp.imofindapi.service.ImovelService;
import br.com.deivisutp.imofindapi.service.ScrappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api("Api de imoveis")
public class ImovelController {

    @Autowired
    private ImovelService imovelService;

    @Autowired
    private ScrappingService scrappingService;

    @ApiOperation(value = "Buscar imóveis")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImovelResponseDTO.class),
            @ApiResponse(code = 400, message = "Bad request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal server error", response = StandardError.class)
    })
    @GetMapping("/buscarImoveis")
    public ResponseEntity<List<ImovelDTO>> buscarImoveis() {
        List<ImovelDTO> lista = scrappingService.buscarImoveis();

        return ResponseEntity.ok().body(lista);
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<Imovel>> getImoveis(@RequestParam(required = false) String descricao,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "3") int size) {
        Pageable paging = PageRequest.of(page, size);
        ImovelFilter search = new ImovelFilter();
        return new ResponseEntity<>(imovelService.serachImoveis(search, descricao, paging),
                HttpStatus.OK);
    }
}
