package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.dto.ImovelFilterDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.exception.NotFoundException;
import br.com.deivisutp.imofindapi.repository.ImovelRepository;
import br.com.deivisutp.imofindapi.repository.filter.ImovelFilter;
import br.com.deivisutp.imofindapi.repository.implementation.ImovelRepositoryImpl;
import br.com.deivisutp.imofindapi.util.DataUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
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
    private ModelMapper modelMapper;

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
        List<ImovelDTO> imoveis = new ArrayList<>();
        imoveis = modelMapper.map(imovelRepository.findAll(), List.class);
        return imoveis;
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
}
