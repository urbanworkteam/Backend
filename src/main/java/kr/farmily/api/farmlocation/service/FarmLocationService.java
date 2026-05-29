package kr.farmily.api.farmlocation.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.dto.FarmLocationPatchRequest;
import kr.farmily.api.farmlocation.dto.FarmLocationRequest;
import kr.farmily.api.farmlocation.repository.FarmLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmLocationService {

    private final FarmLocationRepository repository;
    private final GeocodingService geocodingService;

    @Transactional(readOnly = true)
    public List<FarmLocation> listMine(long userId) {
        return repository.findByUserIdOrderBySortOrderAscIdAsc(userId);
    }

    @Transactional
    public FarmLocation create(long userId, FarmLocationRequest req) {
        int nextOrder = (int) repository.countByUserId(userId);
        FarmLocation loc = repository.save(FarmLocation.create(userId, req.label(), req.address(), nextOrder));
        geocodingService.enrich(loc);
        return loc;
    }

    @Transactional
    public FarmLocation update(long userId, long id, FarmLocationPatchRequest req) {
        FarmLocation loc = requireOwner(userId, id);
        boolean addressChanged = req.address() != null && !req.address().equals(loc.getAddress());
        loc.update(req.label(), req.address());
        if (addressChanged) geocodingService.enrich(loc);
        return loc;
    }

    @Transactional
    public void delete(long userId, long id) {
        FarmLocation loc = requireOwner(userId, id);
        repository.delete(loc);
    }

    @Transactional(readOnly = true)
    public FarmLocation requireOwner(long userId, long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FARM_LOCATION_NOT_FOUND));
    }
}
