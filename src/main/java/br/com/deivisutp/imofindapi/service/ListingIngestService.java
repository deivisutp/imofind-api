package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.entities.CrawlRun;
import br.com.deivisutp.imofindapi.entities.Imovel;
import br.com.deivisutp.imofindapi.entities.ListingEvent;
import br.com.deivisutp.imofindapi.entities.ListingEventType;
import br.com.deivisutp.imofindapi.entities.ListingObservation;
import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.entities.SourceListing;
import br.com.deivisutp.imofindapi.repository.ImovelRepository;
import br.com.deivisutp.imofindapi.repository.ListingEventRepository;
import br.com.deivisutp.imofindapi.repository.ListingObservationRepository;
import br.com.deivisutp.imofindapi.repository.SourceListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import br.com.deivisutp.imofindapi.util.PropertyTypeNormalizer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Ingestão append-only: mantém histórico por anúncio (source_listing + observações + eventos)
 * e atualiza a projeção de estado corrente (imovel), sem nunca apagar o histórico.
 */
@Service
public class ListingIngestService {

    private final SourceService sourceService;
    private final SourceListingRepository listingRepository;
    private final ListingObservationRepository observationRepository;
    private final ListingEventRepository eventRepository;
    private final ImovelRepository imovelRepository;

    public ListingIngestService(SourceService sourceService,
                                SourceListingRepository listingRepository,
                                ListingObservationRepository observationRepository,
                                ListingEventRepository eventRepository,
                                ImovelRepository imovelRepository) {
        this.sourceService = sourceService;
        this.listingRepository = listingRepository;
        this.observationRepository = observationRepository;
        this.eventRepository = eventRepository;
        this.imovelRepository = imovelRepository;
    }

    @Transactional
    public IngestResult ingest(ImovelDTO dto) {
        return ingest(dto, null);
    }

    @Transactional
    public IngestResult ingest(ImovelDTO dto, CrawlRun run) {
        if (dto == null) {
            return new IngestResult(null, false, 0);
        }
        Instant now = Instant.now();
        Source source = sourceService.getOrCreate(dto.getOrigem());
        String externalId = md5(naturalKey(dto));

        SourceListing listing = listingRepository.findBySourceAndExternalId(source, externalId).orElse(null);
        boolean isNew = false;
        boolean reactivated = false;
        if (listing == null) {
            listing = new SourceListing();
            listing.setSource(source);
            listing.setExternalId(externalId);
            listing.setUrl(dto.getLink());
            listing.setFirstSeenAt(now);
            listing.setLastSeenAt(now);
            listing.setActive(Boolean.TRUE);
            listing = listingRepository.save(listing);
            isNew = true;
        } else {
            if (Boolean.FALSE.equals(listing.getActive())) {
                listing.setActive(Boolean.TRUE);
                reactivated = true;
            }
            listing.setLastSeenAt(now);
            listing = listingRepository.save(listing);
        }

        String hash = contentHash(dto);
        ListingObservation latest = observationRepository
                .findTopBySourceListingOrderByIdDesc(listing).orElse(null);
        boolean changed = latest == null || !hash.equals(latest.getContentHash());
        BigDecimal oldPrice = latest != null ? latest.getPrice() : null;

        boolean observationCreated = false;
        if (changed) {
            observationRepository.save(buildObservation(listing, run, dto, hash, now));
            observationCreated = true;
        }

        int eventsCreated = 0;
        if (isNew) {
            if (emit(ListingEventType.NEW, listing, run, null, dto.getPrice(), "NEW|" + listing.getId())) {
                eventsCreated++;
            }
        } else if (reactivated) {
            if (emit(ListingEventType.REACTIVATED, listing, run, oldPrice, dto.getPrice(),
                    "REACT|" + listing.getId() + "|" + hash)) {
                eventsCreated++;
            }
        }
        if (changed && !isNew && priceDiffers(oldPrice, dto.getPrice())) {
            if (emit(ListingEventType.PRICE_CHANGED, listing, run, oldPrice, dto.getPrice(),
                    "PRICE|" + listing.getId() + "|" + hash)) {
                eventsCreated++;
            }
        }

        updateProjection(listing, dto, now, Boolean.TRUE);
        return new IngestResult(externalId, observationCreated, eventsCreated);
    }

