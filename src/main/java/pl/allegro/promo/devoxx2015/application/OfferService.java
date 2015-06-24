package pl.allegro.promo.devoxx2015.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OfferService {

    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {
    	List<Offer> offers = events.stream()
    			.map(ope -> new Offer(ope.getId()
    					, ope.getTitle()
    					, ope.getPhotoUrl()
    					, score(ope)))
    			.filter(o -> o.hasPrettyPhoto())
    			.collect(Collectors.toList());
    	offerRepository.insert(offers);
    }

	private double score(OfferPublishedEvent ope) {
		try {
			return photoScoreSource.getScore(ope.getPhotoUrl());
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			return 0.7;
		}
	}

    public List<Offer> getOffers() {
    	List<Offer> os = offerRepository.findAll();
    	os.sort(Comparator.<Offer, Double>comparing(o -> o.getPhotoScore()).reversed());
    	return os;
    }
}
