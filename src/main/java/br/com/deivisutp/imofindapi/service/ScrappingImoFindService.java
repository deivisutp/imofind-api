package br.com.deivisutp.imofindapi.service;
import br.com.deivisutp.imofindapi.util.IScrapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrappingImoFindService {

    @Autowired
    ImovelService imovelService;

    private final List<IScrapping> scrappingList;

    public ScrappingImoFindService(List<IScrapping> scrappingList) {
        this.scrappingList = scrappingList;
    }

    public void executeScrappingService() {
        imovelService.delete();

        scrappingList.forEach(
                x -> {
                    x.varrerImoveis();
                }
        );
    }
}
