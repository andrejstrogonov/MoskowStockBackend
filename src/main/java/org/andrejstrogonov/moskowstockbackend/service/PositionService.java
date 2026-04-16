package org.andrejstrogonov.moskowstockbackend.service;

import org.andrejstrogonov.moskowstockbackend.model.Position;
import org.andrejstrogonov.moskowstockbackend.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    public List<Position> getPositionsByPortfolioId(String portfolioId) {
        return positionRepository.findByPortfolioId(portfolioId);
    }

    public Optional<Position> getPositionById(String id) {
        return positionRepository.findById(id);
    }

    public Position createPosition(Position position) {
        return positionRepository.save(position);
    }

    public Position updatePosition(String id, Position position) {
        if (positionRepository.existsById(id)) {
            position.setId(id);
            return positionRepository.save(position);
        }
        return null;
    }

    public boolean deletePosition(String id) {
        if (positionRepository.existsById(id)) {
            positionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
