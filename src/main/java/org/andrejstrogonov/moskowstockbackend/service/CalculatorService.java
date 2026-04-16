package org.andrejstrogonov.moskowstockbackend.service;

import org.andrejstrogonov.moskowstockbackend.dto.*;
import org.andrejstrogonov.moskowstockbackend.model.Portfolio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculatorService {

    public DepositResponse calculateDeposit(DepositRequest request) {
        // Simple compound interest calculation
        double principal = request.getPrincipal();
        double rate = request.getAnnualRate() / 100;
        int years = request.getYears();
        int compoundingFrequency = request.getCompoundingFrequency();

        double finalAmount = principal * Math.pow(1 + rate / compoundingFrequency, compoundingFrequency * years);
        double totalInterest = finalAmount - principal;

        List<MonthlyData> monthlyBreakdown = new ArrayList<>();
        // For simplicity, generate monthly data
        double balance = principal;
        for (int month = 1; month <= years * 12; month++) {
            double interestEarned = balance * (rate / 12);
            balance += interestEarned;
            monthlyBreakdown.add(new MonthlyData(month, balance, interestEarned));
        }

        return new DepositResponse(finalAmount, totalInterest, monthlyBreakdown);
    }

    public LoanResponse calculateLoan(LoanRequest request) {
        // Simple loan calculation with early repayments
        double principal = request.getPrincipal();
        double annualRate = request.getAnnualRate() / 100;
        int years = request.getYears();
        List<EarlyRepayment> earlyRepayments = request.getEarlyRepayments();

        int totalMonths = years * 12;
        double monthlyRate = annualRate / 12;
        double monthlyPayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, totalMonths)) / (Math.pow(1 + monthlyRate, totalMonths) - 1);

        List<MonthlyPayment> monthlyPayments = new ArrayList<>();
        double remainingBalance = principal;
        double totalInterest = 0;

        for (int month = 1; month <= totalMonths; month++) {
            double interest = remainingBalance * monthlyRate;
            double principalPayment = monthlyPayment - interest;
            remainingBalance -= principalPayment;

            // Check for early repayment
            for (EarlyRepayment er : earlyRepayments) {
                if (er.getMonth() == month) {
                    remainingBalance -= er.getAmount();
                    principalPayment += er.getAmount();
                }
            }

            totalInterest += interest;
            monthlyPayments.add(new MonthlyPayment(month, monthlyPayment, principalPayment, interest, remainingBalance));

            if (remainingBalance <= 0) break;
        }

        double totalPaid = principal + totalInterest;
        return new LoanResponse(monthlyPayments, totalInterest, totalPaid);
    }

    public BlackScholesResponse calculateBlackScholes(BlackScholesRequest request) {
        // Placeholder for Black-Scholes calculation
        // In real implementation, use proper formulas
        double optionPrice = 10.0; // dummy
        double delta = 0.5;
        double gamma = 0.1;
        double theta = -0.05;
        double vega = 0.2;
        double rho = 0.3;

        return new BlackScholesResponse(optionPrice, delta, gamma, theta, vega, rho);
    }

    public BondYieldResponse calculateBondYield(BondYieldRequest request) {
        // Placeholder for bond yield calculation
        double yieldToMaturity = 5.0; // dummy
        double currentYield = request.getCouponRate() / request.getCurrentPrice() * 100;
        double duration = 5.0;
        double modifiedDuration = duration / (1 + yieldToMaturity / 100);

        return new BondYieldResponse(yieldToMaturity, currentYield, duration, modifiedDuration);
    }

    public StockAnalysisResponse analyzeStock(StockAnalysisRequest request) {
        // Placeholder for stock analysis
        double fairValue = request.getEps() * request.getPe() * 1.1; // dummy
        Recommendation recommendation = Recommendation.HOLD;
        RiskLevel riskLevel = RiskLevel.MEDIUM;
        StockAnalysisResponse.SectorComparison sectorComparison = new StockAnalysisResponse.SectorComparison(15.0, 10.0, 3.0);

        return new StockAnalysisResponse(fairValue, recommendation, riskLevel, sectorComparison);
    }

    public GoalResponse generateGoalPortfolio(GoalRequest request) {
        // Placeholder for goal portfolio generation
        Portfolio portfolio = new Portfolio(); // dummy
        double expectedReturn = request.getTargetAnnualReturnPercent();
        boolean achievable = true;
        String recommendation = "Invest in diversified assets";

        return new GoalResponse(portfolio, expectedReturn, achievable, recommendation);
    }
}