    @Transactional
    public int markRemoved(Source source, Set<String> seenExternalIds, CrawlRun run) {
        List<SourceListing> actives = listingRepository.findBySourceAndActiveTrue(source);
        int removed = 0;
        for (SourceListing listing : actives) {
            if (seenExternalIds.contains(listing.getExternalId())) {
                continue;
            }
            listing.setActive(Boolean.FALSE);
            listingRepository.save(listing);
            ListingObservation latest = observationRepository
                    .findTopBySourceListingOrderByIdDesc(listing).orElse(null);
            BigDecimal lastPrice = latest != null ? latest.getPrice() : null;
            String runPart = run != null ? String.valueOf(run.getId()) : "manual";
            emit(ListingEventType.REMOVED, listing, run, lastPrice, null,
                    "REMOVED|" + listing.getId() + "|" + runPart);
            imovelRepository.findBySourceListingId(listing.getId()).ifPresent(imovel -> {
                imovel.setActive(Boolean.FALSE);
                imovelRepository.save(imovel);
            });
            removed++;
        }
        return removed;
    }

    private ListingObservation buildObservation(SourceListing listing, CrawlRun run,
                                                ImovelDTO dto, String hash, Instant now) {
        ListingObservation obs = new ListingObservation();
        obs.setSourceListing(listing);
        obs.setCrawlRun(run);
        obs.setObservedAt(now);
        obs.setTitle(dto.getTitulo());
        obs.setExtra(dto.getExtra());
        obs.setPrice(dto.getPrice());
        obs.setPriceText(dto.getPrice_varchar());
        obs.setCity(dto.getCity());
        obs.setNeighborhood(dto.getNeighborhood());
        obs.setType(dto.getType());
        obs.setImageUrl(dto.getImage());
        obs.setContentHash(hash);
        return obs;
    }

    private boolean emit(ListingEventType type, SourceListing listing, CrawlRun run,
                         BigDecimal oldPrice, BigDecimal newPrice, String dedupKey) {
        if (eventRepository.existsByDedupKey(dedupKey)) {
            return false;
        }
        ListingEvent event = new ListingEvent();
        event.setSourceListing(listing);
        event.setCrawlRun(run);
        event.setEventType(type);
        event.setOccurredAt(Instant.now());
        event.setOldPrice(oldPrice);
        event.setNewPrice(newPrice);
        event.setDedupKey(dedupKey);
        eventRepository.save(event);
        return true;
    }

    private void updateProjection(SourceListing listing, ImovelDTO dto, Instant now, boolean active) {
        Imovel imovel = imovelRepository.findBySourceListingId(listing.getId()).orElseGet(Imovel::new);
        imovel.setSourceListingId(listing.getId());
        imovel.setExternalId(listing.getExternalId());
        imovel.setTitulo(dto.getTitulo());
        imovel.setExtra(dto.getExtra());
        imovel.setPrice(dto.getPrice());
        imovel.setOrigem(dto.getOrigem());
        imovel.setPrice_varchar(dto.getPrice_varchar());
        imovel.setLink(dto.getLink());
        imovel.setImage(dto.getImage());
        imovel.setCity(dto.getCity());
        imovel.setNeighborhood(dto.getNeighborhood());
        imovel.setType(PropertyTypeNormalizer.normalize(dto.getType()));
        imovel.setArea(dto.getArea());
        imovel.setActive(active);
        if (imovel.getFirstSeenAt() == null) {
            imovel.setFirstSeenAt(listing.getFirstSeenAt());
        }
        imovel.setLastSeenAt(now);
        imovelRepository.save(imovel);
    }

    private static String naturalKey(ImovelDTO dto) {
        String link = dto.getLink();
        if (link != null && !link.trim().isEmpty()) {
            return link.trim();
        }
        return String.join("|", nullSafe(dto.getOrigem()), nullSafe(dto.getTitulo()), nullSafe(dto.getPrice_varchar()));
    }

    private static String contentHash(ImovelDTO dto) {
        String raw = String.join("\u0001",
                nullSafe(dto.getTitulo()),
                nullSafe(dto.getExtra()),
                dto.getPrice() == null ? "" : dto.getPrice().toPlainString(),
                nullSafe(dto.getPrice_varchar()),
                nullSafe(dto.getCity()),
                nullSafe(dto.getNeighborhood()),
                nullSafe(dto.getType()),
                nullSafe(dto.getImage()));
        return md5(raw);
    }

    private static boolean priceDiffers(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return false;
        }
        if (a == null || b == null) {
            return true;
        }
        return a.compareTo(b) != 0;
    }

    private static String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
