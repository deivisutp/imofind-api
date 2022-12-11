package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.ScrappingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrappingService {

    @Autowired
    private ScrappingUtil scrappingUtil;

    @Autowired
    private ImovelService imovelService;



    public List<ImovelDTO> buscarImoveis() {
        List<ImovelDTO> lista = scrappingUtil.obterInfoImoveis("");

        return lista;
    }

    public void verificaImoveisPeriodo() {
        List<ImovelDTO> lista = scrappingUtil.obterInfoImoveis("");

        imovelService.save(lista);
    }
}
