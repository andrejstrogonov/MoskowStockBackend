package org.andrejstrogonov.moskowstockbackend.service;

import org.andrejstrogonov.moskowstockbackend.model.Portfolio;
import org.andrejstrogonov.moskowstockbackend.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    public Optional<Portfolio> getPortfolioById(String id) {
        return portfolioRepository.findById(id);
    }

    public Portfolio createPortfolio(Portfolio portfolio) {
        portfolio.setCreatedDate(LocalDateTime.now());
        portfolio.setLastModified(LocalDateTime.now());
        return portfolioRepository.save(portfolio);
    }

    public Portfolio updatePortfolio(String id, Portfolio portfolio) {
        if (portfolioRepository.existsById(id)) {
            portfolio.setId(id);
            portfolio.setLastModified(LocalDateTime.now());
            return portfolioRepository.save(portfolio);
        }
        return null;
    }

    public boolean deletePortfolio(String id) {
        if (portfolioRepository.existsById(id)) {
            portfolioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
