package br.com.deivisutp.imofindapi.repository.implementation;

import br.com.deivisutp.imofindapi.dto.ImovelFilterDTO;
import br.com.deivisutp.imofindapi.entities.Imovel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class ImovelRepositoryImpl {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void save(Imovel imovel) {
        entityManager.persist(imovel);
    }

    public Long count(ImovelFilterDTO filter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Imovel> root = criteriaQuery.from(Imovel.class);
        List<Predicate> predicateList = prepareSql(criteriaBuilder, root, filter);

        TypedQuery<Long> typedQuery = entityManager.createQuery(
                criteriaQuery.select(criteriaBuilder.count(root))
                .where(predicateList.toArray(
                        new Predicate[0])));

        return typedQuery.getSingleResult();
    }

    public List<Imovel> findAll(ImovelFilterDTO filter, Long totalElements) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Imovel> criteriaQuery = criteriaBuilder.createQuery(Imovel.class);
        Root<Imovel> root = criteriaQuery.from(Imovel.class);
        List<Predicate> predicateList = prepareSql(criteriaBuilder, root, filter);

        TypedQuery<Imovel> typedQuery = entityManager.createQuery(
                criteriaQuery.select(root)
                        .where(predicateList.toArray(
                                new Predicate[0])));

        List<Imovel> results = pagination(typedQuery, filter.getPage(), filter.getSize(), totalElements);
        return results;
    }

    private List<Predicate> prepareSql(CriteriaBuilder criteriaBuilder,
                                       Root<Imovel> root,
                                       ImovelFilterDTO filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
        }

        if (filter.getCity() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + filter.getCity().toLowerCase(Locale.ROOT) + "%"));
        }

        if (filter.getInitialPrice() != null) {

            if (filter.getEndPrice() != null) {
                predicates.add(criteriaBuilder.between(root.get("price"),filter.getInitialPrice(),filter.getEndPrice()));
            } else {
                predicates.add(criteriaBuilder.greaterThan(root.get("price"), filter.getInitialPrice()));
            }
        }

        if (filter.getTitulo() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")),"%" + filter.getTitulo().toLowerCase(Locale.ROOT) + "%"));
        }

        if (filter.getExtra() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("extra")), "%" +filter.getExtra().toLowerCase(Locale.ROOT) + "%"));
        }

        if (filter.getOrigem() != null) {
            predicates.add(criteriaBuilder.equal(root.get("origem"), filter.getOrigem()));
        }

        if (filter.getNeighborhood() != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("neighborhood")), "%" + filter.getNeighborhood().toLowerCase(Locale.ROOT) + "%"));
        }

        if (filter.getType() != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), filter.getType()));
        }
        return predicates;
    }

    private List<Imovel> pagination(TypedQuery<Imovel> query, final Integer page, final Integer pageSize, final Long totalElements){

        int maxResults = pageSize;
        int index = (page - 1) * pageSize;

        if (index > totalElements)
            return new ArrayList<>();

        if ((totalElements - index) <= maxResults)
            maxResults = (int) (totalElements - index);

        return query.setFirstResult(index).setMaxResults(maxResults).getResultList();
    }
}
