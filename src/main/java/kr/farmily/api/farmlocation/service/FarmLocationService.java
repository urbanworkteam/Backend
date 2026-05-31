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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmLocationService {

    private final FarmLocationRepository repository;
    private final KmaGridConverter gridConverter;

    @Transactional(readOnly = true)
    public List<FarmLocation> listMine(long userId) {
        return repository.findByUserIdOrderBySortOrderAscIdAsc(userId);
    }

    @Transactional
    public FarmLocation create(long userId, FarmLocationRequest req) {
        int nextOrder = (int) repository.countByUserId(userId);
        FarmLocation loc = repository.save(FarmLocation.create(userId, req.label(), req.address(), nextOrder));
        applyCoordinates(loc, req.lat(), req.lng());
        return loc;
    }

    @Transactional
    public FarmLocation update(long userId, long id, FarmLocationPatchRequest req) {
        FarmLocation loc = requireOwner(userId, id);
        loc.update(req.label(), req.address());
        if (req.lat() != null && req.lng() != null) {
            applyCoordinates(loc, req.lat(), req.lng());
        }
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

    private void applyCoordinates(FarmLocation loc, BigDecimal lat, BigDecimal lng) {
        KmaGridConverter.Grid g = gridConverter.toGrid(lat.doubleValue(), lng.doubleValue());
        loc.applyGeocode(lat, lng, g.x(), g.y());
    }
}
