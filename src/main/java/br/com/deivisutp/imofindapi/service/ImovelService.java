package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.dto.ImovelFilterDTO;
import br.com.deivisutp.imofindapi.dto.NeighborhoodAggregate;
import br.com.deivisutp.imofindapi.dto.VariacoesResponseDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.exception.NotFoundException;
import br.com.deivisutp.imofindapi.repository.ImovelRepository;
import br.com.deivisutp.imofindapi.repository.ListingEventRepository;
import br.com.deivisutp.imofindapi.repository.filter.ImovelFilter;
import br.com.deivisutp.imofindapi.repository.implementation.ImovelRepositoryImpl;
import br.com.deivisutp.imofindapi.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ImovelService {

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ImovelRepositoryImpl imovelRepo;

    @Autowired
    private ListingEventRepository listingEventRepository;

    @Autowired
    private ListingIngestService listingIngestService;

    @PersistenceContext
    private EntityManager em;

    private CriteriaBuilder builder;

    private void constructBuilder() {
        if  (this.builder == null)
            this.builder = em.getCriteriaBuilder();
    }

    public Imovel buscarImovelId(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nenhum imóvel encontrado com o id informado: " + id));
    }

    public List<ImovelDTO> listarImoveis() {
        return imovelRepository.findAll().stream()
                .map(ImovelService::toDto)
                .collect(Collectors.toList());
    }

    private static ImovelDTO toDto(Imovel imovel) {
        ImovelDTO dto = new ImovelDTO();
        dto.setId(imovel.getId());
        dto.setTitulo(imovel.getTitulo());
        dto.setExtra(imovel.getExtra());
        dto.setPrice(imovel.getPrice());
        dto.setPrice_varchar(imovel.getPrice_varchar());
        dto.setOrigem(imovel.getOrigem());
        dto.setLink(imovel.getLink());
        dto.setImage(imovel.getImage());
        dto.setCity(imovel.getCity());
        dto.setNeighborhood(imovel.getNeighborhood());
        dto.setType(imovel.getType());
        return dto;
    }

    public Page<Imovel> serachImoveis(ImovelFilter filter, String descricao, Pageable pageable) {
        if (descricao != null && !descricao.isEmpty())
            return new PageImpl<>(imovelRepository.searchImoveisLikeTitulo(descricao), pageable, total(filter));

        return new PageImpl<>(imovelRepository.findAll(), pageable, total(filter));

    }

    private Predicate[] criarRestricoes(ImovelFilter filter, Root<Imovel> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (!StringUtils.isEmpty(filter.getCodigoImovel())) {
            predicates.add(
                    builder.equal(root.get("id"),filter.getCodigoImovel())
            );
        }

        if (!StringUtils.isEmpty(filter.getDescricaoImovel())) {
            predicates.add(
                    builder.like(builder.lower(root.get("titulo")), "%" + filter.getDescricaoImovel().toLowerCase(Locale.ROOT) + "%"));
        }

        if (!StringUtils.isEmpty(filter.getOrigemImovel())) {
            predicates.add(
                    builder.like(builder.lower(root.get("origem")), "%" + filter.getOrigemImovel().toLowerCase(Locale.ROOT) + "%"));
        }

        if (!StringUtils.isEmpty(filter.getValorImovel())) {
            predicates.add(
                    builder.between(root.get("price"), BigDecimal.ZERO, filter.getValorImovel()));
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private Long total(ImovelFilter filter) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<Imovel> root = criteria.from(Imovel.class);
        Predicate[] predicates = criarRestricoes(filter, root);
        criteria.where(predicates);
        criteria.select(builder.count(root));
        return em.createQuery(criteria).getSingleResult();
    }

    public void save(List<ImovelDTO> lista) {
        if (lista == null) {
            return;
        }
        lista.forEach(listingIngestService::ingest);
    }

    public void save(ImovelDTO imovelDTO) {
        Imovel imovel = imovelRepository.findById(imovelDTO.getId()).orElseThrow(() -> new RuntimeException("Imóvel não encontrado."));

        imovel.setTitulo(DataUtil.getDataString(imovelDTO.getTitulo(), imovel.getTitulo()));
        imovel.setImage(DataUtil.getDataString(imovelDTO.getImage(), imovel.getImage()));
        imovel.setExtra(DataUtil.getDataString(imovelDTO.getExtra(), imovel.getExtra()));
        imovel.setCity(DataUtil.getDataString(imovelDTO.getCity(), imovel.getCity()));
        imovel.setLink(DataUtil.getDataString(imovelDTO.getLink(), imovel.getLink()));
        imovel.setNeighborhood(DataUtil.getDataString(imovelDTO.getNeighborhood(), imovel.getNeighborhood()));
        imovel.setOrigem(DataUtil.getDataString(imovelDTO.getOrigem(), imovel.getOrigem()));
        imovel.setType(DataUtil.getDataString(imovelDTO.getType(), imovel.getType()));
        imovel.setPrice_varchar(DataUtil.getDataString(imovelDTO.getPrice_varchar(), imovel.getPrice_varchar()));
        imovel.setPrice(imovelDTO.getPrice() != null ? imovelDTO.getPrice() : imovel.getPrice());

        imovelRepo.save(imovel);
    }

    public Long count(ImovelFilterDTO filter) {
       return imovelRepo.count(filter);
    }

    public List<Imovel> getImoveis(ImovelFilterDTO filter, Long totalElements) {
        return imovelRepo.findAll(filter, totalElements);
    }

    public List<NeighborhoodAggregate> getNeighborhoodAggregates(String city, String type) {
        return imovelRepository.aggregateByNeighborhood(emptyToNull(city), emptyToNull(type));
    }

    public List<String> getSources() {
        return imovelRepository.findDistinctSources();
    }

    public List<String> getTypes() {
        return imovelRepository.findDistinctTypes();
    }

    public List<String> getCities() {
        return imovelRepository.findDistinctCities();
    }

    public VariacoesResponseDTO getVariacoes(int dias, int limite) {
        int capped = Math.min(Math.max(limite, 1), 200); // teto evita LIMIT abusivo vindo do cliente
        return new VariacoesResponseDTO(
                listingEventRepository.resumoVariacoes(dias),
                listingEventRepository.reducoesRecentes(dias, capped));
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
